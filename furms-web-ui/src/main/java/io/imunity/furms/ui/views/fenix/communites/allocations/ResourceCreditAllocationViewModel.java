/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import java.math.BigDecimal;
import java.util.Objects;

class ResourceCreditAllocationViewModel {
	public String id;
	public String communityId;
	public SiteComboBoxModel site;
	public ResourceTypeComboBoxModel resourceType;
	public ResourceCreditComboBoxModel resourceCredit;
	public String name;
	public BigDecimal amount;

	ResourceCreditAllocationViewModel(String id, String communityId, SiteComboBoxModel site,
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

	ResourceCreditAllocationViewModel(String communityId) {
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
		ResourceCreditAllocationViewModel that = (ResourceCreditAllocationViewModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ResourceCreditAllocationViewModel{" +
			"id='" + id + '\'' +
			", communityId='" + communityId + '\'' +
			", site='" + site + '\'' +
			", resourceTypeId='" + resourceType + '\'' +
			", resourceCreditId='" + resourceCredit + '\'' +
			", name='" + name + '\'' +
			", amount=" + amount +
			'}';
	}

	public ResourceCreditAllocationViewModel() {}

	public static ResourceCreditAllocationViewModelBuilder builder() {
		return new ResourceCreditAllocationViewModelBuilder();
	}

	public static final class ResourceCreditAllocationViewModelBuilder {
		public String id;
		public String communityId;
		public SiteComboBoxModel site;
		public ResourceTypeComboBoxModel resourceType;
		public ResourceCreditComboBoxModel resourceCredit;
		public String name;
		public BigDecimal amount;

		private ResourceCreditAllocationViewModelBuilder() {
		}

		public ResourceCreditAllocationViewModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ResourceCreditAllocationViewModelBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public ResourceCreditAllocationViewModelBuilder site(SiteComboBoxModel site) {
			this.site = site;
			return this;
		}

		public ResourceCreditAllocationViewModelBuilder resourceType(ResourceTypeComboBoxModel resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public ResourceCreditAllocationViewModelBuilder resourceCredit(ResourceCreditComboBoxModel resourceCredit) {
			this.resourceCredit = resourceCredit;
			return this;
		}

		public ResourceCreditAllocationViewModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceCreditAllocationViewModelBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ResourceCreditAllocationViewModel build() {
			return new ResourceCreditAllocationViewModel(id, communityId, site, resourceType, resourceCredit, name, amount);
		}
	}
}
