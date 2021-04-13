/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation;

import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.sites.Site;

import java.math.BigDecimal;
import java.util.Objects;

public class ProjectAllocationResolved {

	public final String id;
	public final Site site;
	public final ResourceType resourceType;
	public final ResourceCredit resourceCredit;
	public final CommunityAllocation communityAllocation;
	public final String projectId;
	public final String name;
	public final BigDecimal amount;

	ProjectAllocationResolved(String id, Site site, ResourceType resourceType, ResourceCredit resourceCredit,
	                          CommunityAllocation communityAllocation, String projectId, String name, BigDecimal amount) {
		this.id = id;
		this.site = site;
		this.resourceType = resourceType;
		this.resourceCredit = resourceCredit;
		this.communityAllocation = communityAllocation;
		this.projectId = projectId;
		this.name = name;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationResolved that = (ProjectAllocationResolved) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(site, that.site) &&
			Objects.equals(resourceType, that.resourceType) &&
			Objects.equals(resourceCredit, that.resourceCredit) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(communityAllocation, that.communityAllocation) &&
			Objects.equals(name, that.name) &&
			Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, site, resourceType, resourceCredit, communityAllocation, projectId, name, amount);
	}

	@Override
	public String toString() {
		return "CommunityAllocation{" +
			"id=" + id +
			", site=" + site +
			", resourceType=" + resourceType +
			", resourceCredit=" + resourceCredit +
			", projectId=" + projectId +
			", communityAllocation=" + communityAllocation +
			", name='" + name + '\'' +
			", amount='" + amount + '\'' +
			'}';
	}

	public static CommunityAllocationResolvedBuilder builder() {
		return new CommunityAllocationResolvedBuilder();
	}

	public static final class CommunityAllocationResolvedBuilder {
		protected String id;
		public Site site;
		public ResourceType resourceType;
		public ResourceCredit resourceCredit;
		public CommunityAllocation communityAllocation;
		public String projectId;
		public String name;
		public BigDecimal amount;

		private CommunityAllocationResolvedBuilder() {
		}

		public CommunityAllocationResolvedBuilder site(Site site) {
			this.site = site;
			return this;
		}

		public CommunityAllocationResolvedBuilder resourceType(ResourceType resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public CommunityAllocationResolvedBuilder resourceCredit(ResourceCredit resourceCredit) {
			this.resourceCredit = resourceCredit;
			return this;
		}

		public CommunityAllocationResolvedBuilder communityAllocation(CommunityAllocation communityAllocation) {
			this.communityAllocation = communityAllocation;
			return this;
		}

		public CommunityAllocationResolvedBuilder name(String name) {
			this.name = name;
			return this;
		}

		public CommunityAllocationResolvedBuilder id(String id) {
			this.id = id;
			return this;
		}

		public CommunityAllocationResolvedBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public CommunityAllocationResolvedBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ProjectAllocationResolved build() {
			return new ProjectAllocationResolved(id, site, resourceType, resourceCredit, communityAllocation, projectId, name, amount);
		}
	}
}
