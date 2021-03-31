/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.community_allocation;

import java.math.BigDecimal;
import java.util.Objects;

public class CommunityAllocation {

	public final String id;
	public final String communityId;
	public final String resourceCreditId;
	public final String name;
	public final BigDecimal amount;

	private CommunityAllocation(String id, String communityId,
	                            String resourceCreditId, String name, BigDecimal amount) {
		this.id = id;
		this.communityId = communityId;
		this.resourceCreditId = resourceCreditId;
		this.name = name;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocation that = (CommunityAllocation) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(resourceCreditId, that.resourceCreditId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, communityId, resourceCreditId, name, amount);
	}

	@Override
	public String toString() {
		return "CommunityAllocation{" +
			"id=" + id +
			", communityId=" + communityId +
			", resourceCreditId=" + resourceCreditId +
			", name='" + name + '\'' +
			", amount='" + amount + '\'' +
			'}';
	}

	public static CommunityAllocationBuilder builder() {
		return new CommunityAllocationBuilder();
	}

	public static final class CommunityAllocationBuilder {
		protected String id;
		public String communityId;
		public String resourceCreditId;
		public String name;
		public BigDecimal amount;

		private CommunityAllocationBuilder() {
		}

		public CommunityAllocationBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public CommunityAllocationBuilder resourceCreditId(String resourceCreditId) {
			this.resourceCreditId = resourceCreditId;
			return this;
		}

		public CommunityAllocationBuilder name(String name) {
			this.name = name;
			return this;
		}

		public CommunityAllocationBuilder id(String id) {
			this.id = id;
			return this;
		}

		public CommunityAllocationBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public CommunityAllocation build() {
			return new CommunityAllocation(id, communityId, resourceCreditId, name, amount);
		}
	}
}
