/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.invitations;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.authz.roles.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class InvitationEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private InvitationEntityRepository entityRepository;

	@BeforeEach
	void init() {
		entityRepository.deleteAll();
	}

	@Test
	void shouldFindByUserId() {
		InvitationEntity users1InvitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("user1Id")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code1")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity users1InvitationEntity1 = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("user1Id")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code2")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity users2InvitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("user2Id")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		Iterable<InvitationEntity> users1InvitationEntities = entityRepository.saveAll(Set.of(users1InvitationEntity, users1InvitationEntity1));
		entityRepository.save(users2InvitationEntity);

		Set<InvitationEntity> users1InvitationEntitiesSet = StreamSupport.stream(users1InvitationEntities.spliterator(), false)
			.collect(Collectors.toSet());

		assertEquals(users1InvitationEntitiesSet, entityRepository.findByUserIdOrEmail("user1Id", null));
	}

	@Test
	void shouldFindByCode() {
		InvitationEntity users1InvitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("user1Id")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code1")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity users1InvitationEntity1 = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("user1Id")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code2")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity users2InvitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("user2Id")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code3")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity saved = entityRepository.save(users1InvitationEntity);
		entityRepository.saveAll(Set.of(users1InvitationEntity1, users2InvitationEntity));

		assertEquals(Optional.of(saved), entityRepository.findByCode("code1"));
	}

	@Test
	void shouldFindByEmailAndRoleAttributeAndRoleValueAndResourceId() {
		UUID resourceId = UUID.randomUUID();
		InvitationEntity users1InvitationEntity = InvitationEntity.builder()
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId("user1Id")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code1")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity users1InvitationEntity1 = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("user1Id")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code2")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity users2InvitationEntity = InvitationEntity.builder()
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId("user2Id")
			.email("email1")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code3")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity saved = entityRepository.save(users1InvitationEntity);
		entityRepository.saveAll(Set.of(users1InvitationEntity1, users2InvitationEntity));

		assertEquals(Optional.of(saved), entityRepository.findByEmailAndRoleAttributeAndRoleValueAndResourceId("email", "roleAttribute", "roleValue", resourceId));
	}

	@Test
	void shouldFindByEmail() {
		InvitationEntity users1InvitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("user1Id")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code1")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity users1InvitationEntity1 = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("user1Id")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code2")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity users2InvitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("user2Id")
			.email("email2")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		Iterable<InvitationEntity> users1InvitationEntities = entityRepository.saveAll(Set.of(users1InvitationEntity, users1InvitationEntity1));
		entityRepository.save(users2InvitationEntity);

		Set<InvitationEntity> users1InvitationEntitiesSet = StreamSupport.stream(users1InvitationEntities.spliterator(), false)
			.collect(Collectors.toSet());

		assertEquals(users1InvitationEntitiesSet, entityRepository.findByUserIdOrEmail(null, "email"));
	}

	@Test
	void shouldFindByRoleAttributeAndRoleValueAndResourceId() {
		UUID resourceId = UUID.randomUUID();
		InvitationEntity users1InvitationEntity = InvitationEntity.builder()
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId("user1Id")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code1")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity users1InvitationEntity1 = InvitationEntity.builder()
			.resourceId(resourceId)
			.resourceName("resourceName")
			.originator("originator")
			.userId("user2Id")
			.email("email2")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code2")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity users2InvitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("user3Id")
			.email("email3")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		Iterable<InvitationEntity> users1InvitationEntities = entityRepository.saveAll(Set.of(users1InvitationEntity, users1InvitationEntity1));
		entityRepository.save(users2InvitationEntity);

		Set<InvitationEntity> users1InvitationEntitiesSet = StreamSupport.stream(users1InvitationEntities.spliterator(), false)
			.collect(Collectors.toSet());

		assertEquals(users1InvitationEntitiesSet, entityRepository.findByRoleAttributeAndRoleValueAndResourceId("roleAttribute", "roleValue", resourceId));
	}

	@Test
	void shouldCreate() {
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity saved = entityRepository.save(invitationEntity);

		assertEquals(saved, entityRepository.findById(saved.getId()).get());
	}

	@Test
	void shouldUpdate() {
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity saved = entityRepository.save(invitationEntity);

		InvitationEntity updateInvitationEntity = InvitationEntity.builder()
			.id(saved.getId())
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName2")
			.originator("originator2")
			.userId("userId2")
			.email("email2")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute2")
			.roleValue("roleValue2")
			.code("code2")
			.expiredAt(LocalDate.now().plusDays(2).atStartOfDay())
			.build();

		InvitationEntity updated = entityRepository.save(updateInvitationEntity);

		assertEquals(updated, entityRepository.findById(updated.getId()).get());
	}

	@Test
	void shouldDeleteById() {
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity saved = entityRepository.save(invitationEntity);

		entityRepository.deleteById(saved.getId());

		assertEquals(Optional.empty(), entityRepository.findById(saved.getId()));
	}

	@Test
	void shouldDeleteByCode() {
		InvitationEntity invitationEntity = InvitationEntity.builder()
			.resourceId(UUID.randomUUID())
			.resourceName("resourceName")
			.originator("originator")
			.userId("userId")
			.email("email")
			.resourceType(ResourceType.APP_LEVEL)
			.roleAttribute("roleAttribute")
			.roleValue("roleValue")
			.code("code")
			.expiredAt(LocalDate.now().atStartOfDay())
			.build();

		InvitationEntity saved = entityRepository.save(invitationEntity);

		entityRepository.deleteByCode("code");

		assertEquals(Optional.empty(), entityRepository.findById(saved.getId()));
	}
}