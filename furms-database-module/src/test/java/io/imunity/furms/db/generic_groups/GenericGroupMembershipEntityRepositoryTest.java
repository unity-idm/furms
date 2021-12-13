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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GenericGroupMembershipEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private CommunityRepository communityRepository;

	@Autowired
	private GenericGroupEntityRepository entityRepository;
	@Autowired
	private GenericGroupMembershipEntityRepository membershipEntityRepository;

	private UUID communityId;
	private UUID communityId2;
	private UUID groupId;
	private UUID groupId2;

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

		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();

		groupId = entityRepository.save(genericGroupEntity).getId();

		GenericGroupEntity genericGroupEntity2 = GenericGroupEntity.builder()
			.name("name2")
			.communityId(communityId)
			.description("description2")
			.build();

		groupId2 = entityRepository.save(genericGroupEntity2).getId();
	}

	@Test
	void shouldSave(){
		GenericGroupMembershipEntity membershipEntity = GenericGroupMembershipEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupMembershipEntity savedAssignment = membershipEntityRepository.save(membershipEntity);

		GenericGroupMembershipEntity foundAssignment = membershipEntityRepository.findById(membershipEntity.getId()).get();

		assertEquals(savedAssignment, foundAssignment);
	}

	@Test
	void shouldDelete(){
		GenericGroupMembershipEntity membershipEntity = GenericGroupMembershipEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDateTime.now())
			.build();
		GenericGroupMembershipEntity savedAssignment = membershipEntityRepository.save(membershipEntity);

		membershipEntityRepository.deleteById(savedAssignment.getId());

		Optional<GenericGroupMembershipEntity> groupAssignmentEntity = membershipEntityRepository.findById(membershipEntity.getId());

		assertTrue(groupAssignmentEntity.isEmpty());
	}

	@Test
	void shouldUpdate(){
		GenericGroupMembershipEntity membershipEntity = GenericGroupMembershipEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupMembershipEntity savedAssignment = membershipEntityRepository.save(membershipEntity);

		GenericGroupMembershipEntity membershipEntity1 = GenericGroupMembershipEntity.builder()
			.id(savedAssignment.getId())
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDate.now().atStartOfDay().plusDays(2))
			.build();
		GenericGroupMembershipEntity updatedAssignment = membershipEntityRepository.save(membershipEntity1);

		GenericGroupMembershipEntity foundAssignment = membershipEntityRepository.findById(savedAssignment.getId()).get();

		assertEquals(updatedAssignment, foundAssignment);
	}

	@Test
	void shouldFindAllByGenericGroupId(){
		GenericGroupMembershipEntity membershipEntity = GenericGroupMembershipEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupMembershipEntity savedAssignment = membershipEntityRepository.save(membershipEntity);

		GenericGroupMembershipEntity membershipEntity1 = GenericGroupMembershipEntity.builder()
			.userId("userId1")
			.genericGroupId(groupId)
			.memberSince(LocalDate.now().atStartOfDay().plusDays(1))
			.build();
		GenericGroupMembershipEntity savedAssignment1 = membershipEntityRepository.save(membershipEntity1);


		GenericGroupMembershipEntity membershipEntity2 = GenericGroupMembershipEntity.builder()
			.userId("userId")
			.genericGroupId(groupId2)
			.memberSince(LocalDate.now().atStartOfDay().plusDays(2))
			.build();
		membershipEntityRepository.save(membershipEntity2);


		Set<GenericGroupMembershipEntity> genericGroupAssignmentEntities = membershipEntityRepository.findAllByGenericGroupId(groupId);
		assertEquals(2, genericGroupAssignmentEntities.size());
		assertEquals(Set.of(savedAssignment, savedAssignment1), genericGroupAssignmentEntities);
	}

	@Test
	void shouldFindByCommunityIdAndId(){
		GenericGroupMembershipEntity membershipEntity = GenericGroupMembershipEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupMembershipEntity savedAssignment = membershipEntityRepository.save(membershipEntity);

		GenericGroupMembershipEntity groupAssignmentEntity = membershipEntityRepository.findByCommunityIdAndId(communityId, savedAssignment.getId()).get();

		assertEquals(savedAssignment, groupAssignmentEntity);
	}

	@Test
	void shouldNotFindByCommunityIdAndId(){
		GenericGroupMembershipEntity membershipEntity = GenericGroupMembershipEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDateTime.now())
			.build();
		GenericGroupMembershipEntity savedAssignment = membershipEntityRepository.save(membershipEntity);

		Optional<GenericGroupMembershipEntity> byCommunityIdAndId = membershipEntityRepository.findByCommunityIdAndId(communityId2, savedAssignment.getId());

		assertTrue(byCommunityIdAndId.isEmpty());
	}

	@Test
	void shouldExistByGenericGroupIdAndUserId(){
		GenericGroupMembershipEntity membershipEntity = GenericGroupMembershipEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDateTime.now())
			.build();
		membershipEntityRepository.save(membershipEntity);

		boolean existsByGenericGroupIdAndUserId = membershipEntityRepository.existsByGenericGroupIdAndUserId(groupId, "userId");
		assertTrue(existsByGenericGroupIdAndUserId);
	}

	@Test
	void shouldNotExistByGenericGroupIdAndUserId(){
		GenericGroupMembershipEntity membershipEntity = GenericGroupMembershipEntity.builder()
			.userId("userId1")
			.genericGroupId(groupId)
			.memberSince(LocalDateTime.now())
			.build();
		membershipEntityRepository.save(membershipEntity);

		boolean existsByGenericGroupIdAndUserId = membershipEntityRepository.existsByGenericGroupIdAndUserId(groupId, "userId");
		assertFalse(existsByGenericGroupIdAndUserId);
	}
}