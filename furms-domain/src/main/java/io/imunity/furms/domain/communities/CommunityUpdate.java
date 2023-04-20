/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.communities;

import java.util.Objects;

public class CommunityUpdate
{
	public final CommunityId id;
	public final String name;
	public final String description;

	public CommunityUpdate(CommunityId id, String name, String description)
	{
		this.id = id;
		this.name = name;
		this.description = description;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityUpdate that = (CommunityUpdate) o;
		return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description,
			that.description);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, name, description);
	}

	@Override
	public String toString()
	{
		return "CommunityInstallation{" +
			"id=" + id +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			'}';
	}
}
