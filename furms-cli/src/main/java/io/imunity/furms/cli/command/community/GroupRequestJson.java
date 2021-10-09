/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.community;

import java.util.Objects;

public class GroupRequestJson {
	public final String name;
	public final String description;

	public GroupRequestJson(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GroupRequestJson that = (GroupRequestJson) o;
		return Objects.equals(name, that.name) && Objects.equals(description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description);
	}

	@Override
	public String toString() {
		return "GroupRequestJson{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				'}';
	}
}
