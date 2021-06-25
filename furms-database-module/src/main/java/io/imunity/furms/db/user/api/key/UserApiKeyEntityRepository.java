/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user.api.key;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface UserApiKeyEntityRepository extends CrudRepository<UserApiKeyEntity, UUID> {

    boolean existsByUserIdAndApiKey(String userId, UUID apiKey);

    Optional<UserApiKeyEntity> findByUserId(String userId);

    @Modifying
    @Query("DELETE FROM user_api_key WHERE user_id = :userId")
    long deleteByUserId(@Param("userId") String userId);
}
