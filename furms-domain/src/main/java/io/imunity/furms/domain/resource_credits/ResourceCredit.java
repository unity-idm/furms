/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credits;

import static java.time.Clock.systemUTC;
import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ResourceCredit {
	public final String id;
	public final String name;
	public final String siteId;
	public final String resourceTypeId;
	public final boolean splittable;
	public final BigDecimal amount;
	public final LocalDateTime utcCreateTime;
	public final LocalDateTime utcStartTime;
	public final LocalDateTime utcEndTime;

	private ResourceCredit(String id,
			String name,
			String siteId,
			String resourceTypeId,
			boolean splittable,
			BigDecimal amount,
			LocalDateTime utcCreateTime,
			LocalDateTime utcStartTime,
			LocalDateTime utcEndTime) {
		this.id = id;
		this.name = name;
		this.siteId = siteId;
		this.resourceTypeId = resourceTypeId;
		this.splittable = splittable;
		this.amount = amount;
		this.utcCreateTime = ofNullable(utcCreateTime).orElseGet(() -> LocalDateTime.now(systemUTC()));
		this.utcStartTime = utcStartTime;
		this.utcEndTime = utcEndTime;
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, id, name, resourceTypeId, siteId,
				splittable, utcCreateTime, utcEndTime, utcStartTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceCredit other = (ResourceCredit) obj;
		return Objects.equals(amount, other.amount) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name)
				&& Objects.equals(resourceTypeId, other.resourceTypeId)
				&& Objects.equals(siteId, other.siteId) && splittable == other.splittable
				&& Objects.equals(utcCreateTime, other.utcCreateTime)
				&& Objects.equals(utcEndTime, other.utcEndTime)
				&& Objects.equals(utcStartTime, other.utcStartTime);
	}

	@Override
	public String toString() {
		return String.format(
				"ResourceCredit [id=%s, name=%s, siteId=%s, resourceTypeId=%s, splittable=%s, "
				+ "amount=%s, utcCreateTime=%s, utcStartTime=%s, utcEndTime=%s]",
				id, name, siteId, resourceTypeId, splittable, amount,
				utcCreateTime, utcStartTime, utcEndTime);
	}

	public static ResourceCreditBuilder builder() {
		return new ResourceCreditBuilder();
	}

	public static final class ResourceCreditBuilder {
		private String id;
		private String name;
		private String siteId;
		private String resourceTypeId;
		private boolean splittable = true;
		private BigDecimal amount;
		private LocalDateTime utcCreateTime;
		private LocalDateTime utcStartTime;
		private LocalDateTime utcEndTime;

		private ResourceCreditBuilder() {
		}

		public ResourceCreditBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ResourceCreditBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceCreditBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ResourceCreditBuilder resourceTypeId(String resourceTypeId) {
			this.resourceTypeId = resourceTypeId;
			return this;
		}

		public ResourceCreditBuilder splittable(boolean splittable) {
			this.splittable = splittable;
			return this;
		}

		public ResourceCreditBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ResourceCreditBuilder utcCreateTime(LocalDateTime createTime) {
			this.utcCreateTime = createTime;
			return this;
		}

		public ResourceCreditBuilder utcStartTime(LocalDateTime startTime) {
			this.utcStartTime = startTime;
			return this;
		}

		public ResourceCreditBuilder utcEndTime(LocalDateTime endTime) {
			this.utcEndTime = endTime;
			return this;
		}

		public ResourceCredit build() {
			return new ResourceCredit(id, name, siteId, resourceTypeId, splittable, amount, 
					utcCreateTime, utcStartTime, utcEndTime);
		}
	}
}
