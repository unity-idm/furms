/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import java.util.Objects;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;

public class UserRemoval {
	public final String id;
	public final SiteId siteId;
	public final String projectId;
	public final String userAdditionId;
	public final CorrelationId correlationId;
	public final String userId;
	public final UserRemovalStatus status;

	UserRemoval(String id, SiteId siteId, String projectId, String userAdditionId, CorrelationId correlationId, String userId, UserRemovalStatus status) {
		this.id = id;
		this.siteId = siteId;
		this.projectId = projectId;
		this.userAdditionId = userAdditionId;
		this.correlationId = correlationId;
		this.userId = userId;
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserRemoval that = (UserRemoval) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(userAdditionId, that.userAdditionId) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, projectId, correlationId, userId, status, userAdditionId);
	}

	@Override
	public String toString() {
		return "UserRemoval{" +
			"id='" + id + '\'' +
			", siteId='" + siteId + '\'' +
			", projectId='" + projectId + '\'' +
			", userAdditionId='" + userAdditionId + '\'' +
			", correlationId='" + correlationId + '\'' +
			", userId='" + userId + '\'' +
			", status=" + status +
			'}';
	}

	public static UserRemovalBuilder builder() {
		return new UserRemovalBuilder();
	}

	public static final class UserRemovalBuilder {
		private String id;
		private SiteId siteId;
		private String projectId;
		private String userAdditionId;
		private CorrelationId correlationId;
		private String userId;
		private UserRemovalStatus status;

		private UserRemovalBuilder() {
		}

		public UserRemovalBuilder id(String id) {
			this.id = id;
			return this;
		}

		public UserRemovalBuilder userAdditionId(String userAdditionId) {
			this.userAdditionId = userAdditionId;
			return this;
		}

		public UserRemovalBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public UserRemovalBuilder correlationId(CorrelationId correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public UserRemovalBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public UserRemovalBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public UserRemovalBuilder status(UserRemovalStatus status) {
			this.status = status;
			return this;
		}

		public UserRemoval build() {
			return new UserRemoval(id, siteId, projectId, userAdditionId, correlationId, userId, status);
		}
	}
}
