/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users.api.key;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.Optional;
import java.util.UUID;

public interface AdminApiKeyFinder {

	Optional<FURMSUser> findUserByUserIdAndApiKey(PersistentId userId, UUID apiKey);

}
