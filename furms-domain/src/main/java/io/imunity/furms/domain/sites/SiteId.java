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
		if(id == null || id.isBlank() || externalId == null || externalId.id == null || externalId.id.isBlank())
			throw new IllegalArgumentException("Site id or external id should not be null or empty");
		this.id = id;
		this.externalId = externalId;
	}

	public SiteId(String id, String externalId) {
		if(id == null || id.isBlank() || externalId == null || externalId.isBlank())
			throw new IllegalArgumentException("Site id or external id should not be null or empty");
		this.id = id;
		this.externalId = new SiteExternalId(externalId);
	}

	public SiteId(String id) {
		this.id = id;
		this.externalId = null;
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
