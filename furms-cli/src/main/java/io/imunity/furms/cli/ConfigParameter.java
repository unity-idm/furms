/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli;

public enum ConfigParameter {

	URL ("--furmsurl",
			"connection.url",
			"FURMS_URL",
			null),
	USERNAME ("--user",
			"connection.username",
			"FURMS_USER",
			null),
	APIKEY ("--apikey",
			"connection.apikey",
			"FURMS_APIKEY",
			null),
	TRUSTSTORE("--truststore",
			"security.truststore",
			"FURMS_TRUSTSTORE",
			System.getProperty("sun.boot.library.path") + "/security/cacerts"),
	TRUSTSTORE_PASS("--truststorepass",
			"security.truststore.pass",
			"FURMS_TRUSTSTORE_PASS",
			"changeit");

	private final String option;
	private final String property;
	private final String environmentVariable;
	private final String defaultValue;

	ConfigParameter(String option, String property, String environmentVariable, String defaultValue) {
		this.option = option;
		this.property = property;
		this.environmentVariable = environmentVariable;
		this.defaultValue = defaultValue;
	}

	public String getOption() {
		return option;
	}

	public String getProperty() {
		return property;
	}

	public String getEnvironmentVariable() {
		return environmentVariable;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
