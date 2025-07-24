package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private static final int DEFAULT_PROXIMITY_BUFFER = 10;
    private int proximityBuffer = DEFAULT_PROXIMITY_BUFFER;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;
    private final ExecutorService executor = Executors.newFixedThreadPool(100);

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void shutdownExecutor() {
        executor.shutdown();
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = DEFAULT_PROXIMITY_BUFFER;
    }

    public CompletableFuture<Void> calculateRewards(User user) {
        /*
        Ici, il est nécessaire d'utiliser un objet thread-safe comme CopyOnWriteArrayList<E>
        car sinon, un autre thread comme celui de Tracker peut venir modifier la liste userLocations
        pendant que cette méthode calculateRewards fait un forEach dessus.
         */
        List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());

        return CompletableFuture.supplyAsync(gpsUtil::getAttractions, executor)
            .thenCompose(attractions -> {
                List<CompletableFuture<UserReward>> futures = new ArrayList<>();

                for (VisitedLocation visitedLocation : userLocations) {
                    attractions.stream()
                        .filter(attraction -> isEligibleForReward(user, visitedLocation, attraction))
                        .forEach(attraction -> {
                            CompletableFuture<UserReward> futureReward = getRewardPoints(attraction, user)
                                .thenApply(points -> new UserReward(visitedLocation, attraction, points));
                            futures.add(futureReward);
                        });
                }

                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenAccept(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .forEach(user::addUserReward)
                    );
            });
    }

    private boolean isEligibleForReward(User user, VisitedLocation visitedLocation, Attraction attraction) {
        boolean alreadyRewarded = user.getUserRewards().stream()
            .anyMatch(r -> r.attraction.attractionName.equals(attraction.attractionName));

        return !alreadyRewarded && isNearAttraction(visitedLocation, attraction);
    }


    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        int attractionProximityRange = 200;
        return getDistance(attraction, location) <= attractionProximityRange;
    }

    private boolean isNearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) <= proximityBuffer;
    }


    private CompletableFuture<Integer> getRewardPoints(Attraction attraction, User user) {
        return CompletableFuture.supplyAsync(() ->
            rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId()), executor);
    }

    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(
            Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }

}
