/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import java.math.BigDecimal;
import java.util.Objects;

class CommunityAllocationViewModel {
	public String id;
	public String communityId;
	public SiteComboBoxModel site;
	public ResourceTypeComboBoxModel resourceType;
	public ResourceCreditComboBoxModel resourceCredit;
	public String name;
	public BigDecimal amount;

	CommunityAllocationViewModel(String id, String communityId, SiteComboBoxModel site,
	                             ResourceTypeComboBoxModel resourceType,
	                             ResourceCreditComboBoxModel resourceCredit, String name, BigDecimal amount) {
		this.id = id;
		this.communityId = communityId;
		this.site = site;
		this.resourceType = resourceType;
		this.resourceCredit = resourceCredit;
		this.name = name;
		this.amount = amount;
	}

	CommunityAllocationViewModel(String communityId) {
		this.communityId = communityId;
	}

	String getCommunityId() {
		return communityId;
	}

	void setCommunityId(String communityId) {
		this.communityId = communityId;
	}

	SiteComboBoxModel getSite() {
		return site;
	}

	void setSite(SiteComboBoxModel site) {
		this.site = site;
	}

	ResourceTypeComboBoxModel getResourceType() {
		return resourceType;
	}

	void setResourceType(ResourceTypeComboBoxModel resourceType) {
		this.resourceType = resourceType;
	}

	ResourceCreditComboBoxModel getResourceCredit() {
		return resourceCredit;
	}

	void setResourceCredit(ResourceCreditComboBoxModel resourceCredit) {
		this.resourceCredit = resourceCredit;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	BigDecimal getAmount() {
		return amount;
	}

	void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocationViewModel that = (CommunityAllocationViewModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "CommunityAllocationViewModel{" +
			"id='" + id + '\'' +
			", communityId='" + communityId + '\'' +
			", site='" + site + '\'' +
			", resourceTypeId='" + resourceType + '\'' +
			", resourceCreditId='" + resourceCredit + '\'' +
			", name='" + name + '\'' +
			", amount=" + amount +
			'}';
	}

	public CommunityAllocationViewModel() {}

	public static CommunityAllocationViewModelBuilder builder() {
		return new CommunityAllocationViewModelBuilder();
	}

	public static final class CommunityAllocationViewModelBuilder {
		public String id;
		public String communityId;
		public SiteComboBoxModel site;
		public ResourceTypeComboBoxModel resourceType;
		public ResourceCreditComboBoxModel resourceCredit;
		public String name;
		public BigDecimal amount;

		private CommunityAllocationViewModelBuilder() {
		}

		public CommunityAllocationViewModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public CommunityAllocationViewModelBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public CommunityAllocationViewModelBuilder site(SiteComboBoxModel site) {
			this.site = site;
			return this;
		}

		public CommunityAllocationViewModelBuilder resourceType(ResourceTypeComboBoxModel resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public CommunityAllocationViewModelBuilder resourceCredit(ResourceCreditComboBoxModel resourceCredit) {
			this.resourceCredit = resourceCredit;
			return this;
		}

		public CommunityAllocationViewModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public CommunityAllocationViewModelBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public CommunityAllocationViewModel build() {
			return new CommunityAllocationViewModel(id, communityId, site, resourceType, resourceCredit, name, amount);
		}
	}
}
