/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command;

public class CommandUtils {

	public static String createEmptyCommandMessage(String commandName) {
		return "Please select " + commandName + " command option (type --help for more info)";
	}

}
