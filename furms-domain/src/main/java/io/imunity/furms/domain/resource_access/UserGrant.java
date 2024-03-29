/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;
import java.util.Optional;

public class UserGrant {
	public final ProjectAllocationId projectAllocationId;
	public final FenixUserId userId;
	public final AccessStatus status;
	public final Optional<ErrorAccessMessage> errorMessage;

	UserGrant(ProjectAllocationId projectAllocationId, FenixUserId userId, AccessStatus status, Optional<ErrorAccessMessage> errorMessage) {
		this.projectAllocationId = projectAllocationId;
		this.userId = userId;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserGrant userGrant = (UserGrant) o;
		return Objects.equals(projectAllocationId, userGrant.projectAllocationId) &&
			Objects.equals(userId, userGrant.userId) &&
			status == userGrant.status &&
			Objects.equals(errorMessage, userGrant.errorMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectAllocationId, userId, status, errorMessage);
	}

	@Override
	public String toString() {
		return "UserGrant{" +
			"projectAllocationId='" + projectAllocationId + '\'' +
			", userId='" + userId + '\'' +
			", status=" + status +
			", errorMessage='" + errorMessage + '\'' +
			'}';
	}

	public static UserGrantBuilder builder() {
		return new UserGrantBuilder();
	}

	public static final class UserGrantBuilder {
		public ProjectAllocationId projectAllocationId;
		public FenixUserId userId;
		public AccessStatus status;
		public Optional<ErrorAccessMessage> errorMessage = Optional.empty();

		private UserGrantBuilder() {
		}

		public UserGrantBuilder projectAllocationId(String projectAllocationId) {
			this.projectAllocationId = new ProjectAllocationId(projectAllocationId);
			return this;
		}

		public UserGrantBuilder userId(String userId) {
			this.userId = new FenixUserId(userId);
			return this;
		}

		public UserGrantBuilder status(AccessStatus status) {
			this.status = status;
			return this;
		}

		public UserGrantBuilder message(String message) {
			this.errorMessage = Optional.ofNullable(message).map(ErrorAccessMessage::new);
			return this;
		}

		public UserGrant build() {
			return new UserGrant(projectAllocationId, userId, status, errorMessage);
		}
	}
}
