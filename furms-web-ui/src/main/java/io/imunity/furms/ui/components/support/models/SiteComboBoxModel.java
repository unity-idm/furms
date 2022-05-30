/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.support.models;

import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

public class SiteComboBoxModel {
	private final SiteId id;
	private final String name;

	public SiteComboBoxModel(SiteId id, String name) {
		this.id = id;
		this.name = name;
	}

	public SiteId getId() {
		return id;
	}

	public String getName() {
		return name;
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
		return "ComboBoxModel{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			'}';
	}
}
