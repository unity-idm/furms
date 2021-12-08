/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.core.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
public class SecurityProperties {
	private final Optional<String> centralIdPSecret;
	private final Optional<String> centralIdPUsername;
	
	public SecurityProperties(
			@Value("${furms.psk.centralIdPSecret:}") String centralIdPSecret, 
			@Value("${furms.psk.centralIdPUser:}") String centralIdPUsername) {
		
		this.centralIdPSecret = ofNullable(isEmpty(centralIdPSecret) ? null : centralIdPSecret);
		this.centralIdPUsername = ofNullable(isEmpty(centralIdPUsername) ? null : centralIdPUsername);
	}

	public Optional<String> getCentralIdPSecret() {
		return centralIdPSecret;
	}
	
	public Optional<String> getCentralIdPUsername() {
		return centralIdPUsername;
	}
}
