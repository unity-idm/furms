/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users.api.key;

import io.imunity.furms.api.user.api.key.UserApiKeyService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.config.security.method.FurmsPublicAccess;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UnknownUserException;
import io.imunity.furms.domain.users.key.UserApiKey;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.spi.users.api.key.UserApiKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.Capability.REST_API_KEY_MANAGEMENT;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static org.apache.logging.log4j.util.Strings.isEmpty;

@Service
class AdminApiKeyService implements UserApiKeyService {

	private final UserApiKeyRepository repository;
	private final UsersDAO usersDAO;

	AdminApiKeyService(UserApiKeyRepository repository, UsersDAO usersDAO) {
		this.repository = repository;
		this.usersDAO = usersDAO;
	}

	@Override
	@FurmsPublicAccess
	public Optional<FURMSUser> findUserByUserIdAndApiKey(PersistentId userId, UUID apiKey) {
		final boolean exists = repository.exists(UserApiKey.builder()
				.userId(userId)
				.apiKey(apiKey)
				.build());

		if (!exists) {
			return Optional.empty();
		}

		return usersDAO.findById(userId);
	}

	@Override
	@FurmsAuthorize(capability = REST_API_KEY_MANAGEMENT, resourceType = APP_LEVEL)
	public Optional<UserApiKey> findByUserId(PersistentId userId) {
		assertUserExists(userId);

		return repository.findByUserId(userId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = REST_API_KEY_MANAGEMENT, resourceType = APP_LEVEL)
	public void save(PersistentId userId, String value) {
		assertUserExists(userId);
		assertKeyNotExists(userId);

		repository.delete(userId);

		repository.create(UserApiKey.builder()
				.apiKey(UUID.fromString(value))
				.userId(userId)
				.build());
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = REST_API_KEY_MANAGEMENT, resourceType = APP_LEVEL)
	public void revoke(PersistentId userId) {
		assertUserExists(userId);

		repository.delete(userId);
	}

	private void assertUserExists(PersistentId userId) {
		assertTrue(userId != null && !isEmpty(userId.id),
				() -> {
					throw new IllegalArgumentException("User not exists.");
				});

		usersDAO.findById(userId)
				.orElseThrow(() -> new UnknownUserException(new FenixUserId(userId.id)));
	}


	private void assertKeyNotExists(PersistentId userId) {
		assertTrue(userId != null && !isEmpty(userId.id),
				() -> {
					throw new IllegalArgumentException("User not exists.");
				});

		assertTrue(repository.findByUserId(userId).isEmpty(),
				() -> {
					throw new IllegalArgumentException("API KEY for specific user already exists");
				});
	}
}
