/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;

import java.util.Objects;
import java.util.Optional;

public class UserAdditionJob {
	public final UserAdditionJobId id;
	public final UserAdditionId userAdditionId;
	public final CorrelationId correlationId;
	public final UserStatus status;
	public final Optional<UserAdditionErrorMessage> errorMessage;

	UserAdditionJob(UserAdditionJobId id, UserAdditionId userAdditionId, CorrelationId correlationId, UserStatus status, Optional<UserAdditionErrorMessage> errorMessage) {
		this.id = id;
		this.userAdditionId = userAdditionId;
		this.correlationId = correlationId;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAdditionJob that = (UserAdditionJob) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(userAdditionId, that.userAdditionId) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(errorMessage, that.errorMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, correlationId, status, userAdditionId, errorMessage);
	}

	@Override
	public String toString() {
		return "UserRemoval{" +
			"id='" + id + '\'' +
			", userAdditionId='" + userAdditionId + '\'' +
			", correlationId='" + correlationId + '\'' +
			", status=" + status +
			", message=" + errorMessage +
			'}';
	}

	public static UserRemovalBuilder builder() {
		return new UserRemovalBuilder();
	}

	public static final class UserRemovalBuilder {
		private UserAdditionJobId id;
		private UserAdditionId userAdditionId;
		private CorrelationId correlationId;
		private UserStatus status;
		private Optional<UserAdditionErrorMessage> errorMessage = Optional.empty();

		private UserRemovalBuilder() {
		}

		public UserRemovalBuilder id(String id) {
			this.id = new UserAdditionJobId(id);
			return this;
		}

		public UserRemovalBuilder errorMessage(Optional<UserAdditionErrorMessage> errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}

		public UserRemovalBuilder userAdditionId(String userAdditionId) {
			this.userAdditionId = new UserAdditionId(userAdditionId);
			return this;
		}

		public UserRemovalBuilder correlationId(CorrelationId correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public UserRemovalBuilder status(UserStatus status) {
			this.status = status;
			return this;
		}

		public UserAdditionJob build() {
			return new UserAdditionJob(id, userAdditionId, correlationId, status, errorMessage);
		}
	}
}
