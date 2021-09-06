/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli;

import io.imunity.furms.cli.command.RootFurmsCommand;
import picocli.CommandLine;

public class CLIApplication {

	public static void main(String[] args) {
		new CommandLine(new RootFurmsCommand())
				.execute(args);
	}
}
