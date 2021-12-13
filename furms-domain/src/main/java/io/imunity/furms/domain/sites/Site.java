/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.policy_documents.PolicyId;

import java.util.Objects;

public class Site {

	private final String id;
	private final String name;
	private final String oauthClientId;
	private final String connectionInfo;
	private final FurmsImage logo;
	private final Boolean sshKeyFromOptionMandatory;
	private final SiteExternalId externalId;
	private final Integer sshKeyHistoryLength;
	private final PolicyId policyId;

	private Site(String id, String name, String oauthClientId, String connectionInfo, FurmsImage logo,
	             Boolean sshKeyFromOptionMandatory, Integer sshKeyHistoryLength, SiteExternalId externalId,
	             PolicyId policyId) {
		this.id = id;
		this.name = name;
		this.oauthClientId = oauthClientId;
		this.connectionInfo = connectionInfo;
		this.logo = logo;
		this.sshKeyFromOptionMandatory = sshKeyFromOptionMandatory;
		this.sshKeyHistoryLength = sshKeyHistoryLength;
		this.externalId = externalId;
		this.policyId = policyId;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getOauthClientId() {
		return oauthClientId;
	}

	public String getConnectionInfo() {
		return connectionInfo;
	}

	public FurmsImage getLogo() {
		return logo;
	}
	
	public Boolean isSshKeyFromOptionMandatory()
	{
		return sshKeyFromOptionMandatory;
	}

	public SiteExternalId getExternalId() {
		return externalId;
	}
	
	public Integer getSshKeyHistoryLength() {
		return sshKeyHistoryLength;
	}
	
	public boolean isSshKeyHistoryActive(){
		return sshKeyHistoryLength != null && !sshKeyHistoryLength.equals(0);
	}

	public PolicyId getPolicyId() {
		return policyId;
	}

	public static SiteBuilder builder() {
		return new SiteBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Site site = (Site) o;
		return Objects.equals(id, site.id) &&
				Objects.equals(name, site.name) &&
				Objects.equals(oauthClientId, site.oauthClientId) &&
				Objects.equals(connectionInfo, site.connectionInfo) &&
				Objects.equals(logo, site.logo) &&
				Objects.equals(sshKeyFromOptionMandatory, site.sshKeyFromOptionMandatory) &&
				Objects.equals(sshKeyHistoryLength, site.sshKeyHistoryLength) &&
				Objects.equals(externalId, site.externalId) &&
				Objects.equals(policyId, site.policyId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, oauthClientId, connectionInfo, logo, sshKeyFromOptionMandatory, externalId,
				sshKeyHistoryLength, policyId);
	}

	@Override
	public String toString() {
		return "Site{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", oauthClientId='" + oauthClientId + '\'' +
				", connectionInfo='" + connectionInfo + '\'' +
				", sshKeyFromOptionMandatory=" + sshKeyFromOptionMandatory +
				", externalId=" + externalId +
				", sshKeyHistoryLength=" + sshKeyHistoryLength +
				", policyId=" + policyId +
				'}';
	}

	public static class SiteBuilder {
		private String id;
		private String name;
		private String oauthClientId;
		private String connectionInfo;
		private FurmsImage logo;
		private Boolean sshKeyFromOptionMandatory;
		private SiteExternalId externalId;
		private Integer sshKeyHistoryLength;
		private PolicyId policyId = PolicyId.empty();

		private SiteBuilder() {
		}

		public SiteBuilder id(String id) {
			this.id = id;
			return this;
		}

		public SiteBuilder oauthClientId(String oauthClientId) {
			this.oauthClientId = oauthClientId;
			return this;
		}

		public SiteBuilder policyId(PolicyId policyId) {
			this.policyId = policyId;
			return this;
		}

		public SiteBuilder name(String name) {
			this.name = name;
			return this;
		}

		public SiteBuilder connectionInfo(String connectionInfo) {
			this.connectionInfo = connectionInfo;
			return this;
		}

		public SiteBuilder logo(FurmsImage logo) {
			this.logo = logo;
			return this;
		}
		
		public SiteBuilder sshKeyFromOptionMandatory(Boolean sshKeyFromOptionMandatory) {
			this.sshKeyFromOptionMandatory = sshKeyFromOptionMandatory;
			return this;
		}

		public SiteBuilder externalId(SiteExternalId externalId) {
			this.externalId = externalId;
			return this;
		}
		
		public SiteBuilder sshKeyHistoryLength(Integer sshKeyHistoryLength) {
			this.sshKeyHistoryLength = sshKeyHistoryLength;
			return this;
		}

		public Site build() {
			return new Site(id, name, oauthClientId, connectionInfo, logo, sshKeyFromOptionMandatory,
					sshKeyHistoryLength, externalId, policyId);
		}

	}

}
