/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user.api.key;

import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.key.UserApiKey;
import io.imunity.furms.spi.users.api.key.UserApiKeyRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.isEmpty;

@Repository
class FenixAdminApiKeyRepository implements UserApiKeyRepository {

	private final UserApiKeyEntityRepository entityRepository;

	FenixAdminApiKeyRepository(UserApiKeyEntityRepository entityRepository) {
		this.entityRepository = entityRepository;
	}

	@Override
	public boolean exists(UserApiKey userApiKey) {
		if (notValid(userApiKey)) {
			return false;
		}
		return entityRepository.existsByUserIdAndApiKey(userApiKey.getUserId().id, userApiKey.getApiKey());
	}

	@Override
	public Optional<UserApiKey> findByUserId(PersistentId userId) {
		if (notValid(userId)) {
			return Optional.empty();
		}
		return entityRepository.findByUserId(userId.id)
				.map(UserApiKeyEntity::toUserApiKey);
	}

	@Override
	public Optional<UserApiKey> create(UserApiKey userApiKey) {
		if (notValid(userApiKey)) {
			throw new IllegalArgumentException("Incorrect parameters for API Key create.");
		}
		return Optional.of(entityRepository.save(new UserApiKeyEntity(userApiKey)))
				.map(UserApiKeyEntity::toUserApiKey);
	}

	@Override
	public void delete(PersistentId userId) {
		if (notValid(userId)) {
			return;
		}

		entityRepository.deleteByUserId(userId.id);
	}

	private boolean notValid(UserApiKey userApiKey) {
		return userApiKey == null
				|| userApiKey.getApiKey() == null
				|| notValid(userApiKey.getUserId());
	}

	private boolean notValid(PersistentId userId) {
		return userId == null || isEmpty(userId.id);
	}
}
