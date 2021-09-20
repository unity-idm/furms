/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.invitations;


import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.FenixUserId;

@SpringBootTest
class InvitationDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private InvitationDatabaseRepository invitationDatabaseRepository;
	@Autowired
	private InvitationEntityRepository entityRepository;

	@BeforeEach
	void init() {
		entityRepository.deleteAll();
	}

	@Test
	void shouldFindById() {
		Role role = Role.PROJECT_ADMIN;
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(PROJECT)
			.roleAttribute(role.unityRoleAttribute)
			.roleValue(role.unityRoleValue)
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();
		entityRepository.save(invitationEntity);

		Optional<Invitation> invitation = invitationDatabaseRepository.findBy(new InvitationId(invitationEntity.getId()));
		assertTrue(invitation.isPresent());
		assertEquals(invitationEntity.resourceId, invitation.get().resourceId.id);
		assertEquals(invitationEntity.resourceType, invitation.get().resourceId.type.getPersistentId());
		assertEquals(invitationEntity.resourceName, invitation.get().resourceName);
		assertEquals(invitationEntity.code, invitation.get().code.code);
		assertEquals(invitationEntity.email, invitation.get().email);
		assertEquals(invitationEntity.originator, invitation.get().originator);
		assertEquals(invitationEntity.expiredAt, invitation.get().utcExpiredAt);
		assertEquals(invitationEntity.userId, invitation.get().userId.id);
		assertEquals(invitationEntity.roleAttribute, invitation.get().role.unityRoleAttribute);
		assertEquals(invitationEntity.roleValue, invitation.get().role.unityRoleValue);
	}

	@Test
	void shouldFindByCode() {
		Role role = Role.PROJECT_ADMIN;
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(PROJECT)
			.roleAttribute(role.unityRoleAttribute)
			.roleValue(role.unityRoleValue)
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();
		entityRepository.save(invitationEntity);

		Optional<Invitation> invitation = invitationDatabaseRepository.findBy(new InvitationCode(invitationEntity.code));
		assertTrue(invitation.isPresent());
		assertEquals(invitationEntity.resourceId, invitation.get().resourceId.id);
		assertEquals(invitationEntity.resourceType, invitation.get().resourceId.type.getPersistentId());
		assertEquals(invitationEntity.resourceName, invitation.get().resourceName);
		assertEquals(invitationEntity.code, invitation.get().code.code);
		assertEquals(invitationEntity.email, invitation.get().email);
		assertEquals(invitationEntity.originator, invitation.get().originator);
		assertEquals(invitationEntity.expiredAt, invitation.get().utcExpiredAt);
		assertEquals(invitationEntity.userId, invitation.get().userId.id);
		assertEquals(invitationEntity.roleAttribute, invitation.get().role.unityRoleAttribute);
		assertEquals(invitationEntity.roleValue, invitation.get().role.unityRoleValue);
	}

	@Test
	void shouldFindByEmailAndRoleAndResourceId() {
		Role role = Role.PROJECT_ADMIN;
		UUID resourceId = UUID.randomUUID();
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(PROJECT)
			.roleAttribute(role.unityRoleAttribute)
			.roleValue(role.unityRoleValue)
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();
		entityRepository.save(invitationEntity);

		Optional<Invitation> invitation = invitationDatabaseRepository.findBy("email", role, new ResourceId(resourceId, PROJECT));
		assertTrue(invitation.isPresent());
		assertEquals(invitationEntity.resourceId, invitation.get().resourceId.id);
		assertEquals(invitationEntity.resourceType, invitation.get().resourceId.type.getPersistentId());
		assertEquals(invitationEntity.resourceName, invitation.get().resourceName);
		assertEquals(invitationEntity.code, invitation.get().code.code);
		assertEquals(invitationEntity.email, invitation.get().email);
		assertEquals(invitationEntity.originator, invitation.get().originator);
		assertEquals(invitationEntity.expiredAt, invitation.get().utcExpiredAt);
		assertEquals(invitationEntity.userId, invitation.get().userId.id);
		assertEquals(invitationEntity.roleAttribute, invitation.get().role.unityRoleAttribute);
		assertEquals(invitationEntity.roleValue, invitation.get().role.unityRoleValue);
	}

	@Test
	void shouldFindAllByUserId() {
		Role role = Role.PROJECT_ADMIN;
		UUID resourceId = UUID.randomUUID();
		LocalDateTime expiredAt = LocalDate.now().atStartOfDay();
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(APP_LEVEL)
			.roleAttribute(role.unityRoleAttribute)
			.roleValue(role.unityRoleValue)
			.code("code")
			.expiredAt(expiredAt)
			.build();
		InvitationEntity invitationEntity1 = InvitationEntity.builder()
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId1")
			.email("email1")
			.resourceType(APP_LEVEL)
			.roleAttribute(role.unityRoleAttribute)
			.roleValue(role.unityRoleValue)
			.code("code1")
			.expiredAt(expiredAt)
			.build();
		UUID resourceId1 = UUID.randomUUID();
		InvitationEntity invitationEntity2 = InvitationEntity.builder()
			.resourceId(resourceId1)
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId1")
			.email("email1")
			.resourceType(APP_LEVEL)
			.roleAttribute(role.unityRoleAttribute)
			.roleValue(role.unityRoleValue)
			.code("code2")
			.expiredAt(expiredAt)
			.build();
		entityRepository.saveAll(Set.of(invitationEntity, invitationEntity1, invitationEntity2));

		Set<Invitation> invitations = invitationDatabaseRepository.findAllBy(new FenixUserId("userId1"), null);

		assertEquals(2, invitations.size());
		Invitation invitation = invitations.iterator().next();
		assertThat(invitation.resourceId.id).isIn(resourceId, resourceId1);
		assertEquals(invitation.resourceId.type, APP_LEVEL);
		assertEquals(invitation.resourceName, "resourceName");
		assertThat(invitation.code.code).isIn("code1", "code2");
		assertThat(invitation.email).isEqualTo("email1");
		assertEquals(invitation.originator, "originator");
		assertEquals(invitation.utcExpiredAt, expiredAt);
		assertEquals(invitation.role.unityRoleAttribute, role.unityRoleAttribute);
		assertEquals(invitation.role.unityRoleValue, role.unityRoleValue);
	}

	@Test
	void shouldFindAllByEmail() {
		Role role = Role.PROJECT_ADMIN;
		UUID resourceId = UUID.randomUUID();
		LocalDateTime expiredAt = LocalDate.now().atStartOfDay();
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(APP_LEVEL)
			.roleAttribute(role.unityRoleAttribute)
			.roleValue(role.unityRoleValue)
			.code("code")
			.expiredAt(expiredAt)
			.build();
		InvitationEntity invitationEntity1 = InvitationEntity.builder()
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId1")
			.email("email1")
			.resourceType(APP_LEVEL)
			.roleAttribute(role.unityRoleAttribute)
			.roleValue(role.unityRoleValue)
			.code("code1")
			.expiredAt(expiredAt)
			.build();
		UUID resourceId1 = UUID.randomUUID();
		InvitationEntity invitationEntity2 = InvitationEntity.builder()
			.resourceId(resourceId1)
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId1")
			.email("email1")
			.resourceType(APP_LEVEL)
			.roleAttribute(role.unityRoleAttribute)
			.roleValue(role.unityRoleValue)
			.code("code2")
			.expiredAt(expiredAt)
			.build();
		entityRepository.saveAll(Set.of(invitationEntity, invitationEntity1, invitationEntity2));

		Set<Invitation> invitations = invitationDatabaseRepository.findAllBy(FenixUserId.empty(), "email1");

		assertEquals(2, invitations.size());
		Invitation invitation = invitations.iterator().next();
		assertThat(invitation.resourceId.id).isIn(resourceId, resourceId1);
		assertEquals(invitation.resourceId.type, APP_LEVEL);
		assertEquals(invitation.resourceName, "resourceName");
		assertThat(invitation.code.code).isIn("code1", "code2");
		assertThat(invitation.email).isEqualTo("email1");
		assertEquals(invitation.originator, "originator");
		assertEquals(invitation.utcExpiredAt, expiredAt);
		assertEquals(invitation.role.unityRoleAttribute, role.unityRoleAttribute);
		assertEquals(invitation.role.unityRoleValue, role.unityRoleValue);
	}

	@Test
	void shouldFindAllByRoleAndResourceId() {
		Role role = Role.PROJECT_ADMIN;
		UUID resourceId = UUID.randomUUID();
		LocalDateTime expiredAt = LocalDate.now().atStartOfDay();
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(APP_LEVEL)
			.roleAttribute(role.unityRoleAttribute)
			.roleValue(role.unityRoleValue)
			.code("code")
			.expiredAt(expiredAt)
			.build();
		InvitationEntity invitationEntity1 = InvitationEntity.builder()
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId1")
			.email("email1")
			.resourceType(APP_LEVEL)
			.roleAttribute(role.unityRoleAttribute)
			.roleValue(role.unityRoleValue)
			.code("code1")
			.expiredAt(expiredAt)
			.build();
		InvitationEntity invitationEntity2 = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId1")
			.email("email1")
			.resourceType(APP_LEVEL)
			.roleAttribute(role.unityRoleAttribute)
			.roleValue(role.unityRoleValue)
			.code("code2")
			.expiredAt(expiredAt)
			.build();
		entityRepository.saveAll(Set.of(invitationEntity, invitationEntity1, invitationEntity2));

		Set<Invitation> invitations = invitationDatabaseRepository.findAllBy(role, resourceId);

		assertEquals(2, invitations.size());
		Invitation invitation = invitations.iterator().next();
		assertEquals(invitation.resourceId.id, resourceId);
		assertEquals(invitation.resourceId.type, APP_LEVEL);
		assertEquals(invitation.resourceName, "resourceName");
		assertThat(invitation.code.code).isIn("code", "code1");
		assertThat(invitation.email).isIn("email", "email1");
		assertEquals(invitation.originator, "originator");
		assertEquals(invitation.utcExpiredAt, expiredAt);
		assertThat(invitation.email).isIn("email", "email1");
		assertEquals(invitation.role.unityRoleAttribute, role.unityRoleAttribute);
		assertEquals(invitation.role.unityRoleValue, role.unityRoleValue);
	}

	@Test
	void shouldCreate() {
		Invitation invitation = Invitation.builder()
			.resourceId(new ResourceId(UUID.randomUUID(), APP_LEVEL))
			.resourceName("resourceName")
			.originator("originator")
			.userId(new FenixUserId("userId"))
			.email("email")
			.role(Role.PROJECT_ADMIN)
			.code("code")
			.utcExpiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationId invitationId = invitationDatabaseRepository.create(invitation);

		Optional<InvitationEntity> invitationEntity = entityRepository.findById(invitationId.id);
		assertEquals(true, invitationEntity.isPresent());
		assertEquals(invitation.resourceId.id, invitationEntity.get().resourceId);
		assertEquals(invitation.resourceId.type.getPersistentId(), invitationEntity.get().resourceType);
		assertEquals(invitation.resourceName, invitationEntity.get().resourceName);
		assertEquals(invitation.code.code, invitationEntity.get().code);
		assertEquals(invitation.email, invitationEntity.get().email);
		assertEquals(invitation.originator, invitationEntity.get().originator);
		assertEquals(invitation.utcExpiredAt, invitationEntity.get().expiredAt);
		assertEquals(invitation.userId.id, invitationEntity.get().userId);
		assertEquals(invitation.role.unityRoleAttribute, invitationEntity.get().roleAttribute);
		assertEquals(invitation.role.unityRoleValue, invitationEntity.get().roleValue);
	}

	@Test
	void shouldUpdateExpiredAt() {
		LocalDateTime dateTime = LocalDate.now().atStartOfDay().plusDays(5);
		Invitation invitation = Invitation.builder()
			.resourceId(new ResourceId(UUID.randomUUID(), APP_LEVEL))
			.resourceName("resourceName")
			.originator("originator")
			.userId(new FenixUserId("userId"))
			.email("email")
			.role(Role.PROJECT_ADMIN)
			.code("code")
			.utcExpiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationId invitationId = invitationDatabaseRepository.create(invitation);
		invitationDatabaseRepository.updateExpiredAt(invitationId, dateTime);

		Optional<InvitationEntity> invitationEntity = entityRepository.findById(invitationId.id);
		assertTrue(invitationEntity.isPresent());
		assertEquals(invitation.resourceId.id, invitationEntity.get().resourceId);
		assertEquals(invitation.resourceId.type.getPersistentId(), invitationEntity.get().resourceType);
		assertEquals(invitation.resourceName, invitationEntity.get().resourceName);
		assertEquals(invitation.code.code, invitationEntity.get().code);
		assertEquals(invitation.email, invitationEntity.get().email);
		assertEquals(invitation.originator, invitationEntity.get().originator);
		assertEquals(dateTime, invitationEntity.get().expiredAt);
		assertEquals(invitation.userId.id, invitationEntity.get().userId);
		assertEquals(invitation.role.unityRoleAttribute, invitationEntity.get().roleAttribute);
		assertEquals(invitation.role.unityRoleValue, invitationEntity.get().roleValue);
	}

	@Test
	void shouldDeleteById() {
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();
		InvitationEntity saved = entityRepository.save(invitationEntity);

		invitationDatabaseRepository.deleteBy(new InvitationId(saved.getId()));

		assertEquals(Optional.empty(), entityRepository.findById(saved.getId()));
	}

	@Test
	void shouldDeleteByInvitationCode() {
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();
		InvitationEntity saved = entityRepository.save(invitationEntity);

		invitationDatabaseRepository.deleteBy(new InvitationCode("code"));

		assertEquals(Optional.empty(), entityRepository.findById(saved.getId()));
	}

	@Test
	void shouldDeleteAll() {
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		entityRepository.saveAll(Set.of(invitationEntity));

		invitationDatabaseRepository.deleteBy(new InvitationCode("code"));

		assertEquals(false, entityRepository.findAll().iterator().hasNext());
	}
}