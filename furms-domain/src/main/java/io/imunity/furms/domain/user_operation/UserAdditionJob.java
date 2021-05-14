/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;

import java.util.Objects;
import java.util.Optional;

public class UserAdditionJob {
	public final String id;
	public final String userAdditionId;
	public final CorrelationId correlationId;
	public final UserStatus status;
	public final Optional<ErrorUserMessage> message;

	UserAdditionJob(String id, String userAdditionId, CorrelationId correlationId, UserStatus status, Optional<ErrorUserMessage> message) {
		this.id = id;
		this.userAdditionId = userAdditionId;
		this.correlationId = correlationId;
		this.status = status;
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAdditionJob that = (UserAdditionJob) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(userAdditionId, that.userAdditionId) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, correlationId, status, userAdditionId, message);
	}

	@Override
	public String toString() {
		return "UserRemoval{" +
			"id='" + id + '\'' +
			", userAdditionId='" + userAdditionId + '\'' +
			", correlationId='" + correlationId + '\'' +
			", status=" + status +
			", message=" + message +
			'}';
	}

	public static UserRemovalBuilder builder() {
		return new UserRemovalBuilder();
	}

	public static final class UserRemovalBuilder {
		private String id;
		private String userAdditionId;
		private CorrelationId correlationId;
		private UserStatus status;
		private Optional<ErrorUserMessage> message = Optional.empty();

		private UserRemovalBuilder() {
		}

		public UserRemovalBuilder id(String id) {
			this.id = id;
			return this;
		}

		public UserRemovalBuilder message(String message) {
			this.message = Optional.ofNullable(message).map(ErrorUserMessage::new);
			return this;
		}

		public UserRemovalBuilder userAdditionId(String userAdditionId) {
			this.userAdditionId = userAdditionId;
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
			return new UserAdditionJob(id, userAdditionId, correlationId, status, message);
		}
	}
}
