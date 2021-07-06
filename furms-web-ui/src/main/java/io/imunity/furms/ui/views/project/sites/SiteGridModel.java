/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.sites;

import java.util.Objects;

class SiteGridModel {
	public final String siteId;
	public final String siteName;
	public final String status;
	public final String message;

	SiteGridModel(String siteId, String siteName, String status, String message) {
		this.siteId = siteId;
		this.siteName = siteName;
		this.status = status;
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteGridModel that = (SiteGridModel) o;
		return Objects.equals(siteId, that.siteId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId);
	}

	@Override
	public String toString() {
		return "SiteGridModel{" +
			"siteId='" + siteId + '\'' +
			", siteName='" + siteName + '\'' +
			", status='" + status + '\'' +
			", message='" + message + '\'' +
			'}';
	}
}
