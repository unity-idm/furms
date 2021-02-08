/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.domain.users;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class CommunityMembership {
	public final String id;
	public final String name;
	public final Set<ProjectMembership> projects;
	public final Set<Attribute> attributes;
	
	public CommunityMembership(String id, String name, Collection<ProjectMembership> projects,
			Collection<Attribute> attributes) {
		this.id = id;
		this.name = name;
		this.projects = Set.copyOf(projects);
		this.attributes = Set.copyOf(attributes);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(attributes, id, name, projects);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommunityMembership other = (CommunityMembership) obj;
		return Objects.equals(attributes, other.attributes) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name) && Objects.equals(projects, other.projects);
	}

	@Override
	public String toString()
	{
		return String.format("CommunityMembership [id=%s, name=%s, projects=%s, attributes=%s]", id, name,
				projects, attributes);
	}
}
