/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command;

import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.cli.ConfigParameter;
import io.imunity.furms.cli.logging.VerboseType;
import io.imunity.furms.cli.client.FurmsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.OFF;
import static io.imunity.furms.cli.logging.VerboseType.ALL;
import static io.imunity.furms.cli.logging.VerboseType.COMMAND;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.http.util.TextUtils.isEmpty;

@Command(mixinStandardHelpOptions = true)
public abstract class FurmsCommand implements Runnable {

	private final static String DEFAULT_CONFIG_FILE = System.getProperty("user.home") + "/furms.properties";

	protected final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected ObjectMapper objectMapper = new ObjectMapper();
	protected FurmsClient furmsClient;

	@Option(names = {"--user"})
	protected String username;

	@Option(names = {"--apikey"})
	protected String apiKey;

	@Option(names = {"--furmsurl"})
	protected String furmsurl;

	@Option(names = {"--configfile"})
	protected String configFile;

	@Option(names = {"--truststore"})
	protected String truststore;

	@Option(names = {"--truststorepass"})
	protected String truststorepass;

	@Option(names = {"-s", "--silent"}, description = "Printing only calls results in JSON.")
	protected boolean silentMode;

	@Option(names = {"-v", "--verbose"}, description = "Enable verbose mode. Options [COMMAND, ALL]")
	protected VerboseType verboseMode;

	protected abstract void executeCommand() throws Exception;

	@Override
	public final void run() {
		configureLogger();
		username = findPropertyValue(username, ConfigParameter.USERNAME);
		apiKey = findPropertyValue(apiKey, ConfigParameter.APIKEY);
		furmsurl = findPropertyValue(furmsurl, ConfigParameter.URL);
		truststore = findPropertyValue(truststore, ConfigParameter.TRUSTSTORE);
		truststorepass = findPropertyValue(truststorepass, ConfigParameter.TRUSTSTORE_PASS);

		if (isEmpty(username) || isEmpty(apiKey) || isEmpty(furmsurl) || isEmpty(truststore) || isEmpty(truststorepass)) {
			LOG.error("Required parameters has not been set. Read documentation for more info.");
			System.exit(-1);
		}
		furmsClient = FurmsClient.builder()
				.url(furmsurl)
				.username(username)
				.apiKey(apiKey)
				.trustStore(truststore)
				.trustStorePassword(truststorepass)
				.silentMode(silentMode)
				.build();

		try {
			executeCommand();
		} catch (Exception e) {
			LOG.error("Error during command execution", e);
			System.exit(-1);
		}
	}

	private void configureLogger() {
		final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		if (verboseMode == COMMAND) {
			final ch.qos.logback.classic.Logger commands = loggerContext.getLogger(FurmsCommand.class.getPackageName());

			if (commands != null) {
				commands.setLevel(DEBUG);
			}
			final ch.qos.logback.classic.Logger client = loggerContext.getLogger(FurmsClient.class);
			if (client != null) {
				client.setLevel(DEBUG);
			}
		} else if (verboseMode == ALL) {
			loggerContext.getLoggerList().forEach(logger -> logger.setLevel(DEBUG));
		}
		if (silentMode) {
			loggerContext.getLoggerList().forEach(logger -> logger.setLevel(OFF));
		}
	}

	protected String readFile(String filePath) {
		try {
			final Resource resource = new FileSystemResource(filePath);
			final byte[] bytes = FileCopyUtils.copyToByteArray(resource.getFile());
			return new String(bytes);
		} catch (IOException e) {
			LOG.error("Unable to find file {}", filePath);
			throw new IllegalArgumentException(e);
		}
	}

	protected String toJson(Object object) throws Exception {
		return objectMapper.writeValueAsString(object);
	}

	private String findPropertyValue(String argument, ConfigParameter param) {
		return ofNullable(argument)
				.or(findInConfigFile(param))
				.or(findInEnvironmentVariables(param))
				.or(findInDefaultConfigFile(param))
				.or(findDefaultValue(param))
				.or(() -> {
					LOG.error("Unable to find value for {}", param.getOption());
					return Optional.empty();
				})
				.orElse(null);
	}

	private Supplier<Optional<String>> findInConfigFile(ConfigParameter param) {
		return () -> {
			if (StringUtils.isEmpty(configFile)) {
				return empty();
			}
			return findInFile(configFile, param);
		};
	}

	private Supplier<Optional<String>> findInEnvironmentVariables(ConfigParameter param) {
		return () -> ofNullable(System.getenv(param.getEnvironmentVariable()));
	}

	private Supplier<Optional<String>> findInDefaultConfigFile(ConfigParameter param) {
		return () -> findInFile(DEFAULT_CONFIG_FILE, param);
	}

	private Supplier<Optional<String>> findDefaultValue(ConfigParameter param) {
		return () -> ofNullable(param.getDefaultValue());
	}

	private Optional<String> findInFile(String configFile, ConfigParameter param) {
		try {
			final Resource resource = new FileSystemResource(configFile);
			final Properties props = PropertiesLoaderUtils.loadProperties(resource);
			return ofNullable(props.getProperty(param.getProperty()));
		} catch (IOException e) {
			LOG.error("Unable to find file {}", configFile);
			return empty();
		}
	}
}
