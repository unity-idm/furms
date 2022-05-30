/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

class SiteGridItem implements Cloneable {

	private final SiteId id;
	private String name;

	static SiteGridItem of(Site site) {
		return new SiteGridItem(site.getId(), site.getName());
	}

	private SiteGridItem(SiteId id, String name) {
		this.id = id;
		this.name = name;
	}

	public SiteId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public SiteGridItem clone() {
		return new SiteGridItem(this.id, this.name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteGridItem that = (SiteGridItem) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "SiteGridItem{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				'}';
	}
}
