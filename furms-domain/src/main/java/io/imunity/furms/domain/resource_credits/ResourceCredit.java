/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credits;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static java.util.Optional.ofNullable;

public class ResourceCredit {
	public final String id;
	public final String name;
	public final String siteId;
	public final String resourceTypeId;
	public final Boolean split;
	public final Boolean access;
	public final BigDecimal amount;
	public final LocalDateTime createTime;
	public final LocalDateTime startTime;
	public final LocalDateTime endTime;

	private ResourceCredit(String id, String name, String siteId, String resourceTypeId, Boolean split, Boolean access, BigDecimal amount, LocalDateTime createTime, LocalDateTime startTime, LocalDateTime endTime) {
		this.id = id;
		this.name = name;
		this.siteId = siteId;
		this.resourceTypeId = resourceTypeId;
		this.split = ofNullable(split).orElse(true);
		this.access = ofNullable(access).orElse(false);
		this.amount = amount;
		this.createTime = ofNullable(createTime).orElseGet(LocalDateTime::now);
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCredit that = (ResourceCredit) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(name, that.name) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(resourceTypeId, that.resourceTypeId) &&
			Objects.equals(split, that.split) &&
			Objects.equals(access, that.access) &&
			Objects.equals(amount, that.amount) &&
			Objects.equals(createTime, that.createTime) &&
			Objects.equals(startTime, that.startTime) &&
			Objects.equals(endTime, that.endTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, siteId, resourceTypeId, split, access, amount, createTime, startTime, endTime);
	}

	@Override
	public String toString() {
		return "ResourceCredit{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", siteId='" + siteId + '\'' +
			", resourceTypeId='" + resourceTypeId + '\'' +
			", split=" + split +
			", access=" + access +
			", amount=" + amount +
			", createTime=" + createTime +
			", startTime=" + startTime +
			", endTime=" + endTime +
			'}';
	}

	public static ResourceCreditBuilder builder() {
		return new ResourceCreditBuilder();
	}

	public static final class ResourceCreditBuilder {
		public String id;
		public String name;
		public String siteId;
		public String resourceTypeId;
		public Boolean split;
		public Boolean access;
		public BigDecimal amount;
		public LocalDateTime createTime;
		public LocalDateTime startTime;
		public LocalDateTime endTime;

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

		public ResourceCreditBuilder split(Boolean split) {
			this.split = split;
			return this;
		}

		public ResourceCreditBuilder access(Boolean access) {
			this.access = access;
			return this;
		}

		public ResourceCreditBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ResourceCreditBuilder createTime(LocalDateTime createTime) {
			this.createTime = createTime;
			return this;
		}

		public ResourceCreditBuilder startTime(LocalDateTime startTime) {
			this.startTime = startTime;
			return this;
		}

		public ResourceCreditBuilder endTime(LocalDateTime endTime) {
			this.endTime = endTime;
			return this;
		}

		public ResourceCredit build() {
			return new ResourceCredit(id, name, siteId, resourceTypeId, split, access, amount, createTime, startTime, endTime);
		}
	}
}
