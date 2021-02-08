/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.cidp;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import io.imunity.furms.domain.users.CommunityMembership;

public class CommunityMembershipJson {
	public final String id;
	public final String name;
	public final List<GroupMembershipJson> groups;
	public final List<ProjectMembershipJson> projects;
	public final List<AttributeJson> attributes;
	
	CommunityMembershipJson(String id, String name, List<GroupMembershipJson> groups, List<ProjectMembershipJson> projects,
			List<AttributeJson> attributes) {
		this.id = id;
		this.name = name;
		this.groups = List.copyOf(groups);
		this.projects = List.copyOf(projects);
		this.attributes = List.copyOf(attributes);
	}
	
	public CommunityMembershipJson(CommunityMembership membership) {
		this(membership.id, membership.name, Collections.emptyList(), 
				membership.projects.stream().map(ProjectMembershipJson::new).collect(toList()), 
				membership.attributes.stream().map(AttributeJson::new).collect(toList()));
	}
}
