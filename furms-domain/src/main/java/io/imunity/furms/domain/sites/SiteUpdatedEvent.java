/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import java.util.Objects;

public class SiteUpdatedEvent implements SiteEvent {
	public final Site oldSite;
	public final Site newSite;

	public SiteUpdatedEvent(Site oldSite, Site newSite) {
		this.oldSite = oldSite;
		this.newSite = newSite;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteUpdatedEvent siteEvent = (SiteUpdatedEvent) o;
		return Objects.equals(oldSite, siteEvent.oldSite) &&
			Objects.equals(newSite, siteEvent.newSite);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldSite, newSite);
	}

	@Override
	public String toString() {
		return "SiteUpdatedEvent{" +
			"oldSite='" + oldSite + '\'' +
			",newSite='" + newSite + '\'' +
			'}';
	}
}
