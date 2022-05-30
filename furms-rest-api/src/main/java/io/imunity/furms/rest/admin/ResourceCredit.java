/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.resource_credits.ResourceCreditWithAllocations;

import java.util.Objects;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCZoned;

class ResourceCredit {
	public final String creditId;
	public final String name;
	public final Validity validity;
	public final String resourceTypeId;
	public final ResourceAmount amount;

	ResourceCredit(String creditId, String name, Validity validity, String resourceTypeId,
			ResourceAmount amount) {
		this.creditId = creditId;
		this.name = name;
		this.validity = validity;
		this.resourceTypeId = resourceTypeId;
		this.amount = amount;
	}

	ResourceCredit(ResourceCreditWithAllocations credit) {
		this(credit.getId().id.toString(),
				credit.getName(),
				new Validity(convertToUTCZoned(credit.getUtcStartTime()), convertToUTCZoned(credit.getUtcEndTime())),
				credit.getResourceType().id.id.toString(),
				new ResourceAmount(credit.getAmount(), credit.getResourceType().unit.getSuffix()));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCredit that = (ResourceCredit) o;
		return Objects.equals(creditId, that.creditId)
				&& Objects.equals(name, that.name)
				&& Objects.equals(validity, that.validity)
				&& Objects.equals(resourceTypeId, that.resourceTypeId)
				&& Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(creditId, name, validity, resourceTypeId, amount);
	}

	@Override
	public String toString() {
		return "ResourceCredit{" +
				"creditId=" + creditId +
				", name='" + name + '\'' +
				", validity=" + validity +
				", resourceTypeId='" + resourceTypeId + '\'' +
				", amount=" + amount +
				'}';
	}
}
