/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.invitations;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.invitations.InvitationRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
	public Optional<Invitation> findBy(InvitationId id) {
		return repository.findById(id.id)
			.map(InvitationEntity::toInvitation);
	}

	@Override
	public Optional<Invitation> findBy(InvitationId id, String email) {
		return repository.findByIdAndEmail(id.id, email)
			.map(InvitationEntity::toInvitation);
	}

	@Override
	public Optional<Invitation> findBy(InvitationCode code) {
		return repository.findByCode(code.code)
			.map(InvitationEntity::toInvitation);
	}

	@Override
	public Optional<Invitation> findBy(String email, Role role, ResourceId resourceId) {
		return repository.findByEmailAndRoleAttributeAndRoleValueAndResourceId(email, role.unityRoleAttribute, role.unityRoleValue, ResourceIdToUUIDMapper.map(resourceId))
			.map(InvitationEntity::toInvitation);
	}

	@Override
	public Set<Invitation> findAllBy(FenixUserId id) {
		return repository.findAllByUserId(id.id).stream()
			.map(InvitationEntity::toInvitation)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<Invitation> findAllBy(String email) {
		return repository.findAllByEmail(email).stream()
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
	public InvitationId create(Invitation invitation) {
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(ResourceIdToUUIDMapper.map(invitation.resourceId))
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
		InvitationEntity saved = repository.save(invitationEntity);
		return new InvitationId(saved.getId());
	}

	@Override
	public void updateExpiredAt(InvitationId id, LocalDateTime utcExpiredAt) {
		repository.findById(id.id).ifPresent(invitation ->
			repository.save(
				InvitationEntity.builder()
					.id(invitation.getId())
					.resourceId(invitation.resourceId)
					.resourceType(invitation.resourceType)
					.resourceName(invitation.resourceName)
					.originator(invitation.originator)
					.userId(invitation.userId)
					.email(invitation.email)
					.roleAttribute(invitation.roleAttribute)
					.roleValue(invitation.roleValue)
					.code(invitation.code)
					.expiredAt(utcExpiredAt)
					.build()
				)
		);
	}

	@Override
	public void updateExpiredAtAndRole(InvitationId id, LocalDateTime utcExpiredAt, Role role) {
		repository.findById(id.id).ifPresent(invitation ->
			repository.save(
				InvitationEntity.builder()
					.id(invitation.getId())
					.resourceId(invitation.resourceId)
					.resourceType(invitation.resourceType)
					.resourceName(invitation.resourceName)
					.originator(invitation.originator)
					.userId(invitation.userId)
					.email(invitation.email)
					.roleAttribute(role.unityRoleAttribute)
					.roleValue(role.unityRoleValue)
					.code(invitation.code)
					.expiredAt(utcExpiredAt)
					.build()
			)
		);
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

