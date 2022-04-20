/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation;

import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.sites.Site;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ProjectAllocationResolved {

	public final ProjectAllocationId id;
	public final Site site;
	public final ResourceType resourceType;
	public final ResourceCredit resourceCredit;
	public final CommunityAllocation communityAllocation;
	public final ProjectId projectId;
	public final String projectName;
	public final String name;
	public final BigDecimal amount;
	public final BigDecimal consumed;
	public final LocalDateTime creationTime;

	ProjectAllocationResolved(ProjectAllocationId id, Site site, ResourceType resourceType, ResourceCredit resourceCredit,
	                          CommunityAllocation communityAllocation, ProjectId projectId, String projectName, String name,
	                          BigDecimal amount, BigDecimal consumed, LocalDateTime creationTime) {
		this.id = id;
		this.site = site;
		this.resourceType = resourceType;
		this.resourceCredit = resourceCredit;
		this.communityAllocation = communityAllocation;
		this.projectId = projectId;
		this.projectName = projectName;
		this.name = name;
		this.amount = amount;
		this.consumed = consumed;
		this.creationTime = creationTime;
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
			Objects.equals(projectName, that.projectName) &&
			Objects.equals(communityAllocation, that.communityAllocation) &&
			Objects.equals(name, that.name) &&
			Objects.equals(consumed, that.consumed) &&
			Objects.equals(creationTime, that.creationTime) &&
			Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, site, resourceType, resourceCredit, communityAllocation, projectId, projectName, name, amount, consumed, creationTime);
	}

	@Override
	public String toString() {
		return "CommunityAllocation{" +
			"id=" + id +
			", site=" + site +
			", resourceType=" + resourceType +
			", resourceCredit=" + resourceCredit +
			", projectId=" + projectId +
			", projectName=" + projectName +
			", communityAllocation=" + communityAllocation +
			", name='" + name + '\'' +
			", amount='" + amount + '\'' +
			", consumed='" + consumed + '\'' +
			", creationTime='" + creationTime + '\'' +
			'}';
	}

	public static CommunityAllocationResolvedBuilder builder() {
		return new CommunityAllocationResolvedBuilder();
	}

	public static final class CommunityAllocationResolvedBuilder {
		private ProjectAllocationId id;
		private Site site;
		private ResourceType resourceType;
		private ResourceCredit resourceCredit;
		private CommunityAllocation communityAllocation;
		private ProjectId projectId;
		private String projectName;
		private String name;
		private BigDecimal amount;
		private BigDecimal consumed;
		private LocalDateTime creationTime;

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
			this.id = new ProjectAllocationId(id);
			return this;
		}

		public CommunityAllocationResolvedBuilder projectId(String projectId) {
			this.projectId = new ProjectId(projectId);
			return this;
		}

		public CommunityAllocationResolvedBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public CommunityAllocationResolvedBuilder projectName(String projectName) {
			this.projectName = projectName;
			return this;
		}

		public CommunityAllocationResolvedBuilder consumed(BigDecimal consumed) {
			this.consumed = consumed;
			return this;
		}

		public CommunityAllocationResolvedBuilder creationTime(LocalDateTime creationTime) {
			this.creationTime = creationTime;
			return this;
		}

		public ProjectAllocationResolved build() {
			return new ProjectAllocationResolved(id, site, resourceType, resourceCredit, communityAllocation, projectId,
				projectName, name, amount, consumed, creationTime);
		}
	}
}
