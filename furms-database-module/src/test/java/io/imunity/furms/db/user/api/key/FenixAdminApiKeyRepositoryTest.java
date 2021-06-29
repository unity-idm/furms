/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user.api.key;


import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.key.UserApiKey;
import io.imunity.furms.spi.users.api.key.UserApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FenixAdminApiKeyRepositoryTest {

    @Autowired
    private UserApiKeyRepository repository;

    @Autowired
    private UserApiKeyEntityRepository entityRepository;

    @BeforeEach
    void setUp() {
        entityRepository.deleteAll();
    }

    @Test
    void shouldNotExistsForNonCompleteParam() {
        assertFalse(repository.exists(null));
        assertFalse(repository.exists(UserApiKey.builder()
                .userId(new PersistentId("userId"))
                .build()));
        assertFalse(repository.exists(UserApiKey.builder()
                .apiKey(UUID.randomUUID())
                .build()));
    }

    @Test
    void shouldNotExistsForNonExistingUserApiKeyRecord() {
        //given
        final UserApiKey userApiKey = UserApiKey.builder()
                .userId(new PersistentId("userId"))
                .apiKey(UUID.randomUUID())
                .build();
        entityRepository.save(new UserApiKeyEntity(userApiKey));

        final UserApiKey wrongUserId = UserApiKey.builder()
                .userId(new PersistentId("otherUserId"))
                .apiKey(userApiKey.getApiKey())
                .build();
        final UserApiKey wrongApiKey = UserApiKey.builder()
                .userId(userApiKey.getUserId())
                .apiKey(UUID.randomUUID())
                .build();

        //when + then
        assertFalse(repository.exists(wrongUserId));
        assertFalse(repository.exists(wrongApiKey));
    }

    @Test
    void shouldFindByUserId() {
        //given
        final UserApiKey userApiKey = UserApiKey.builder()
                .userId(new PersistentId("userId"))
                .apiKey(UUID.randomUUID())
                .build();
        entityRepository.save(new UserApiKeyEntity(userApiKey));

        //when
        final Optional<UserApiKey> byUserId = repository.findByUserId(userApiKey.getUserId());

        //then
        assertThat(byUserId).isPresent();
        assertThat(byUserId.get().getUserId()).isEqualTo(userApiKey.getUserId());
        assertThat(byUserId.get().getApiKey()).isEqualTo(userApiKey.getApiKey());
    }

    @Test
    void shouldNotFindByUserIdForIncorrectUserId() {
        //then
        assertThat(repository.findByUserId(null)).isEmpty();
        assertThat(repository.findByUserId(new PersistentId(""))).isEmpty();
        assertThat(repository.findByUserId(new PersistentId("non-existed"))).isEmpty();
    }

    @Test
    void shouldCreateUserApiKey() {
        //given
        final UserApiKey userApiKey = UserApiKey.builder()
                .userId(new PersistentId("userId"))
                .apiKey(UUID.randomUUID())
                .build();

        //when
        repository.create(userApiKey);

        //then
        final Optional<UserApiKeyEntity> created = entityRepository.findByUserId(userApiKey.getUserId().id);
        assertThat(created).isPresent();
        assertThat(created.get().getUserId()).isEqualTo(userApiKey.getUserId().id);
        assertThat(created.get().getApiKey()).isEqualTo(userApiKey.getApiKey());
    }

    @Test
    void shouldNotCreateForNonCompleteUserApiKeyParam() {
        assertThrows(IllegalArgumentException.class, () -> repository.create(null));
        assertThrows(IllegalArgumentException.class, () -> repository.create(UserApiKey.builder()
                .userId(new PersistentId("userId"))
                .build()));
        assertThrows(IllegalArgumentException.class, () -> repository.create(UserApiKey.builder()
                .apiKey(UUID.randomUUID())
                .build()));
    }

    @Test
    void shouldDeleteUserApiKeyForSpecificUserId() {
        //given
        final UserApiKey userApiKey = UserApiKey.builder()
                .userId(new PersistentId("userId"))
                .apiKey(UUID.randomUUID())
                .build();
        entityRepository.save(new UserApiKeyEntity(userApiKey));

        //when
        repository.delete(userApiKey.getUserId());

        //then
        final Optional<UserApiKeyEntity> created = entityRepository.findByUserId(userApiKey.getUserId().id);
        assertThat(created).isEmpty();
    }

    @Test
    void shouldNotDeleteDueToWrongUserId() {
        //given
        final UserApiKey userApiKey = UserApiKey.builder()
                .userId(new PersistentId("userId"))
                .apiKey(UUID.randomUUID())
                .build();
        entityRepository.save(new UserApiKeyEntity(userApiKey));

        //when
        repository.delete(new PersistentId("darth-vader"));

        //then
        final Optional<UserApiKeyEntity> created = entityRepository.findByUserId(userApiKey.getUserId().id);
        assertThat(created).isPresent();
    }


}