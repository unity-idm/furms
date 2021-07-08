/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.resource_credits.ResourceCreditWithAllocations;
import io.imunity.furms.domain.resource_types.ResourceType;

import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.domain.resource_types.ResourceMeasureUnit.NONE;
import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCZoned;

class ResourceCredit {
	public final SiteCreditId id;
	public final String name;
	public final Validity validity;
	public final String resourceTypeId;
	public final ResourceAmount amount;

	ResourceCredit(SiteCreditId id, String name, Validity validity, String resourceTypeId,
			ResourceAmount amount) {
		this.id = id;
		this.name = name;
		this.validity = validity;
		this.resourceTypeId = resourceTypeId;
		this.amount = amount;
	}

	ResourceCredit(io.imunity.furms.domain.resource_credits.ResourceCredit credit,
	                      Optional<ResourceType> resource) {
		this(new SiteCreditId(credit.siteId, credit.id),
				credit.name,
				new Validity(convertToUTCZoned(credit.utcStartTime), convertToUTCZoned(credit.utcEndTime)),
				credit.resourceTypeId,
				new ResourceAmount(credit.amount, resource.map(type -> type.unit.getSuffix()).orElse(NONE.getSuffix())));
	}

	ResourceCredit(ResourceCreditWithAllocations credit) {
		this(new SiteCreditId(credit.getSiteId(), credit.getId()),
				credit.getName(),
				new Validity(convertToUTCZoned(credit.getUtcStartTime()), convertToUTCZoned(credit.getUtcEndTime())),
				credit.getResourceType().id,
				new ResourceAmount(credit.getAmount(), credit.getResourceType().unit.getSuffix()));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCredit that = (ResourceCredit) o;
		return Objects.equals(id, that.id)
				&& Objects.equals(name, that.name)
				&& Objects.equals(validity, that.validity)
				&& Objects.equals(resourceTypeId, that.resourceTypeId)
				&& Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, validity, resourceTypeId, amount);
	}

	@Override
	public String toString() {
		return "ResourceCredit{" +
				"id=" + id +
				", name='" + name + '\'' +
				", validity=" + validity +
				", resourceTypeId='" + resourceTypeId + '\'' +
				", amount=" + amount +
				'}';
	}
}
