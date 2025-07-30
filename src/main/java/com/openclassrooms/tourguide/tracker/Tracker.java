package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

public class Tracker implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(Tracker.class);
	private static final long TRACKING_POLLING_INTERVAL = TimeUnit.MINUTES.toSeconds(5);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final TourGuideService tourGuideService;
	private boolean stop = false;

	public Tracker(TourGuideService tourGuideService) {
		this.tourGuideService = tourGuideService;

		executorService.submit(this);
	}

	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}

	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();

		while (!Thread.currentThread().isInterrupted() && !stop) {
			List<User> users = tourGuideService.getAllUsers();
			logger.debug("Begin Tracker. Tracking {} users.", users.size());
			stopWatch.start();
			// Lancer les traitements asynchrones
			/*
			Ici, le .thenAccept(v -> {}) sert à transformer un CompletableFuture<VisitedLocation>
			en un CompletableFuture<Void>. Parce que CompletableFuture.allOf(...) attend un tableau de
			CompletableFuture<Void>. Il ne traite que des CompletableFuture<Void>.
			 */
			List<CompletableFuture<Void>> futures = users.stream()
				.map(user -> tourGuideService.trackUserLocation(user).thenAccept(v -> {}))
				.toList();

			// Attendre que tous soient terminés
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
			stopWatch.stop();
			logger.debug("Tracker Time Elapsed: {} seconds.", TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(TRACKING_POLLING_INTERVAL);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}

		logger.debug("Tracker stopping");

	}
}
