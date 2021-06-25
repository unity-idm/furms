/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.users.api.key;

import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.key.UserApiKey;

import java.util.Optional;

public interface UserApiKeyRepository {

    boolean exists(Optional<UserApiKey> userApiKey);

    Optional<UserApiKey> findByUserId(PersistentId userId);

    Optional<UserApiKey> create(Optional<UserApiKey> userApiKey);

    void delete(PersistentId userId);

}
