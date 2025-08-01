package com.openclassrooms.tourguide.dto;

import com.openclassrooms.tourguide.service.RewardsService;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

import java.util.ArrayList;
import java.util.List;

public class NearbyAttractionMapper {

    private final RewardsService rewardsService;
    private final RewardCentral rewardCentral;

    public NearbyAttractionMapper(RewardsService rewardsService, RewardCentral rewardCentral) {
        this.rewardsService = rewardsService;
        this.rewardCentral = rewardCentral;
    }

    public NearbyAttractionsDTO map(List<Attraction> nearbyAttractions, VisitedLocation visitedLocation) {
        List<NearbyAttractionsDTO.AttractionInfo> attractionInfos = new ArrayList<>();
        for (Attraction attraction : nearbyAttractions) {
            double distanceFromVisitedLocation = rewardsService.getDistance(attraction, visitedLocation.location);
            int attractionRewardPoints = rewardCentral.getAttractionRewardPoints(
                attraction.attractionId, visitedLocation.userId);
            attractionInfos.add(
                new NearbyAttractionsDTO.AttractionInfo(
                    attraction.attractionName, new Location(attraction.latitude, attraction.longitude), distanceFromVisitedLocation,
                    attractionRewardPoints
                ));
        }
        return new NearbyAttractionsDTO(visitedLocation.location, attractionInfos);
    }
}
