/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.settings;

import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;

import java.util.Objects;

public class SiteSettingsDto implements Cloneable {

	private String id;
	private String name;
	private FurmsImage logo;
	private String connectionInfo;
	private Boolean sshKeyFromOptionMandatory;
	private Integer sshKeyHistoryLength;
	private SiteExternalId externalId;

	public SiteSettingsDto(Site site) {
		this.id = site.getId();
		this.name = site.getName();
		this.logo = site.getLogo();
		this.connectionInfo = site.getConnectionInfo();
		this.sshKeyFromOptionMandatory = site.isSshKeyFromOptionMandatory();
		this.externalId = site.getExternalId();
		this.sshKeyHistoryLength = site.getSshKeyHistoryLength();
	}

	SiteSettingsDto(String id, String name, FurmsImage logo, String connectionInfo,
			Boolean sshKeyFromOptionMandatory, Integer sshKeyHistoryLength, SiteExternalId externalId) {
		this.id = id;
		this.name = name;
		this.logo = logo;
		this.connectionInfo = connectionInfo;
		this.sshKeyFromOptionMandatory = sshKeyFromOptionMandatory;
		this.sshKeyHistoryLength = sshKeyHistoryLength;
		this.externalId = externalId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
	
	public Integer getSshKeyHistoryLength() {
		return sshKeyHistoryLength;
	}

	public void setSshKeyHistoryLength(Integer sshKeyHistoryLength) {
		this.sshKeyHistoryLength = sshKeyHistoryLength;
	}

	@Override
	public SiteSettingsDto clone() {
		return new SiteSettingsDto(this.id, this.name, this.logo, this.connectionInfo,
				this.sshKeyFromOptionMandatory, this.sshKeyHistoryLength, new SiteExternalId(externalId.id));
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
				", logo=" + logo +
				", connectionInfo='" + connectionInfo + '\'' +
				", sshKeyFromOptionMandatory=" + sshKeyFromOptionMandatory +
				", sshKeyHistoryLength=" + sshKeyHistoryLength +
				", externalId=" + externalId +
				'}';
	}
}
