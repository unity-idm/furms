/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.domain.users;

import java.util.List;

public class ProjectMembership {
	public final String name;
	public final List<Attribute> attributes;

	public ProjectMembership(String name, List<Attribute> attributes) {
		this.name = name;
		this.attributes = List.copyOf(attributes);
	}
}
