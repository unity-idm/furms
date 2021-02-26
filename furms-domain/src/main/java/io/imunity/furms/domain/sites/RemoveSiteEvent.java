/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import java.util.Objects;

public class RemoveSiteEvent implements SiteEvent{
	public final String id;

	public RemoveSiteEvent(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RemoveSiteEvent siteEvent = (RemoveSiteEvent) o;
		return Objects.equals(id, siteEvent.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "RemoveSiteEvent{" +
			"id='" + id + '\'' +
			'}';
	}
}
