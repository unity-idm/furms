/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import java.util.Objects;

public class ComboBoxModel {
	private final String id;
	private final String communityId;
	private final String name;

	public ComboBoxModel(String id, String communityId, String name) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCommunityId() {
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
