/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import java.util.Objects;

public class SiteId {
	public final String id;
	public final SiteExternalId externalId;

	public SiteId(String id, SiteExternalId externalId) {
		this.id = id;
		this.externalId = externalId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteId siteId = (SiteId) o;
		return Objects.equals(id, siteId.id) && Objects.equals(externalId, siteId.externalId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, externalId);
	}

	@Override
	public String toString() {
		return "SiteId{" +
			"id='" + id + '\'' +
			", externalId=" + externalId +
			'}';
	}
}
