/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command;

import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.OFF;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.http.util.TextUtils.isEmpty;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.LoggerContext;
import io.imunity.furms.cli.CLIApplication;
import io.imunity.furms.cli.ConfigParameter;
import io.imunity.furms.cli.ConfigParameterNames;
import io.imunity.furms.cli.client.FurmsClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public abstract class FurmsCommand implements Runnable {

	private final static String DEFAULT_CONFIG_FILE = System.getProperty("user.home") + "/.furms.properties";

	protected final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	protected FurmsClient furmsClient;
	private Optional<Properties> configurationFromFile;

	@Option(names = {ConfigParameterNames.USERNAME}, description = "User identifier obtained from FURMS.")
	protected String username;

	@Option(names = {ConfigParameterNames.API_KEY}, description = "API access key generated in FURMS.")
	protected String apiKey;

	@Option(names = {ConfigParameterNames.FURMS_URL}, description = "FURMS installation URL, e.g. https://furms.example.com")
	protected String furmsurl;

	@Option(names = {"--config-file"}, description = "Path of an optional config file, with general settings.")
	protected String configFile;

	@Option(names = {ConfigParameterNames.TRUSTSTORE_PATH}, description = "Path of HTTPS truststore.")
	protected String truststore;

	@Option(names = {ConfigParameterNames.TRUSTSTORE_TYPE}, description = "Type of the trustore, either 'PKCS12' or 'JKS'.")
	protected String truststoreType;
	
	@Option(names = {ConfigParameterNames.TRUSTSTORE_PASS}, description = "Password of the truststore.")
	protected String truststorepass;

	@Option(names = {"-s", "--silent"}, description = "Print only JSON result of an operation.")
	protected boolean silentMode;

	@Option(names = {"-v", "--verbose"}, description = "Enable verbose mode, overrides -s.")
	protected boolean verboseMode;

	@Option(names = {"-d", "--debug"}, description = "Enables all logs for debugging purposes, overrides -v and -s.")
	protected boolean debugMode;


	protected abstract void executeCommand() throws Exception;

	@Override
	public final void run() {
		configureLogger();
		configurationFromFile = loadConfigurationFromFile();
		username = findRequiredPropertyValue(username, ConfigParameter.USERNAME);
		apiKey = findRequiredPropertyValue(apiKey, ConfigParameter.APIKEY);
		furmsurl = findRequiredPropertyValue(furmsurl, ConfigParameter.URL);
		truststore = findRequiredPropertyValue(truststore, ConfigParameter.TRUSTSTORE);
		truststoreType = findRequiredPropertyValue(truststoreType, ConfigParameter.TRUSTSTORE_TYPE);
		truststorepass = findRequiredPropertyValue(truststorepass, ConfigParameter.TRUSTSTORE_PASS);

		if (isEmpty(username) || isEmpty(apiKey) || isEmpty(furmsurl) || isEmpty(truststore) || isEmpty(truststorepass))
			return;
		
		furmsClient = FurmsClient.builder()
				.url(furmsurl)
				.username(username)
				.apiKey(apiKey)
				.trustStore(truststore)
				.trustStorePassword(truststorepass)
				.trustStoreType(truststoreType)
				.build();

		try {
			executeCommand();
		} catch (Exception e) {
			LOG.error("Error during command execution: {}", e.toString());
			LOG.debug("Error details", e);
			return;
		}
	}

	private void configureLogger() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		if (debugMode) {
			ch.qos.logback.classic.Logger furmsLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
			furmsLogger.setLevel(DEBUG);
		} else if (verboseMode) {
			ch.qos.logback.classic.Logger furmsLogger = loggerContext.getLogger(
					CLIApplication.class.getPackageName());
			if (furmsLogger != null)
				furmsLogger.setLevel(DEBUG);
		} else if (silentMode) {
			loggerContext.getLoggerList().forEach(logger -> logger.setLevel(OFF));
		}
	}

	protected String readFile(String filePath) {
		try {
			Resource resource = new FileSystemResource(filePath);
			byte[] bytes = FileCopyUtils.copyToByteArray(resource.getFile());
			return new String(bytes);
		} catch (IOException e) {
			LOG.error("Unable to find file {}", filePath);
			throw new IllegalArgumentException(e);
		}
	}

	protected String toJson(Object object) throws Exception {
		return objectMapper.writeValueAsString(object);
	}

	private String findRequiredPropertyValue(String argument, ConfigParameter param) {
		String value = findPropertyValue(argument, param);
		if (isEmpty(value))
		{
			LOG.error("Required parameter is not provided: provide either '{}' command line param, "
					+ "set it in config file under '{}' property or define environment variable '{}'",
					param.getOption(), param.getProperty(), param.getEnvironmentVariable());
			return null;
		} else
			LOG.debug("Config param {}='{}'", param.option, value);
		return value;
	}
	
	private String findPropertyValue(String argument, ConfigParameter param) {
		return ofNullable(argument)
				.or(findInEnvironmentVariables(param))
				.or(findInConfigFile(param))
				.or(findDefaultValue(param))
				.or(() -> {
					return Optional.empty();
				})
				.orElse(null);
	}

	private Supplier<Optional<String>> findInConfigFile(ConfigParameter param) {
		return () -> configurationFromFile.map(p -> p.getProperty(param.getProperty()));
	}

	private Supplier<Optional<String>> findInEnvironmentVariables(ConfigParameter param) {
		return () -> ofNullable(System.getenv(param.getEnvironmentVariable()));
	}

	private Supplier<Optional<String>> findDefaultValue(ConfigParameter param) {
		return () -> ofNullable(param.getDefaultValue());
	}

	private Optional<Properties> loadConfigurationFromFile() {
		String usedConfigFile = isEmpty(configFile) ? DEFAULT_CONFIG_FILE : configFile;
		return loadConfigFile(usedConfigFile);
	}
	
	private Optional<Properties> loadConfigFile(String configFile) {
		try {
			Resource resource = new FileSystemResource(configFile);
			Properties props = PropertiesLoaderUtils.loadProperties(resource);
			LOG.debug("Configuration loaded from file: {}", configFile);
			return Optional.of(props);
		} catch (IOException e) {
			LOG.debug("Configuration file was not loaded from {}: {}", configFile, e.toString());
			return empty();
		}
	}
}
