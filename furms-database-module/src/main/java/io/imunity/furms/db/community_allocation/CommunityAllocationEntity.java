/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.community_allocation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("community_allocation")
public class CommunityAllocationEntity extends UUIDIdentifiable {

	public final UUID communityId;
	public final UUID resourceCreditId;
	public final String name;
	public final BigDecimal amount;
	public final LocalDateTime creationTime;

	CommunityAllocationEntity(UUID id, UUID communityId, UUID resourceCreditId,
	                          String name, BigDecimal amount, LocalDateTime creationTime) {
		this.id = id;
		this.communityId = communityId;
		this.resourceCreditId = resourceCreditId;
		this.name = name;
		this.amount = amount;
		this.creationTime = creationTime;
	}

	public CommunityAllocation toCommunityAllocation() {
		return CommunityAllocation.builder()
			.id(id.toString())
			.communityId(communityId.toString())
			.resourceCreditId(resourceCreditId.toString())
			.name(name)
			.amount(amount)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocationEntity that = (CommunityAllocationEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(resourceCreditId, that.resourceCreditId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(creationTime, that.creationTime) &&
			Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, resourceCreditId, name, creationTime, amount);
	}

	@Override
	public String toString() {
		return "CommunityAllocationEntity{" +
			"id=" + id +
			", resourceCreditId=" + resourceCreditId +
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
		private UUID communityId;
		private UUID resourceCreditId;
		private String name;
		private BigDecimal amount;
		private LocalDateTime creationTime;

		private CommunityAllocationEntityBuilder() {
		}

		public CommunityAllocationEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public CommunityAllocationEntityBuilder communityId(UUID communityId) {
			this.communityId = communityId;
			return this;
		}

		public CommunityAllocationEntityBuilder resourceCreditId(UUID resourceCreditId) {
			this.resourceCreditId = resourceCreditId;
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

		public CommunityAllocationEntity build() {
			return new CommunityAllocationEntity(id, communityId, resourceCreditId, name, amount, creationTime);
		}
	}
}
