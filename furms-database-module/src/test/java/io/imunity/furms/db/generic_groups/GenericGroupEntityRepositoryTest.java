/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GenericGroupEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private CommunityRepository communityRepository;

	@Autowired
	private GenericGroupEntityRepository entityRepository;
	@Autowired
	private GenericGroupMembershipEntityRepository membershipEntityRepository;

	private UUID communityId;
	private UUID communityId2;

	@BeforeEach
	void setUp() {
		String id = communityRepository.create(Community.builder()
			.name("name")
			.description("description")
			.logo(FurmsImage.empty())
			.build()
		);
		communityId = UUID.fromString(id);
		String id2 = communityRepository.create(Community.builder()
			.name("new_name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.build()
		);
		communityId2 = UUID.fromString(id2);
	}

	@Test
	void shouldSave(){
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		GenericGroupEntity savedGroup = entityRepository.save(genericGroupEntity);

		GenericGroupEntity foundGroup = entityRepository.findById(savedGroup.getId()).get();

		assertEquals(savedGroup, foundGroup);
	}

	@Test
	void shouldDelete(){
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		GenericGroupEntity savedGroup = entityRepository.save(genericGroupEntity);
		entityRepository.deleteById(genericGroupEntity.getId());

		Optional<GenericGroupEntity> found = entityRepository.findById(savedGroup.getId());

		assertTrue(found.isEmpty());
	}

	@Test
	void shouldUpdate(){
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		GenericGroupEntity savedGroup = entityRepository.save(genericGroupEntity);

		GenericGroupEntity genericGroupEntity1 = GenericGroupEntity.builder()
			.id(savedGroup.getId())
			.name("name1")
			.communityId(communityId)
			.description("description1")
			.build();
		GenericGroupEntity updatedGroup = entityRepository.save(genericGroupEntity1);

		GenericGroupEntity foundGroup = entityRepository.findById(savedGroup.getId()).get();

		assertEquals(updatedGroup, foundGroup);
	}

	@Test
	void shouldFindAllByCommunityId() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		GenericGroupEntity group1 = entityRepository.save(genericGroupEntity);

		GenericGroupEntity genericGroupEntity1 = GenericGroupEntity.builder()
			.name("name1")
			.communityId(communityId)
			.description("description1")
			.build();
		GenericGroupEntity group2 = entityRepository.save(genericGroupEntity1);

		GenericGroupEntity genericGroupEntity2 = GenericGroupEntity.builder()
			.name("name2")
			.communityId(communityId2)
			.description("description2")
			.build();
		entityRepository.save(genericGroupEntity2);

		Set<GenericGroupEntity> allByCommunityId = entityRepository.findAllByCommunityId(communityId);
		assertEquals(Set.of(group1, group2), allByCommunityId);
	}

	@Test
	void shouldExistByCommunityIdAndId() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		GenericGroupEntity group1 = entityRepository.save(genericGroupEntity);

		boolean existsByCommunityIdAndId = entityRepository.existsByCommunityIdAndId(communityId, group1.getId());

		assertTrue(existsByCommunityIdAndId);
	}

	@Test
	void shouldNotExistByCommunityIdAndId() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId2)
			.description("description")
			.build();
		GenericGroupEntity group1 = entityRepository.save(genericGroupEntity);

		boolean existsByCommunityIdAndId = entityRepository.existsByCommunityIdAndId(communityId, group1.getId());

		assertFalse(existsByCommunityIdAndId);
	}

	@Test
	void shouldExistByCommunityIdAndName() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		entityRepository.save(genericGroupEntity);

		boolean existsByCommunityIdAndName = entityRepository.existsByCommunityIdAndName(communityId, "name");

		assertTrue(existsByCommunityIdAndName);
	}

	@Test
	void shouldNotExistByCommunityIdAndName() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		entityRepository.save(genericGroupEntity);

		GenericGroupEntity genericGroupEntity1 = GenericGroupEntity.builder()
			.name("name1")
			.communityId(communityId2)
			.description("description")
			.build();
		entityRepository.save(genericGroupEntity1);

		boolean existsByCommunityIdAndName = entityRepository.existsByCommunityIdAndName(communityId, "name1");

		assertFalse(existsByCommunityIdAndName);
	}

	@Test
	void shouldFindAllWithAssignmentAmountEquals2() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		GenericGroupEntity savedGroup = entityRepository.save(genericGroupEntity);

		GenericGroupMembershipEntity genericGroupEntity1 = GenericGroupMembershipEntity.builder()
			.userId("userId")
			.genericGroupId(savedGroup.getId())
			.memberSince(LocalDateTime.now())
			.build();
		GenericGroupMembershipEntity genericGroupEntity2 = GenericGroupMembershipEntity.builder()
			.userId("userId2")
			.genericGroupId(savedGroup.getId())
			.memberSince(LocalDateTime.now())
			.build();
		membershipEntityRepository.save(genericGroupEntity1);
		membershipEntityRepository.save(genericGroupEntity2);

		Set<GenericGroupEntityWithMembershipAmount> allWithAssignmentAmount = entityRepository.findAllWithAssignmentAmount(communityId);

		assertEquals(1, allWithAssignmentAmount.size());
		GenericGroupEntityWithMembershipAmount membershipAmount = allWithAssignmentAmount.iterator().next();
		assertEquals("name", membershipAmount.name);
		assertEquals(communityId, membershipAmount.communityId);
		assertEquals("description", membershipAmount.description);
		assertEquals(2, membershipAmount.membershipAmount);
	}

	@Test
	void shouldFindAllWithAssignmentAmountEquals0() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		entityRepository.save(genericGroupEntity);

		Set<GenericGroupEntityWithMembershipAmount> allWithAssignmentAmount = entityRepository.findAllWithAssignmentAmount(communityId);

		assertEquals(1, allWithAssignmentAmount.size());
		GenericGroupEntityWithMembershipAmount membershipAmount = allWithAssignmentAmount.iterator().next();
		assertEquals("name", membershipAmount.name);
		assertEquals(communityId, membershipAmount.communityId);
		assertEquals("description", membershipAmount.description);
		assertEquals(0, membershipAmount.membershipAmount);
	}

	@Test
	void shouldFindAllAssignmentsByCommunityIdAndGroupId() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		GenericGroupEntity savedGroup = entityRepository.save(genericGroupEntity);

		GenericGroupEntity genericGroupEntity1 = GenericGroupEntity.builder()
			.name("name2")
			.communityId(communityId)
			.description("description2")
			.build();
		entityRepository.save(genericGroupEntity1);

		GenericGroupMembershipEntity genericGroupMembershipEntity = GenericGroupMembershipEntity.builder()
			.userId("userId")
			.genericGroupId(savedGroup.getId())
			.memberSince(LocalDateTime.now())
			.build();
		GenericGroupMembershipEntity genericGroupMembershipEntity1 = GenericGroupMembershipEntity.builder()
			.userId("userId1")
			.genericGroupId(savedGroup.getId())
			.memberSince(LocalDateTime.now())
			.build();
		membershipEntityRepository.save(genericGroupMembershipEntity);
		membershipEntityRepository.save(genericGroupMembershipEntity1);

		Set<GenericGroupEntityWithMembership> allAssignments = entityRepository.findAllAssignments(communityId, savedGroup.getId());

		assertEquals(2, allAssignments.size());
		Iterator<GenericGroupEntityWithMembership> iterator = allAssignments.iterator();

		GenericGroupEntityWithMembership entityWithAssignment = iterator.next();
		assertThat(entityWithAssignment.communityId).isEqualTo(communityId);
		assertThat(entityWithAssignment.userId).isIn("userId", "userId1");
		assertThat(entityWithAssignment.description).isEqualTo("description");
		assertThat(entityWithAssignment.name).isEqualTo("name");

		GenericGroupEntityWithMembership entityWithAssignment2 = iterator.next();
		assertThat(entityWithAssignment2.communityId).isEqualTo(communityId);
		assertThat(entityWithAssignment2.userId).isIn("userId", "userId1");
		assertThat(entityWithAssignment2.description).isEqualTo("description");
		assertThat(entityWithAssignment2.name).isEqualTo("name");
	}


	@Test
	void shouldFindAllAssignmentsByUserId() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		GenericGroupEntity savedGroup = entityRepository.save(genericGroupEntity);

		GenericGroupEntity genericGroupEntity1 = GenericGroupEntity.builder()
			.name("name2")
			.communityId(communityId)
			.description("description2")
			.build();
		GenericGroupEntity save2 = entityRepository.save(genericGroupEntity1);

		GenericGroupMembershipEntity genericGroupMembershipEntity = GenericGroupMembershipEntity.builder()
			.userId("userId")
			.genericGroupId(savedGroup.getId())
			.memberSince(LocalDateTime.now())
			.build();
		GenericGroupMembershipEntity genericGroupMembershipEntity1 = GenericGroupMembershipEntity.builder()
			.userId("userId")
			.genericGroupId(save2.getId())
			.memberSince(LocalDateTime.now())
			.build();
		membershipEntityRepository.save(genericGroupMembershipEntity);
		membershipEntityRepository.save(genericGroupMembershipEntity1);

		Set<GenericGroupEntityWithMembership> allAssignments = entityRepository.findAllAssignments("userId");

		assertEquals(2, allAssignments.size());
		Iterator<GenericGroupEntityWithMembership> iterator = allAssignments.iterator();

		GenericGroupEntityWithMembership entityWithAssignment = iterator.next();
		assertThat(entityWithAssignment.communityId).isEqualTo(communityId);
		assertThat(entityWithAssignment.userId).isEqualTo("userId");
		assertThat(entityWithAssignment.description).isIn("description", "description2");
		assertThat(entityWithAssignment.name).isIn("name", "name2");

		GenericGroupEntityWithMembership entityWithAssignment2 = iterator.next();
		assertThat(entityWithAssignment2.communityId).isEqualTo(communityId);
		assertThat(entityWithAssignment2.userId).isEqualTo("userId");
		assertThat(entityWithAssignment2.description).isIn("description", "description2");
		assertThat(entityWithAssignment2.name).isIn("name", "name2");
	}
}