/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users.key;

import io.imunity.furms.domain.users.PersistentId;

import java.util.Objects;
import java.util.UUID;

public class UserApiKey {

	private final PersistentId userId;
	private final UUID apiKey;

	public UserApiKey(PersistentId userId, UUID apiKey) {
		this.userId = userId;
		this.apiKey = apiKey;
	}

	public PersistentId getUserId() {
		return userId;
	}

	public UUID getApiKey() {
		return apiKey;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserApiKey that = (UserApiKey) o;
		return Objects.equals(userId, that.userId) && Objects.equals(apiKey, that.apiKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, apiKey);
	}

	@Override
	public String toString() {
		return "UserApiKey{" +
				"userId='" + userId + '\'' +
				", apiKey=" + apiKey +
				'}';
	}

	public static UserApiKeyBuilder builder() {
		return new UserApiKeyBuilder();
	}

	public static final class UserApiKeyBuilder {
		private PersistentId userId;
		private UUID apiKey;

		private UserApiKeyBuilder() {
		}

		public UserApiKeyBuilder userId(PersistentId userId) {
			this.userId = userId;
			return this;
		}

		public UserApiKeyBuilder apiKey(UUID apiKey) {
			this.apiKey = apiKey;
			return this;
		}

		public UserApiKey build() {
			return new UserApiKey(userId, apiKey);
		}
	}
}
