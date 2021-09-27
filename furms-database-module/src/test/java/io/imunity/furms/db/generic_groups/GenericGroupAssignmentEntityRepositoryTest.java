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
class GenericGroupAssignmentEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private CommunityRepository communityRepository;

	@Autowired
	private GenericGroupEntityRepository entityRepository;
	@Autowired
	private GenericGroupAssignmentEntityRepository assignmentEntityRepository;

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
		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		GenericGroupAssignmentEntity foundAssignment = assignmentEntityRepository.findById(assignmentEntity.getId()).get();

		assertEquals(savedAssignment, foundAssignment);
	}

	@Test
	void shouldDelete(){
		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDateTime.now())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		assignmentEntityRepository.deleteById(savedAssignment.getId());

		Optional<GenericGroupAssignmentEntity> groupAssignmentEntity = assignmentEntityRepository.findById(assignmentEntity.getId());

		assertTrue(groupAssignmentEntity.isEmpty());
	}

	@Test
	void shouldUpdate(){
		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		GenericGroupAssignmentEntity assignmentEntity1 = GenericGroupAssignmentEntity.builder()
			.id(savedAssignment.getId())
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDate.now().atStartOfDay().plusDays(2))
			.build();
		GenericGroupAssignmentEntity updatedAssignment = assignmentEntityRepository.save(assignmentEntity1);

		GenericGroupAssignmentEntity foundAssignment = assignmentEntityRepository.findById(savedAssignment.getId()).get();

		assertEquals(updatedAssignment, foundAssignment);
	}

	@Test
	void shouldFindAllByGenericGroupId(){
		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		GenericGroupAssignmentEntity assignmentEntity1 = GenericGroupAssignmentEntity.builder()
			.userId("userId1")
			.genericGroupId(groupId)
			.memberSince(LocalDate.now().atStartOfDay().plusDays(1))
			.build();
		GenericGroupAssignmentEntity savedAssignment1 = assignmentEntityRepository.save(assignmentEntity1);


		GenericGroupAssignmentEntity assignmentEntity2 = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(groupId2)
			.memberSince(LocalDate.now().atStartOfDay().plusDays(2))
			.build();
		GenericGroupAssignmentEntity savedAssignment2 = assignmentEntityRepository.save(assignmentEntity2);


		Set<GenericGroupAssignmentEntity> genericGroupAssignmentEntities = assignmentEntityRepository.findAllByGenericGroupId(groupId);
		assertEquals(2, genericGroupAssignmentEntities.size());
		assertEquals(Set.of(savedAssignment, savedAssignment1), genericGroupAssignmentEntities);
	}

	@Test
	void shouldFindByCommunityIdAndId(){
		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		GenericGroupAssignmentEntity groupAssignmentEntity = assignmentEntityRepository.findByCommunityIdAndId(communityId, savedAssignment.getId()).get();

		assertEquals(savedAssignment, groupAssignmentEntity);
	}

	@Test
	void shouldNotFindByCommunityIdAndId(){
		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDateTime.now())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		Optional<GenericGroupAssignmentEntity> byCommunityIdAndId = assignmentEntityRepository.findByCommunityIdAndId(communityId2, savedAssignment.getId());

		assertTrue(byCommunityIdAndId.isEmpty());
	}

	@Test
	void shouldExistByGenericGroupIdAndUserId(){
		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(groupId)
			.memberSince(LocalDateTime.now())
			.build();
		assignmentEntityRepository.save(assignmentEntity);

		boolean existsByGenericGroupIdAndUserId = assignmentEntityRepository.existsByGenericGroupIdAndUserId(groupId, "userId");
		assertTrue(existsByGenericGroupIdAndUserId);
	}

	@Test
	void shouldNotExistByGenericGroupIdAndUserId(){
		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId1")
			.genericGroupId(groupId)
			.memberSince(LocalDateTime.now())
			.build();
		assignmentEntityRepository.save(assignmentEntity);

		boolean existsByGenericGroupIdAndUserId = assignmentEntityRepository.existsByGenericGroupIdAndUserId(groupId, "userId");
		assertFalse(existsByGenericGroupIdAndUserId);
	}
}