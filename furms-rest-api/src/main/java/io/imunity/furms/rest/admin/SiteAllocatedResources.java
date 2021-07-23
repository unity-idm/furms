/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunkResolved;

import java.math.BigDecimal;
import java.util.Objects;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCZoned;

class SiteAllocatedResources {
	public final String allocationId;
	public final String siteId;
	public final BigDecimal amount;
	public final Validity validity;

	public SiteAllocatedResources(String allocationId, String siteId, BigDecimal amount, Validity validity) {
		this.allocationId = allocationId;
		this.siteId = siteId;
		this.amount = amount;
		this.validity = validity;
	}

	public SiteAllocatedResources(ProjectAllocationChunkResolved chunk) {
		this(chunk.projectAllocation.id, chunk.projectAllocation.site.getId(), chunk.amount,
				new Validity(convertToUTCZoned(chunk.validFrom), convertToUTCZoned(chunk.validTo)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteAllocatedResources that = (SiteAllocatedResources) o;
		return Objects.equals(allocationId, that.allocationId)
				&& Objects.equals(siteId, that.siteId)
				&& Objects.equals(amount, that.amount)
				&& Objects.equals(validity, that.validity);
	}

	@Override
	public int hashCode() {
		return Objects.hash(allocationId, siteId, amount, validity);
	}

	@Override
	public String toString() {
		return "SiteAllocatedResources{" +
				"allocationId='" + allocationId + '\'' +
				", siteId='" + siteId + '\'' +
				", amount=" + amount +
				", validity=" + validity +
				'}';
	}
}
