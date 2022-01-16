/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import java.util.Objects;

public class SiteRemovedEvent implements SiteEvent {
	public final Site site;

	public SiteRemovedEvent(Site site) {
		this.site = site;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteRemovedEvent siteEvent = (SiteRemovedEvent) o;
		return Objects.equals(site, siteEvent.site);
	}

	@Override
	public int hashCode() {
		return Objects.hash(site);
	}

	@Override
	public String toString() {
		return "RemoveSiteEvent{" +
			"site='" + site + '\'' +
			'}';
	}
}
