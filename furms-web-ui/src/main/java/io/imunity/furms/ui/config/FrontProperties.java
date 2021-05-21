/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "furms.front")
public class FrontProperties {

	private final String language;
	private final int maxSessionInactivity;
	private final int secondsBeforeShowingSessionExpirationWarning;


	public FrontProperties(String language, int maxSessionInactivity,
			int secondsBeforeShowingSessionExpirationWarning) {
		
		this.language = language;
		this.maxSessionInactivity = maxSessionInactivity;
		this.secondsBeforeShowingSessionExpirationWarning = secondsBeforeShowingSessionExpirationWarning;
	}

	public String getLanguage() {
		return language;
	}

	public int getMaxSessionInactivity() {
		return maxSessionInactivity;
	}

	public int getSecondsBeforeShowingSessionExpirationWarning() {
		return secondsBeforeShowingSessionExpirationWarning;
	}
}
