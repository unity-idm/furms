/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

public class UserAddition {
	public final String id;
	public final SiteId siteId;
	public final String projectId;
	public final CorrelationId correlationId;
	public final String userId;
	public final UserAdditionStatus status;

	UserAddition(String id, SiteId siteId, String projectId, CorrelationId correlationId, String userId, UserAdditionStatus status) {
		this.id = id;
		this.siteId = siteId;
		this.projectId = projectId;
		this.correlationId = correlationId;
		this.userId = userId;
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAddition that = (UserAddition) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, projectId, correlationId, userId, status);
	}

	@Override
	public String toString() {
		return "UserAddition{" +
			"id='" + id + '\'' +
			", siteId='" + siteId + '\'' +
			", projectId='" + projectId + '\'' +
			", correlationId='" + correlationId + '\'' +
			", userId='" + userId + '\'' +
			", status=" + status +
			'}';
	}

	public static UserAdditionBuilder builder() {
		return new UserAdditionBuilder();
	}

	public static final class UserAdditionBuilder {
		public String id;
		public SiteId siteId;
		public String projectId;
		public CorrelationId correlationId;
		public String userId;
		public UserAdditionStatus status;

		private UserAdditionBuilder() {
		}

		public UserAdditionBuilder id(String id) {
			this.id = id;
			return this;
		}

		public UserAdditionBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public UserAdditionBuilder correlationId(CorrelationId correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public UserAdditionBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public UserAdditionBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public UserAdditionBuilder status(UserAdditionStatus status) {
			this.status = status;
			return this;
		}

		public UserAddition build() {
			return new UserAddition(id, siteId, projectId, correlationId, userId, status);
		}
	}
}
