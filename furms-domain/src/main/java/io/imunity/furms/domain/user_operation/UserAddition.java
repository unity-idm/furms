/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;
import java.util.Optional;

public class UserAddition {
	public final UserAdditionId id;
	public final SiteId siteId;
	public final ProjectId projectId;
	public final CorrelationId correlationId;
	public final String uid;
	public final FenixUserId userId;
	public final UserStatus status;
	public final Optional<UserAdditionErrorMessage> errorMessage;

	UserAddition(UserAdditionId id, SiteId siteId, ProjectId projectId, CorrelationId correlationId, String uid,
	             FenixUserId userId,
	             UserStatus status, Optional<UserAdditionErrorMessage> errorMessage) {
		this.id = id;
		this.siteId = siteId;
		this.projectId = projectId;
		this.correlationId = correlationId;
		this.userId = userId;
		this.uid = uid;
		this.status = status;
		this.errorMessage = errorMessage;
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
			Objects.equals(uid, that.uid) &&
			Objects.equals(errorMessage, that.errorMessage) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, projectId, correlationId, uid, userId, status, errorMessage);
	}

	@Override
	public String toString() {
		return "UserAddition{" +
			"id='" + id + '\'' +
			", siteId='" + siteId + '\'' +
			", projectId='" + projectId + '\'' +
			", correlationId='" + correlationId + '\'' +
			", uid='" + uid + '\'' +
			", userId='" + userId + '\'' +
			", status=" + status +
			", errorMessage=" + errorMessage +
			'}';
	}

	public static UserAdditionBuilder builder() {
		return new UserAdditionBuilder();
	}

	public static final class UserAdditionBuilder {
		private UserAdditionId id;
		private SiteId siteId;
		private ProjectId projectId;
		private CorrelationId correlationId;
		private String uid;
		private FenixUserId userId;
		private UserStatus status;
		private Optional<UserAdditionErrorMessage> errorMessage = Optional.empty();

		private UserAdditionBuilder() {
		}

		public UserAdditionBuilder id(String id) {
			this.id = new UserAdditionId(id);
			return this;
		}

		public UserAdditionBuilder projectId(String projectId) {
			this.projectId = new ProjectId(projectId);
			return this;
		}

		public UserAdditionBuilder correlationId(CorrelationId correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public UserAdditionBuilder userId(String userId) {
			this.userId = new FenixUserId(userId);
			return this;
		}

		public UserAdditionBuilder uid(String uid) {
			this.uid = uid;
			return this;
		}

		public UserAdditionBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public UserAdditionBuilder status(UserStatus status) {
			this.status = status;
			return this;
		}

		public UserAdditionBuilder errorMessage(Optional<UserAdditionErrorMessage> errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}

		public UserAddition build() {
			return new UserAddition(id, siteId, projectId, correlationId, uid, userId, status, errorMessage);
		}
	}
}
