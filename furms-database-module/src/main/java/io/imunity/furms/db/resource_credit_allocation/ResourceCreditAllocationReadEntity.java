/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credit_allocation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.db.resource_credits.ResourceCreditEntity;
import io.imunity.furms.db.resource_types.ResourceTypeEntity;
import io.imunity.furms.db.sites.SiteEntity;
import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocationExtend;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Objects;

@Table("resource_credit_allocation")
public class ResourceCreditAllocationReadEntity extends UUIDIdentifiable {

	@Column("site_id")
	public final SiteEntity site;
	@Column("resource_type_id")
	public final ResourceTypeEntity resourceType;
	@Column("resource_credit_id")
	public final ResourceCreditEntity resourceCredit;
	public final String communityId;
	public final String name;
	public final BigDecimal amount;

	ResourceCreditAllocationReadEntity(SiteEntity site, ResourceTypeEntity resourceType,
	                                   ResourceCreditEntity resourceCredit, String communityId, String name,
	                                   BigDecimal amount) {
		this.site = site;
		this.resourceType = resourceType;
		this.resourceCredit = resourceCredit;
		this.communityId = communityId;
		this.name = name;
		this.amount = amount;
	}

	ResourceCreditAllocationExtend toResourceCreditAllocation() {
		return ResourceCreditAllocationExtend.builder()
			.id(id.toString())
			.site(site.toSite())
			.resourceType(resourceType.toResourceType())
			.resourceCredit(resourceCredit.toResourceCredit())
			.communityId(communityId)
			.name(name)
			.amount(amount)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCreditAllocationReadEntity that = (ResourceCreditAllocationReadEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(site, that.site) &&
			Objects.equals(resourceType, that.resourceType) &&
			Objects.equals(resourceCredit, that.resourceCredit) &&
			Objects.equals(name, that.name) &&
			Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, site, resourceType, resourceCredit, name, amount);
	}

	@Override
	public String toString() {
		return "ResourceCreditAllocationReadEntity{" +
			"site=" + site +
			", resourceType=" + resourceType +
			", resourceCredit=" + resourceCredit +
			", name='" + name + '\'' +
			", amount=" + amount +
			", id=" + id +
			'}';
	}
}
