/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.io.File;

public class FurmsCommandSpecFactory {

	public static CommandLine commandLine() {
		final CommandSpec commandSpec = CommandSpec.forAnnotatedObject(RootFurmsCommand.class);
		commandSpec.name(findAppName());
		commandSpec.version(FurmsCommandSpecFactory.class.getPackage().getImplementationVersion());

		return new CommandLine(commandSpec);
	}

	private static String findAppName() {
		final File currentFile = new File(RootFurmsCommand.class
				.getProtectionDomain()
				.getCodeSource()
				.getLocation()
				.getPath());
		if (currentFile.exists()) {
			return currentFile.getName();
		} else {
			return "furms";
		}
	}

}
