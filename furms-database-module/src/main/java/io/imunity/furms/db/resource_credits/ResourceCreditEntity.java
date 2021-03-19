/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credits;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("resource_credit")
class ResourceCreditEntity extends UUIDIdentifiable {

	public final UUID siteId;
	public final UUID resourceTypeId;
	public final String name;
	public final Boolean split;
	public final Boolean access;
	public final BigDecimal amount;
	public final LocalDateTime createTime;
	public final LocalDateTime startTime;
	public final LocalDateTime endTime;

	ResourceCreditEntity(UUID id, UUID siteId, UUID resourceTypeId, String name, Boolean split, Boolean access, BigDecimal amount, LocalDateTime createTime, LocalDateTime startTime, LocalDateTime endTime) {
		this.id = id;
		this.siteId = siteId;
		this.resourceTypeId = resourceTypeId;
		this.name = name;
		this.split = split;
		this.access = access;
		this.amount = amount;
		this.createTime = createTime;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public ResourceCredit toResourceCredit() {
		return ResourceCredit.builder()
			.id(id.toString())
			.siteId(siteId.toString())
			.resourceTypeId(resourceTypeId.toString())
			.name(name)
			.split(split)
			.access(access)
			.amount(amount)
			.utcCreateTime(createTime)
			.utcStartTime(startTime)
			.utcEndTime(endTime)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCreditEntity that = (ResourceCreditEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(resourceTypeId, that.resourceTypeId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(split, that.split) &&
			Objects.equals(access, that.access) &&
			Objects.equals(amount, that.amount) &&
			Objects.equals(createTime, that.createTime) &&
			Objects.equals(startTime, that.startTime) &&
			Objects.equals(endTime, that.endTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, resourceTypeId, name, split, access, amount, createTime, startTime, endTime);
	}

	@Override
	public String toString() {
		return "ResourceCreditEntity{" +
			"siteId=" + siteId +
			", resourceTypeId=" + resourceTypeId +
			", name='" + name + '\'' +
			", split=" + split +
			", access=" + access +
			", amount=" + amount +
			", createTime=" + createTime +
			", startTime=" + startTime +
			", endTime=" + endTime +
			", id=" + id +
			'}';
	}

	public static ResourceCreditEntityBuilder builder() {
		return new ResourceCreditEntityBuilder();
	}

	public static final class ResourceCreditEntityBuilder {
		public UUID siteId;
		public UUID resourceTypeId;
		public String name;
		public Boolean split;
		public Boolean access;
		public BigDecimal amount;
		public LocalDateTime createTime;
		public LocalDateTime startTime;
		public LocalDateTime endTime;
		protected UUID id;

		private ResourceCreditEntityBuilder() {
		}

		public ResourceCreditEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
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

		public ResourceCreditEntityBuilder split(Boolean split) {
			this.split = split;
			return this;
		}

		public ResourceCreditEntityBuilder access(Boolean access) {
			this.access = access;
			return this;
		}

		public ResourceCreditEntityBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ResourceCreditEntityBuilder createTime(LocalDateTime createTime) {
			this.createTime = createTime;
			return this;
		}

		public ResourceCreditEntityBuilder startTime(LocalDateTime startTime) {
			this.startTime = startTime;
			return this;
		}

		public ResourceCreditEntityBuilder endTime(LocalDateTime endTime) {
			this.endTime = endTime;
			return this;
		}

		public ResourceCreditEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public ResourceCreditEntity build() {
			return new ResourceCreditEntity(id, siteId, resourceTypeId, name, split, access, amount, createTime, startTime, endTime);
		}
	}
}
