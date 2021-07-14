/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users.api.key;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UnknownUserException;
import io.imunity.furms.domain.users.key.UserApiKey;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.spi.users.api.key.UserApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AdminApiKeyServiceTest {

	@Mock
	private UserApiKeyRepository repository;

	@Mock
	private UsersDAO usersDAO;

	@InjectMocks
	private AdminApiKeyService service;

	@Test
	void shouldFindUserByUserIdAndApiKey() {
		//given
		final PersistentId userId = new PersistentId("userId");
		final UUID apiKey = UUID.randomUUID();
		when(repository.exists(UserApiKey.builder().userId(userId).apiKey(apiKey).build())).thenReturn(true);
		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
				.id(userId)
				.email("email")
				.build()));

		//when
		final Optional<FURMSUser> user = service.findUserByUserIdAndApiKey(userId, apiKey);

		//then
		assertThat(user).isPresent();
		assertThat(user.get().id.get()).isEqualTo(userId);
	}

	@Test
	void shouldNotFindUserByUserIdAndApiKeyDueToNonExistingAPIKey() {
		//given
		final PersistentId userId = new PersistentId("userId");
		final UUID apiKey = UUID.randomUUID();
		when(repository.exists(UserApiKey.builder().userId(userId).apiKey(apiKey).build())).thenReturn(false);

		//when
		final Optional<FURMSUser> user = service.findUserByUserIdAndApiKey(userId, apiKey);

		//then
		assertThat(user).isEmpty();
	}

	@Test
	void shouldFindByUserId() {
		//given
		final PersistentId userId = new PersistentId("userId");
		final UUID apiKey = UUID.randomUUID();
		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
				.id(userId)
				.email("email")
				.build()));
		when(repository.findByUserId(userId)).thenReturn(Optional.of(UserApiKey.builder()
				.userId(userId)
				.apiKey(apiKey)
				.build()));

		//when
		final Optional<UserApiKey> userApiKey = service.findByUserId(userId);

		//then
		assertThat(userApiKey).isPresent();
		assertThat(userApiKey.get().getUserId()).isEqualTo(userId);
		assertThat(userApiKey.get().getApiKey()).isEqualTo(apiKey);
	}

	@Test
	void shouldNotFindByUserIdDueToNonExistingUser() {
		//given
		final PersistentId userId = new PersistentId("userId");
		final UUID apiKey = UUID.randomUUID();
		when(usersDAO.findById(userId)).thenReturn(Optional.empty());

		//when + then
		assertThrows(UnknownUserException.class, () -> service.findByUserId(userId));
	}

	@Test
	void shouldGenerateNewApiKey() {
		//given
		final PersistentId userId = new PersistentId("userId");
		final UUID apiKey = UUID.randomUUID();
		final UserApiKey userApiKey = UserApiKey.builder()
				.apiKey(apiKey)
				.userId(userId)
				.build();
		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
				.id(userId)
				.email("email")
				.build()));
		when(repository.findByUserId(userId)).thenReturn(Optional.empty());
		when(repository.create(any())).thenReturn(Optional.of(userApiKey));

		//when
		service.save(userId, apiKey.toString());
	}

	@Test
	void shouldNotGenerateNewApiKeyDueToUserNotExists() {
		//given
		final PersistentId userId = new PersistentId("userId");
		final UUID apiKey = UUID.randomUUID();
		final UserApiKey userApiKey = UserApiKey.builder()
				.apiKey(apiKey)
				.userId(userId)
				.build();
		when(usersDAO.findById(userId)).thenReturn(Optional.empty());

		//when + then
		assertThrows(UnknownUserException.class, () -> service.save(userId, apiKey.toString()));
	}

	@Test
	void shouldNotGenerateNewApiKeyDueToApiKeyAlreadyExists() {
		//given
		final PersistentId userId = new PersistentId("userId");
		final UUID apiKey = UUID.randomUUID();
		final UserApiKey userApiKey = UserApiKey.builder()
				.apiKey(apiKey)
				.userId(userId)
				.build();
		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
				.id(userId)
				.email("email")
				.build()));
		when(repository.findByUserId(userId)).thenReturn(Optional.of(userApiKey));

		//when + then
		assertThrows(IllegalArgumentException.class, () -> service.save(userId, apiKey.toString()));
	}

	@Test
	void shouldRevokeApiKey() {
		//given
		final PersistentId userId = new PersistentId("userId");
		final UUID apiKey = UUID.randomUUID();
		final UserApiKey userApiKey = UserApiKey.builder()
				.apiKey(apiKey)
				.userId(userId)
				.build();
		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
				.id(userId)
				.email("email")
				.build()));

		//when
		service.revoke(userId);

		//then
		verify(repository, times(1)).delete(userId);
	}

	@Test
	void shouldNotRevokeApiKeyDueToUserNotExists() {
		//given
		final PersistentId userId = new PersistentId("userId");
		when(usersDAO.findById(userId)).thenReturn(Optional.empty());

		//when + then
		assertThrows(UnknownUserException.class, () -> service.revoke(userId));
	}


}