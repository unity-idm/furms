/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

@ConstructorBinding
@ConfigurationProperties(prefix = "furms.front")
public class FrontProperties {

	private final String language;
	private final List<String> acceptedImgFiles;

	FrontProperties(String language, List<String> acceptedImgFiles) {
		this.language = language;
		this.acceptedImgFiles = acceptedImgFiles;
	}

	public String getLanguage() {
		return language;
	}

	public List<String> getAcceptedImgFiles() {
		return acceptedImgFiles;
	}
}
