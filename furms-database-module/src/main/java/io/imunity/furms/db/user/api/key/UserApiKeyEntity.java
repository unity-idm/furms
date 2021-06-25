/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user.api.key;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.key.UserApiKey;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("user_api_key")
public class UserApiKeyEntity extends UUIDIdentifiable {

    private final UUID apiKey;

    private final String userId;

    @PersistenceConstructor
    UserApiKeyEntity(UUID id, UUID apiKey, String userId) {
        this.id = id;
        this.apiKey = apiKey;
        this.userId = userId;
    }

    public UserApiKeyEntity(UserApiKey userApiKey) {
        if (userApiKey == null) {
            throw new IllegalArgumentException("UserApiKey object is null.");
        }
        this.id = null;
        this.apiKey = userApiKey.getApiKey();
        this.userId = userApiKey.getUserId().id;
    }

    public UUID getApiKey() {
        return apiKey;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserApiKeyEntity that = (UserApiKeyEntity) o;
        return Objects.equals(apiKey, that.apiKey) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiKey, userId);
    }

    @Override
    public String toString() {
        return "ApiKeyEntity{" +
                "apiKey=" + apiKey +
                ", userId='" + userId + '\'' +
                '}';
    }

    public UserApiKey toUserApiKey() {
        return UserApiKey.builder()
                .apiKey(apiKey)
                .userId(new PersistentId(userId))
                .build();
    }

}
