/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.sites;

import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_site_access.UserSiteAccessStatusWithMessage;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

class SiteTreeGridModel {
	public final SiteId siteId;
	public final String siteName;
	public final String status;
	public final String message;
	public final FenixUserId userId;
	public final String userName;
	public final String userEmail;
	public final UserSiteAccessStatusWithMessage userAccessStatus;

	SiteTreeGridModel(SiteId siteId, String siteName, String status, String message, FenixUserId userId, String userName,
	                  String userEmail, UserSiteAccessStatusWithMessage userAccessStatus) {
		this.siteId = siteId;
		this.siteName = siteName;
		this.status = status;
		this.message = message;
		this.userId = userId;
		this.userName = userName;
		this.userEmail = userEmail;
		this.userAccessStatus = userAccessStatus;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteTreeGridModel that = (SiteTreeGridModel) o;
		return Objects.equals(siteId, that.siteId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, userId);
	}

	@Override
	public String toString() {
		return "SiteGridModel{" +
			"siteId='" + siteId + '\'' +
			", siteName='" + siteName + '\'' +
			", status='" + status + '\'' +
			", message='" + message + '\'' +
			", userId='" + userId + '\'' +
			", userName='" + userName + '\'' +
			", userEmail='" + userEmail + '\'' +
			", userAccessStatus='" + userAccessStatus + '\'' +
			'}';
	}

	public static SiteTreeGridModelBuilder builder() {
		return new SiteTreeGridModelBuilder();
	}

	public static final class SiteTreeGridModelBuilder {
		private SiteId siteId;
		private String siteName;
		private String status;
		private String message;
		private FenixUserId userId;
		private String userName;
		private String userEmail;
		private UserSiteAccessStatusWithMessage userAccessStatus;

		private SiteTreeGridModelBuilder() {
		}

		public SiteTreeGridModelBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public SiteTreeGridModelBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public SiteTreeGridModelBuilder status(String status) {
			this.status = status;
			return this;
		}

		public SiteTreeGridModelBuilder message(String message) {
			this.message = message;
			return this;
		}

		public SiteTreeGridModelBuilder userId(FenixUserId userId) {
			this.userId = userId;
			return this;
		}

		public SiteTreeGridModelBuilder userName(String userName) {
			this.userName = userName;
			return this;
		}

		public SiteTreeGridModelBuilder userEmail(String userEmail) {
			this.userEmail = userEmail;
			return this;
		}


		public SiteTreeGridModelBuilder userAccessStatus(UserSiteAccessStatusWithMessage userAccessStatus) {
			this.userAccessStatus = userAccessStatus;
			return this;
		}

		public SiteTreeGridModel build() {
			return new SiteTreeGridModel(siteId, siteName, status, message, userId, userName, userEmail, userAccessStatus);
		}
	}
}
