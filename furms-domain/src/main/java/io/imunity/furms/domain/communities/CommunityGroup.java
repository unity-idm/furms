/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.communities;

import java.util.Objects;

public class CommunityGroup {
	private final CommunityId id;
	private final String name;

	public CommunityGroup(CommunityId id, String name) {
		this.id = id;
		this.name = name;
	}

	public CommunityId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static CommunityGroup.CommunityGroupBuilder builder() {
		return new CommunityGroup.CommunityGroupBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityGroup that = (CommunityGroup) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public String toString() {
		return "CommunityGroup{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			'}';
	}

	public static class CommunityGroupBuilder {
		private CommunityId id;
		private String name;

		public CommunityGroup.CommunityGroupBuilder id(CommunityId id) {
			this.id = id;
			return this;
		}

		public CommunityGroup.CommunityGroupBuilder name(String name) {
			this.name = name;
			return this;
		}

		public CommunityGroup build() {
			return new CommunityGroup(id, name);
		}
	}
}
