/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.server;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import io.imunity.furms.spi.mtc.UnityMonitoringDAO;
import io.imunity.furms.spi.mtc.UnityMonitoringDAO.UnityConnectException;

@Controller
class UnityServerDetector {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UnityMonitoringDAO monitoringDAO;

	UnityServerDetector(UnityMonitoringDAO monitoringDAO) {
		this.monitoringDAO = monitoringDAO;
	}

	void waitForUnityToStartUp() throws InterruptedException {
		for (long retryCount = 1; retryCount <= 120; retryCount++) {

			if (isUnityAvailable()) {
				LOG.debug("Unity is up and running");
				return;
			} else {
				LOG.info("Waiting for unity.");
			}

			TimeUnit.SECONDS.sleep(1);
		}
	}

	private boolean isUnityAvailable() {
		try {
			monitoringDAO.ping();
			return true;
		} catch (UnityConnectException e) {
			LOG.trace("Unity is not yet available.", e);
			return false;
		}
	}
}
