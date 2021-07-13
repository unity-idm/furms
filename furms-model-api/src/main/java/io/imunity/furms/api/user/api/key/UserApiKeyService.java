/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.user.api.key;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.key.UserApiKey;

import java.util.Optional;
import java.util.UUID;

public interface UserApiKeyService {

	Optional<FURMSUser> findUserByUserIdAndApiKey(PersistentId userId, UUID apiKey);

	Optional<UserApiKey> findByUserId(PersistentId userId);

	void save(PersistentId userId, String value);

	void revoke(PersistentId userId);

}
