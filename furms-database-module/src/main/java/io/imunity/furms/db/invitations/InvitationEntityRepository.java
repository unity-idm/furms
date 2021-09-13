/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.invitations;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface InvitationEntityRepository extends CrudRepository<InvitationEntity, UUID> {
	Optional<InvitationEntity> findByIdAndUserId(UUID id, String userId);
	Optional<InvitationEntity> findByCode(String code);
	Optional<InvitationEntity> findByEmailAndRoleAttributeAndRoleValueAndResourceId(String email, String roleAttribute, String roleVale, UUID resourceId);
	Set<InvitationEntity> findByUserIdOrEmail(String userId, String email);
	Set<InvitationEntity> findByRoleAttributeAndRoleValueAndResourceId(String roleAttribute, String roleVale, UUID resourceId);
	@Modifying
	@Query("delete from invitation where code = :code")
	void deleteByCode(@Param("code") String code);
}