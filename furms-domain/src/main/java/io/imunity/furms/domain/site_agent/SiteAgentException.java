/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.site_agent;

public class SiteAgentException extends RuntimeException{
	public SiteAgentException() {
	}

	public SiteAgentException(String message) {
		super(message);
	}

	public SiteAgentException(String message, Throwable cause) {
		super(message, cause);
	}

	public SiteAgentException(Throwable cause) {
		super(cause);
	}

	public SiteAgentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
