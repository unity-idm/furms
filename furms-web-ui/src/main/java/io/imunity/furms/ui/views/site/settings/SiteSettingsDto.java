/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.settings;

import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

public class SiteSettingsDto implements Cloneable {

	private SiteId id;
	private String name;
	private FurmsImage logo;
	private String oauthClientId;
	private String connectionInfo;
	private Boolean sshKeyFromOptionMandatory;
	private Boolean prohibitOldsshKeys;
	private SiteExternalId externalId;
	private PolicyId policyId;

	public SiteSettingsDto(Site site) {
		this.id = site.getId();
		this.name = site.getName();
		this.logo = site.getLogo();
		this.oauthClientId = site.getOauthClientId();
		this.connectionInfo = site.getConnectionInfo();
		this.sshKeyFromOptionMandatory = site.isSshKeyFromOptionMandatory();
		this.externalId = site.getExternalId();
		this.prohibitOldsshKeys = site.getSshKeyHistoryLength() != null && site.getSshKeyHistoryLength() > 0;
		this.policyId = site.getPolicyId();
	}

	SiteSettingsDto(SiteId id, String name, FurmsImage logo, String oauthClientId, String connectionInfo,
			Boolean sshKeyFromOptionMandatory, Boolean prohibitOldsshKeys, SiteExternalId externalId, PolicyId policyId) {
		this.id = id;
		this.name = name;
		this.logo = logo;
		this.oauthClientId = oauthClientId;
		this.connectionInfo = connectionInfo;
		this.sshKeyFromOptionMandatory = sshKeyFromOptionMandatory;
		this.prohibitOldsshKeys = prohibitOldsshKeys;
		this.externalId = externalId;
		this.policyId = policyId;
	}

	public SiteId getId() {
		return id;
	}

	public void setId(SiteId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FurmsImage getLogo() {
		return logo;
	}

	public void setLogo(FurmsImage logo) {
		this.logo = logo;
	}

	public String getOauthClientId() {
		return oauthClientId;
	}

	public void setOauthClientId(String oauthClientId) {
		this.oauthClientId = oauthClientId;
	}

	public String getConnectionInfo() {
		return connectionInfo;
	}

	public void setConnectionInfo(String connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	public SiteExternalId getExternalId() {
		return externalId;
	}

	public void setExternalId(SiteExternalId externalId) {
		this.externalId = externalId;
	}
	
	public Boolean isSshKeyFromOptionMandatory() {
		return sshKeyFromOptionMandatory;
	}

	public void setSshKeyFromOptionMandatory(Boolean sshKeyFromOptionMandatory) {
		this.sshKeyFromOptionMandatory = sshKeyFromOptionMandatory;
	}
	
	public Boolean isProhibitOldsshKeys() {
		return prohibitOldsshKeys;
	}

	public void setProhibitOldsshKeys(Boolean prohibitOldsshKeys) {
		this.prohibitOldsshKeys = prohibitOldsshKeys;
	}

	public PolicyId getPolicyId() {
		return policyId;
	}

	public void setPolicyId(PolicyId policyId) {
		this.policyId = policyId;
	}

	@Override
	public SiteSettingsDto clone() {
		return new SiteSettingsDto(this.id, this.name, this.logo, this.oauthClientId, this.connectionInfo,
				this.sshKeyFromOptionMandatory, this.prohibitOldsshKeys, new SiteExternalId(externalId.id),
				new PolicyId(this.policyId.id));
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteSettingsDto that = (SiteSettingsDto) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "SiteSettingsDto{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", oauthClientId='" + oauthClientId + '\'' +
				", logo=" + logo +
				", sshKeyFromOptionMandatory=" + sshKeyFromOptionMandatory +
				", prohibitOldsshKeys=" + prohibitOldsshKeys +
				", externalId=" + externalId +
				", policyId=" + policyId +
				'}';
	}

	
}
