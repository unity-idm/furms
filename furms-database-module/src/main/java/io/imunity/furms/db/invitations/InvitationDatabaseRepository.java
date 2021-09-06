/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.invitations;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.invitations.InvitationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
class InvitationDatabaseRepository implements InvitationRepository {
	private final InvitationEntityRepository repository;

	InvitationDatabaseRepository(InvitationEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Invitation> findBy(InvitationId id, FenixUserId userId) {
		return repository.findByIdAndUserId(id.id, userId.id)
			.map(InvitationEntity::toInvitation);
	}

	@Override
	public Set<Invitation> findAllBy(FenixUserId id, String email) {
		return repository.findByUserIdOrEmail(id.id, email).stream()
			.map(InvitationEntity::toInvitation)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<Invitation> findAllBy(Role role, UUID resourceId) {
		return repository.findByRoleAttributeAndRoleValueAndResourceId(role.unityRoleAttribute, role.unityRoleValue, resourceId).stream()
			.map(InvitationEntity::toInvitation)
			.collect(Collectors.toSet());
	}

	@Override
	public void create(Invitation invitation) {
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(invitation.resourceId.id)
			.resourceType(invitation.resourceId.type)
			.resourceName(invitation.resourceName)
			.originator(invitation.originator)
			.userId(invitation.userId.id)
			.email(invitation.email)
			.roleAttribute(invitation.role.unityRoleAttribute)
			.roleValue(invitation.role.unityRoleValue)
			.code(Optional.ofNullable(invitation.code).map(code -> code.code).orElse(null))
			.expiredAt(invitation.utcExpiredAt)
			.build();
		repository.save(invitationEntity);
	}

	@Override
	public void deleteBy(InvitationId id) {
		repository.deleteById(id.id);
	}

	@Override
	public void deleteBy(InvitationCode invitationCode) {
		repository.deleteByCode(invitationCode.code);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
}

