/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import java.util.Objects;
import java.util.UUID;

import io.imunity.furms.domain.Id;

public class SiteId implements Id {
	public final UUID id;
	public final SiteExternalId externalId;

	public SiteId(String id, SiteExternalId externalId) {
		if(id == null || id.isBlank() || externalId == null || externalId.id == null || externalId.id.isBlank())
			throw new IllegalArgumentException("Site id or external id should not be null or empty");
		this.id = UUID.fromString(id);
		this.externalId = externalId;
	}

	public SiteId(String id, String externalId) {
		if(id == null || id.isBlank() || externalId == null || externalId.isBlank())
			throw new IllegalArgumentException("Site id or external id should not be null or empty");
		this.id = UUID.fromString(id);
		this.externalId = new SiteExternalId(externalId);
	}

	public SiteId(String id) {
		this.id = UUID.fromString(id);
		this.externalId = null;
	}

	public SiteId(UUID id) {
		this.id = id;
		this.externalId = null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteId siteId = (SiteId) o;
		return Objects.equals(id, siteId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "SiteId{" + "id='" + id + '\'' + ", externalId=" + externalId + '}';
	}

	@Override
	public String asRawString() {
		return id == null ? null : id.toString();
	}
}
