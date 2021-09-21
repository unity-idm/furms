/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli;

public enum ConfigParameter {
	
	URL (ConfigParameterNames.FURMS_URL,
			"url",
			"FURMS_URL",
			null),
	USERNAME (ConfigParameterNames.USERNAME,
			"username",
			"FURMS_USER",
			null),
	APIKEY (ConfigParameterNames.API_KEY,
			"apikey",
			"FURMS_APIKEY",
			null),
	TRUSTSTORE(ConfigParameterNames.TRUSTSTORE_PATH,
			"truststore",
			"FURMS_TRUSTSTORE",
			getDefaultTruststore()),
	TRUSTSTORE_TYPE(ConfigParameterNames.TRUSTSTORE_TYPE,
			"truststoreType",
			"FURMS_TRUSTSTORE_TYPE",
			"JKS"),
	TRUSTSTORE_PASS(ConfigParameterNames.TRUSTSTORE_PASS,
			"truststorePass",
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
	
	private static String getDefaultTruststore() {
		String javaHome = System.getenv("JAVA_HOME");
		return javaHome == null ? null : javaHome + "/lib/security/cacerts";
	}
}
