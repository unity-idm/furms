/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.domain.users;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class ProjectMembership {
	public final String id;
	public final String name;
	public final Set<UserAttribute> attributes;

	public ProjectMembership(String id, String name, Collection<UserAttribute> attributes) {
		this.id = id;
		this.name = name;
		this.attributes = Set.copyOf(attributes);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(attributes, id, name);
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
		ProjectMembership other = (ProjectMembership) obj;
		return Objects.equals(attributes, other.attributes) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name);
	}

	@Override
	public String toString()
	{
		return String.format("ProjectMembership [id=%s, name=%s, attributes=%s]", id, name, attributes);
	}
}
