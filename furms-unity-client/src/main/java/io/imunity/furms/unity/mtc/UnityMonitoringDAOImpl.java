/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.unity.mtc;

import java.net.ConnectException;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.imunity.furms.spi.mtc.UnityMonitoringDAO;
import io.imunity.furms.unity.config.WebClientConfig;

@Component
class UnityMonitoringDAOImpl implements UnityMonitoringDAO {

	private final WebClient unityClient;

	UnityMonitoringDAOImpl(@Qualifier(WebClientConfig.BASE_UNITY_CLIENT) WebClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public void ping() {
		unityClient.get()
				.uri("/unitygw/pub")
				.retrieve()
				.bodyToMono(Void.class)
				.onErrorMap(isConnectException(), ex -> new UnityConnectException(ex))
				.block();
	}

	private Predicate<? super Throwable> isConnectException() {
		return ex -> {
			Throwable exceptionToMatch = ex.getCause() != null ? ex.getCause() : ex;
			return exceptionToMatch.getClass().isAssignableFrom(ConnectException.class);
		};
	}
}
