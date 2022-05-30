/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import com.google.common.collect.ImmutableList;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;

class Community {
	public final String id;
	public final String name;
	public final List<String> allocations;

	Community(String id, String name, List<String> allocations) {
		this.id = id;
		this.name = name;
		this.allocations = ImmutableList.copyOf(allocations);
	}

	Community(io.imunity.furms.domain.communities.Community community, Set<CommunityAllocation> allocations) {
		this(community.getId().id.toString(),
				community.getName(),
				allocations.stream()
				.map(allocation -> allocation.id.id.toString())
				.collect(toList()));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Community community = (Community) o;
		return Objects.equals(id, community.id)
				&& Objects.equals(name, community.name)
				&& Objects.equals(allocations, community.allocations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, allocations);
	}

	@Override
	public String toString() {
		return "Community{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", allocations=" + allocations +
				'}';
	}
}
