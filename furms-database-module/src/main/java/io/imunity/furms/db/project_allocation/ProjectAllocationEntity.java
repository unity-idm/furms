/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("project_allocation")
class ProjectAllocationEntity extends UUIDIdentifiable {

	public final UUID projectId;
	public final UUID communityAllocationId;
	public final String name;
	public final BigDecimal amount;
	public final LocalDateTime creationTime;

	ProjectAllocationEntity(UUID id, UUID projectId, UUID communityAllocationId, String name, BigDecimal amount, LocalDateTime creationTime) {
		this.id = id;
		this.projectId = projectId;
		this.communityAllocationId = communityAllocationId;
		this.name = name;
		this.amount = amount;
		this.creationTime = creationTime;
	}

	ProjectAllocation toProjectAllocation() {
		return ProjectAllocation.builder()
			.id(id.toString())
			.projectId(projectId.toString())
			.communityAllocationId(communityAllocationId.toString())
			.name(name)
			.amount(amount)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationEntity that = (ProjectAllocationEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(communityAllocationId, that.communityAllocationId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(creationTime, that.creationTime) &&
			Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, communityAllocationId, name, amount, creationTime);
	}

	@Override
	public String toString() {
		return "ProjectAllocationEntity{" +
			"id=" + id +
			", projectId=" + projectId +
			", communityAllocationId=" + communityAllocationId +
			", name='" + name + '\'' +
			", amount=" + amount +
			", creationTime=" + creationTime +
			'}';
	}

	public static CommunityAllocationEntityBuilder builder() {
		return new CommunityAllocationEntityBuilder();
	}

	public static final class CommunityAllocationEntityBuilder {
		private UUID id;
		private UUID projectId;
		private UUID communityAllocationId;
		private String name;
		private BigDecimal amount;
		private LocalDateTime creationTime;

		private CommunityAllocationEntityBuilder() {
		}

		public CommunityAllocationEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public CommunityAllocationEntityBuilder projectId(UUID projectId) {
			this.projectId = projectId;
			return this;
		}

		public CommunityAllocationEntityBuilder communityAllocationId(UUID communityAllocationId) {
			this.communityAllocationId = communityAllocationId;
			return this;
		}

		public CommunityAllocationEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public CommunityAllocationEntityBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public CommunityAllocationEntityBuilder creationTime(LocalDateTime creationTime) {
			this.creationTime = creationTime;
			return this;
		}

		public ProjectAllocationEntity build() {
			return new ProjectAllocationEntity(id, projectId, communityAllocationId, name, amount, creationTime);
		}
	}
}
