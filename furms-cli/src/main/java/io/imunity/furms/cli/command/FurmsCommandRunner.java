/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine;

@Component
@Command
public class FurmsCommandRunner implements CommandLineRunner, ExitCodeGenerator {

	private final RootFurmsCommand rootFurmsCommand;
	private final IFactory factory;

	private int exitCode;

	public FurmsCommandRunner(IFactory factory) {
		this.rootFurmsCommand = new RootFurmsCommand();
		this.factory = factory;
	}

	@Override
	public void run(String... args) throws Exception {
		exitCode = new CommandLine(rootFurmsCommand, factory)
				.execute(args);
	}

	@Override
	public int getExitCode() {
		return exitCode;
	}

}
