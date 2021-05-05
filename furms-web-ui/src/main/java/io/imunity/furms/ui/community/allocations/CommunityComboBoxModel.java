/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.community.allocations;

import io.imunity.furms.domain.communities.Community;

import java.util.Objects;

public class CommunityComboBoxModel {
	private final String id;
	private final String name;

	public CommunityComboBoxModel(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public CommunityComboBoxModel(Community community) {
		this.id = community.getId();
		this.name = community.getName();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityComboBoxModel that = (CommunityComboBoxModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "CommunityComboBoxModel{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			'}';
	}
}
