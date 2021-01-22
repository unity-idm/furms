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

	FrontProperties(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}
}
