/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credit_allocation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocation;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Table("resource_credit_allocation")
class ResourceCreditAllocationEntity extends UUIDIdentifiable {

	public final UUID siteId;
	public final UUID communityId;
	public final UUID resourceTypeId;
	public final UUID resourceCreditId;
	public final String name;
	public final BigDecimal amount;

	ResourceCreditAllocationEntity(UUID id, UUID siteId, UUID communityId, UUID resourceTypeId, UUID resourceCreditId,
	                               String name, BigDecimal amount) {
		this.id = id;
		this.siteId = siteId;
		this.communityId = communityId;
		this.resourceTypeId = resourceTypeId;
		this.resourceCreditId = resourceCreditId;
		this.name = name;
		this.amount = amount;
	}

	ResourceCreditAllocation toResourceCreditAllocation() {
		return ResourceCreditAllocation.builder()
			.id(id.toString())
			.siteId(siteId.toString())
			.communityId(communityId.toString())
			.resourceTypeId(resourceTypeId.toString())
			.resourceCreditId(resourceCreditId.toString())
			.name(name)
			.amount(amount)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCreditAllocationEntity that = (ResourceCreditAllocationEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(resourceTypeId, that.resourceTypeId) &&
			Objects.equals(resourceCreditId, that.resourceCreditId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, resourceTypeId, resourceCreditId, name, amount);
	}

	@Override
	public String toString() {
		return "ResourceCreditAllocationEntity{" +
			"id=" + id +
			", siteId=" + siteId +
			", resourceTypeId=" + resourceTypeId +
			", resourceCreditId=" + resourceCreditId +
			", name='" + name + '\'' +
			", amount=" + amount +
			'}';
	}

	public static ResourceCreditEntityBuilder builder() {
		return new ResourceCreditEntityBuilder();
	}

	public static final class ResourceCreditEntityBuilder {
		protected UUID id;
		public UUID siteId;
		public UUID communityId;
		public UUID resourceTypeId;
		public UUID resourceCreditId;
		public String name;
		public BigDecimal amount;

		private ResourceCreditEntityBuilder() {
		}

		public ResourceCreditEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public ResourceCreditEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public ResourceCreditEntityBuilder communityId(UUID communityId) {
			this.communityId = communityId;
			return this;
		}

		public ResourceCreditEntityBuilder resourceCreditId(UUID resourceCreditId) {
			this.resourceCreditId = resourceCreditId;
			return this;
		}

		public ResourceCreditEntityBuilder resourceTypeId(UUID resourceTypeId) {
			this.resourceTypeId = resourceTypeId;
			return this;
		}

		public ResourceCreditEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceCreditEntityBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ResourceCreditAllocationEntity build() {
			return new ResourceCreditAllocationEntity(id, siteId, communityId, resourceTypeId, resourceCreditId, name, amount);
		}
	}
}
