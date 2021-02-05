/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.spi.mtc;

public interface UnityMonitoringDAO {

	void ping();

	static class UnityConnectException extends RuntimeException {
		public UnityConnectException(Throwable cause) {
			super(cause);
		}
	}
}
