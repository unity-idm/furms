/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.community_allocation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.db.resource_credits.ResourceCreditEntity;
import io.imunity.furms.db.resource_types.ResourceTypeEntity;
import io.imunity.furms.db.sites.SiteEntity;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Table("community_allocation")
public class CommunityAllocationReadEntity extends UUIDIdentifiable {

	@Column("site_id")
	public final SiteEntity site;
	@Column("resource_type_id")
	public final ResourceTypeEntity resourceType;
	@Column("resource_credit_id")
	public final ResourceCreditEntity resourceCredit;
	public final UUID communityId;
	public final String name;
	public final BigDecimal amount;


	CommunityAllocationReadEntity(UUID id, String name,
	                              UUID communityId, BigDecimal amount, SiteEntity site, ResourceTypeEntity resourceType,
	                              ResourceCreditEntity resourceCredit) {
		this.id = id;
		this.name = name;
		this.communityId = communityId;
		this.amount = amount;
		this.site = site;
		this.resourceType = resourceType;
		this.resourceCredit = resourceCredit;
	}

	CommunityAllocationResolved toCommunityAllocation() {
		return CommunityAllocationResolved.builder()
			.id(id.toString())
			.site(site.toSite())
			.resourceType(resourceType.toResourceType())
			.resourceCredit(resourceCredit.toResourceCredit())
			.communityId(communityId.toString())
			.name(name)
			.amount(amount)
			.build();
	}

	@Override
	public String toString() {
		return "CommunityAllocationReadEntity{" +
			"site=" + site +
			", resourceType=" + resourceType +
			", resourceCredit=" + resourceCredit +
			", name='" + name + '\'' +
			", amount=" + amount +
			", id=" + id +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocationReadEntity that = (CommunityAllocationReadEntity) o;
		return Objects.equals(site, that.site)
			&& Objects.equals(id, that.id)
			&& Objects.equals(resourceType, that.resourceType)
			&& Objects.equals(resourceCredit, that.resourceCredit)
			&& Objects.equals(communityId, that.communityId)
			&& Objects.equals(name, that.name)
			&& Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, site, resourceType, resourceCredit, communityId, name, amount);
	}


}
