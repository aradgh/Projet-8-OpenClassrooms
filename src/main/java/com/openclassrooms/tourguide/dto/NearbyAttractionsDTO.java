package com.openclassrooms.tourguide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openclassrooms.tourguide.TourGuideModule;
import com.openclassrooms.tourguide.service.RewardsService;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

import java.util.ArrayList;
import java.util.List;

public class NearbyAttractionsDTO {

    public static class AttractionInfo {
        public final String attractionName;
        public final Location attractionLocation;
        public final double distanceMiles;
        public final int rewardPoints;

        public AttractionInfo(String name, Location location, double distance, int points) {
            this.attractionName = name;
            this.attractionLocation = location;
            this.distanceMiles = distance;
            this.rewardPoints = points;
        }
    }

    @JsonProperty("userLocation")
    private final Location userLocation;

    @JsonProperty("nearbyAttractions")
    private final List<AttractionInfo> attractions = new ArrayList<>();

    public Location getUserLocation() {
        return userLocation;
    }

    public List<AttractionInfo> getAttractions() {
        return attractions;
    }

    public NearbyAttractionsDTO(List<Attraction> nearbyAttractions, VisitedLocation visitedLocation) {
        RewardCentral rewardCentral = new TourGuideModule().getRewardCentral();
        RewardsService rewardsService = new TourGuideModule().getRewardsService();

        this.userLocation = visitedLocation.location;

        for (Attraction a : nearbyAttractions) {
            double distance = rewardsService.getDistance(a, visitedLocation.location);
            int points = rewardCentral.getAttractionRewardPoints(a.attractionId, visitedLocation.userId);
            attractions.add(new AttractionInfo(a.attractionName, new Location(a.latitude, a.longitude), distance, points));
        }
    }
}
