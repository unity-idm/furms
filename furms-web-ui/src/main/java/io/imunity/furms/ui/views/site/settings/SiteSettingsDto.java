/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.settings;

import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.sites.Site;

import java.util.Objects;

public class SiteSettingsDto implements Cloneable {

	private String id;
	private String name;
	private FurmsImage logo;
	private String connectionInfo;

	public SiteSettingsDto(Site site) {
		this.id = site.getId();
		this.name = site.getName();
		this.logo = site.getLogo();
		this.connectionInfo = site.getConnectionInfo();
	}

	SiteSettingsDto(String id, String name, FurmsImage logo, String connectionInfo) {
		this.id = id;
		this.name = name;
		this.logo = logo;
		this.connectionInfo = connectionInfo;
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

	@Override
	public SiteSettingsDto clone() {
		return new SiteSettingsDto(this.id, this.name, this.logo, this.connectionInfo);
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
				'}';
	}
}
