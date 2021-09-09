/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command;

import io.imunity.furms.cli.command.community.CommunityCommand;
import io.imunity.furms.cli.command.community.GroupCommand;
import io.imunity.furms.cli.command.project.ProjectCommand;
import io.imunity.furms.cli.command.site.SiteCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Unmatched;

import java.util.List;

@Command(
		mixinStandardHelpOptions = true,
		subcommands = {
				CommunityCommand.class,
				GroupCommand.class,
				ProjectCommand.class,
				SiteCommand.class
		}
)
public class RootFurmsCommand implements Runnable {

	@Unmatched
	private List<String> ignoredParams;

	@Override
	public void run() {
		System.out.println("Pick your command. Type --help or -h for more info.");
	}
}
