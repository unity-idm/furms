/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.community;

class GroupRequestJson {
	public final String name;
	public final String description;

	public GroupRequestJson(String name, String description) {
		this.name = name;
		this.description = description;
	}
}
