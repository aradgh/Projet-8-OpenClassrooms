package com.openclassrooms.tourguide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import gpsUtil.location.Location;

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
    private List<AttractionInfo> attractions = new ArrayList<>();

    public Location getUserLocation() {
        return userLocation;
    }

    public List<AttractionInfo> getAttractions() {
        return attractions;
    }

    public NearbyAttractionsDTO(Location userLocation, List<AttractionInfo> attractions) {
        this.userLocation = userLocation;
        this.attractions = attractions;
    }
}
