/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.cidp;

import io.imunity.furms.domain.users.ProjectMembership;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ProjectMembershipJson {
	public final String id;
	public final String name;
	public final List<AttributeJson> attributes;

	ProjectMembershipJson(String id, String name, List<AttributeJson> attributes) {
		this.id = id;
		this.name = name;
		this.attributes = List.copyOf(attributes);
	}
	
	ProjectMembershipJson(ProjectMembership membership) {
		this(membership.id.id.toString(), membership.name,
				membership.attributes.stream().map(AttributeJson::new).collect(toList()));
	}
	
}
