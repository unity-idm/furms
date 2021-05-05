/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credits;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static java.time.Clock.systemUTC;
import static java.util.Optional.ofNullable;

public class ResourceCredit {
	public final String id;
	public final String name;
	public final String siteId;
	public final String resourceTypeId;
	public final Boolean split;
	public final Boolean access;
	public final BigDecimal amount;
	public final LocalDateTime utcCreateTime;
	public final LocalDateTime utcStartTime;
	public final LocalDateTime utcEndTime;

	private ResourceCredit(String id, String name, String siteId, String resourceTypeId, Boolean split, Boolean access, BigDecimal amount, LocalDateTime utcCreateTime, LocalDateTime utcStartTime, LocalDateTime utcEndTime) {
		this.id = id;
		this.name = name;
		this.siteId = siteId;
		this.resourceTypeId = resourceTypeId;
		this.split = ofNullable(split).orElse(true);
		this.access = ofNullable(access).orElse(false);
		this.amount = amount;
		this.utcCreateTime = ofNullable(utcCreateTime).orElseGet(() -> LocalDateTime.now(systemUTC()));
		this.utcStartTime = utcStartTime;
		this.utcEndTime = utcEndTime;
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
			Objects.equals(utcCreateTime, that.utcCreateTime) &&
			Objects.equals(utcStartTime, that.utcStartTime) &&
			Objects.equals(utcEndTime, that.utcEndTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, siteId, resourceTypeId, split, access, amount, utcCreateTime, utcStartTime, utcEndTime);
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
			", createTime=" + utcCreateTime +
			", startTime=" + utcStartTime +
			", endTime=" + utcEndTime +
			'}';
	}

	public static ResourceCreditBuilder builder() {
		return new ResourceCreditBuilder();
	}

	public static final class ResourceCreditBuilder {
		private String id;
		private String name;
		private String siteId;
		private String resourceTypeId;
		private Boolean split;
		private Boolean access;
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
			return new ResourceCredit(id, name, siteId, resourceTypeId, split, access, amount, utcCreateTime, utcStartTime, utcEndTime);
		}
	}
}
