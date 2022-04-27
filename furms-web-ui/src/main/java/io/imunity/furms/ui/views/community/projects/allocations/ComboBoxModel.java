/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.ProjectId;

import java.util.Objects;

public class ComboBoxModel {
	private final ProjectId id;
	private final CommunityId communityId;
	private final String name;

	public ComboBoxModel(ProjectId id, CommunityId communityId, String name) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
	}

	public ProjectId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public CommunityId getCommunityId() {
		return communityId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ComboBoxModel that = (ComboBoxModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ComboBoxModel{" +
			"id='" + id + '\'' +
			", communityId='" + communityId + '\'' +
			", name='" + name + '\'' +
			'}';
	}
}
