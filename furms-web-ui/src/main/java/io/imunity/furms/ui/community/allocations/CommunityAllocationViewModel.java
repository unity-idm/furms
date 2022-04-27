/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.community.allocations;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.ui.components.support.models.SiteComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceCreditComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceTypeComboBoxModel;

import java.math.BigDecimal;
import java.util.Objects;

public class CommunityAllocationViewModel {
	private CommunityAllocationId id;
	private CommunityId communityId;
	private final String communityName;
	private SiteComboBoxModel site;
	private ResourceTypeComboBoxModel resourceType;
	private ResourceCreditComboBoxModel resourceCredit;
	private String name;
	private BigDecimal amount;

	public CommunityAllocationViewModel(CommunityId communityId, String communityName) {
		this.communityId = communityId;
		this.communityName = communityName;
	}

	public CommunityAllocationViewModel(CommunityAllocationId id,
	                                    CommunityId communityId,
	                                    String communityName,
	                                    SiteComboBoxModel site,
	                                    ResourceTypeComboBoxModel resourceType,
	                                    ResourceCreditComboBoxModel resourceCredit,
	                                    String name,
	                                    BigDecimal amount) {
		this.id = id;
		this.communityId = communityId;
		this.communityName = communityName;
		this.site = site;
		this.resourceType = resourceType;
		this.resourceCredit = resourceCredit;
		this.name = name;
		this.amount = amount;
	}

	public CommunityAllocationId getId() {
		return id;
	}

	public CommunityId getCommunityId() {
		return communityId;
	}

	public void setCommunityId(CommunityId communityId) {
		this.communityId = communityId;
	}

	public String getCommunityName() {
		return communityName;
	}

	public SiteComboBoxModel getSite() {
		return site;
	}

	public void setSite(SiteComboBoxModel site) {
		this.site = site;
	}

	public ResourceTypeComboBoxModel getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceTypeComboBoxModel resourceType) {
		this.resourceType = resourceType;
	}

	public ResourceCreditComboBoxModel getResourceCredit() {
		return resourceCredit;
	}

	public void setResourceCredit(ResourceCreditComboBoxModel resourceCredit) {
		this.resourceCredit = resourceCredit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
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
			", communityName='" + communityName + '\'' +
			", site='" + site + '\'' +
			", resourceTypeId='" + resourceType + '\'' +
			", resourceCreditId='" + resourceCredit + '\'' +
			", name='" + name + '\'' +
			", amount=" + amount +
			'}';
	}

	public static CommunityAllocationViewModelBuilder builder() {
		return new CommunityAllocationViewModelBuilder();
	}

	public static final class CommunityAllocationViewModelBuilder {
		private CommunityAllocationId id;
		private CommunityId communityId;
		private String communityName;
		private SiteComboBoxModel site;
		private ResourceTypeComboBoxModel resourceType;
		private ResourceCreditComboBoxModel resourceCredit;
		private String name;
		private BigDecimal amount;

		private CommunityAllocationViewModelBuilder() {
		}

		public CommunityAllocationViewModelBuilder id(CommunityAllocationId id) {
			this.id = id;
			return this;
		}

		public CommunityAllocationViewModelBuilder communityId(CommunityId communityId) {
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

		public CommunityAllocationViewModelBuilder communityName(String communityName) {
			this.communityName = communityName;
			return this;
		}

		public CommunityAllocationViewModel build() {
			return new CommunityAllocationViewModel(id, communityId, communityName, site, resourceType, resourceCredit, name, amount);
		}
	}
}
