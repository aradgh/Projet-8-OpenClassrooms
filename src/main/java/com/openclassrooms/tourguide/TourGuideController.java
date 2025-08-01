package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.dto.NearbyAttractionMapper;
import com.openclassrooms.tourguide.dto.NearbyAttractionsDTO;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rewardCentral.RewardCentral;
import tripPricer.Provider;

import java.util.List;

@RestController
public class TourGuideController {

	final TourGuideService tourGuideService;

    public TourGuideController(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;
    }

    @GetMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @GetMapping("/getLocation")
    public VisitedLocation getLocation(@RequestParam String userName) {
    	return tourGuideService.getUserLocation(getUser(userName));
    }

 	//  Returns a new JSON object that contains:
    	// Names of Tourist attractions,
        // Tourist attractions lat/long,
        // The user's location lat/long,
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
        //    Note: Attraction reward points can be gathered from RewardsCentral
    @GetMapping("/getNearbyAttractions")
    public NearbyAttractionsDTO getNearbyAttractions(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        List<Attraction> nearbyAttractions = tourGuideService.getNearByAttractions(visitedLocation);
        TourGuideModule tourGuideModule = new TourGuideModule();
        return new NearbyAttractionMapper(tourGuideModule.getRewardsService(), tourGuideModule.getRewardCentral())
            .map(nearbyAttractions, visitedLocation);
    }

    @GetMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(getUser(userName));
    }
       
    @GetMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(getUser(userName));
    }
    
    private User getUser(String userName) {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("userName must not be blank");
        }
        return tourGuideService.getUser(userName);
    }
   

}