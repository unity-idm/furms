/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.sites.data;

import io.imunity.furms.domain.sites.Site;

import java.util.Objects;

public class SiteDataGrid {

	private final String id;
	private String name;

	public static SiteDataGrid of(Site site) {
		return new SiteDataGrid(site.getId(), site.getName());
	}

	private SiteDataGrid(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteDataGrid that = (SiteDataGrid) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "SiteGridData{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				'}';
	}
}
