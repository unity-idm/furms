/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.community.allocations;

import java.util.Objects;

public class SiteComboBoxModel {
	public final String id;
	public final String name;

	public SiteComboBoxModel(String id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteComboBoxModel that = (SiteComboBoxModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ResourceTypeComboBoxModel{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			'}';
	}
}
