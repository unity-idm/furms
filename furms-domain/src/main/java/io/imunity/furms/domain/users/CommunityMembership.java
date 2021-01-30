/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.domain.users;

import java.util.List;

public class CommunityMembership {
	public final String name;
	public final List<ProjectMembership> projects;
	public final List<Attribute> attributes;
	
	public CommunityMembership(String name, List<ProjectMembership> projects,
			List<Attribute> attributes) {
		this.name = name;
		this.projects = List.copyOf(projects);
		this.attributes = List.copyOf(attributes);
	}
}
