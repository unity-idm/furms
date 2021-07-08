/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.resource_credits.ResourceCredit;

import java.util.Objects;

class SiteCreditId {
	public final String siteId;
	public final String creditId;

	SiteCreditId(String siteId, String creditId) {
		this.siteId = siteId;
		this.creditId = creditId;
	}

	SiteCreditId(ResourceCredit resourceCredit) {
		this(resourceCredit.siteId, resourceCredit.id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteCreditId that = (SiteCreditId) o;
		return Objects.equals(siteId, that.siteId)
				&& Objects.equals(creditId, that.creditId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, creditId);
	}

	@Override
	public String toString() {
		return "SiteCreditId{" +
				"siteId='" + siteId + '\'' +
				", creditId='" + creditId + '\'' +
				'}';
	}
}
