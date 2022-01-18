/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import java.util.Objects;

public class SiteCreatedEvent implements SiteEvent {
	public final Site site;

	public SiteCreatedEvent(Site site) {
		this.site = site;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteCreatedEvent siteEvent = (SiteCreatedEvent) o;
		return Objects.equals(site, siteEvent.site);
	}

	@Override
	public int hashCode() {
		return Objects.hash(site);
	}

	@Override
	public String toString() {
		return "SiteCreatedEvent{" +
			"site='" + site + '\'' +
			'}';
	}
}
