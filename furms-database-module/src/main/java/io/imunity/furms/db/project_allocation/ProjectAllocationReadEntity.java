/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;

import io.imunity.furms.db.community_allocation.CommunityAllocationEntity;
import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.db.resource_credits.ResourceCreditEntity;
import io.imunity.furms.db.resource_types.ResourceTypeEntity;
import io.imunity.furms.db.sites.SiteEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("project_allocation")
public class ProjectAllocationReadEntity extends UUIDIdentifiable {

	@Column("site_id")
	public final SiteEntity site;
	@Column("resource_type_id")
	public final ResourceTypeEntity resourceType;
	@Column("resource_credit_id")
	public final ResourceCreditEntity resourceCredit;
	@Column("community_allocation_id")
	public final CommunityAllocationEntity communityAllocation;
	public final UUID projectId;
	public final String projectName;
	public final String name;
	public final BigDecimal amount;
	public final LocalDateTime creationTime;

	ProjectAllocationReadEntity(UUID id, String name,
	                            UUID projectId, BigDecimal amount, SiteEntity site, ResourceTypeEntity resourceType,
	                            ResourceCreditEntity resourceCredit, CommunityAllocationEntity communityAllocation, String projectName,
	                            LocalDateTime creationTime) {
		this.id = id;
		this.name = name;
		this.projectId = projectId;
		this.projectName = projectName;
		this.amount = amount;
		this.site = site;
		this.resourceType = resourceType;
		this.resourceCredit = resourceCredit;
		this.communityAllocation = communityAllocation;
		this.creationTime = creationTime;
	}

	@Override
	public String toString() {
		return "CommunityAllocationReadEntity{" +
			"site=" + site +
			", resourceType=" + resourceType +
			", resourceCredit=" + resourceCredit +
			", communityAllocation=" + communityAllocation +
			", projectId='" + projectId + '\'' +
			", projectName='" + projectName + '\'' +
			", name='" + name + '\'' +
			", amount=" + amount +
			", id=" + id +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationReadEntity that = (ProjectAllocationReadEntity) o;
		return Objects.equals(site, that.site)
			&& Objects.equals(id, that.id)
			&& Objects.equals(resourceType, that.resourceType)
			&& Objects.equals(resourceCredit, that.resourceCredit)
			&& Objects.equals(communityAllocation, that.communityAllocation)
			&& Objects.equals(projectId, that.projectId)
			&& Objects.equals(name, that.name)
			&& Objects.equals(projectName, that.projectName)
			&& Objects.equals(amount, that.amount)
			&& Objects.equals(creationTime, that.creationTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, site, resourceType, resourceCredit, communityAllocation, projectId, projectName, name, amount, creationTime);
	}

}
