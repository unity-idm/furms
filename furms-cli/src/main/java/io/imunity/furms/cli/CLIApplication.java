/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.LoggerFactory;

import static ch.qos.logback.classic.Level.INFO;
import static io.imunity.furms.cli.command.FurmsCommandSpecFactory.commandLine;

public class CLIApplication {

	public static void main(String[] args) {
		initLogger();
		commandLine().execute(args);
	}

	private static void initLogger() {
		final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		final PatternLayout ple = new PatternLayout();
		ple.setPattern("[%-5level] %msg%n");
		ple.setContext(loggerContext);
		ple.start();
		final ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
		consoleAppender.setContext(loggerContext);
		consoleAppender.setLayout(ple);
		consoleAppender.start();

		loggerContext.getLoggerList().forEach(logger -> {
			logger.detachAndStopAllAppenders();
			logger.addAppender(consoleAppender);
			logger.setLevel(INFO);
			logger.setAdditive(false);
		});
	}
}
