/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignment;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignmentId;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignmentAmount;
import io.imunity.furms.domain.generic_groups.GenericGroupWithAssignments;
import io.imunity.furms.domain.generic_groups.GroupAccess;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GenericGroupDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private GenericGroupEntityRepository entityRepository;
	@Autowired
	private GenericGroupAssignmentEntityRepository assignmentEntityRepository;

	@Autowired
	private GenericGroupDatabaseRepository databaseRepository;

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
	void shouldFindByGenericGroupId() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);

		GenericGroup genericGroup = databaseRepository.findBy(new GenericGroupId(saved.getId())).get();

		assertEquals(saved.getId(), genericGroup.id.id);
		assertEquals(saved.communityId, saved.communityId);
		assertEquals(saved.name, saved.name);
		assertEquals(saved.description, saved.description);
	}

	@Test
	void shouldFindGroupWithAssignments() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);

		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(saved.getId())
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		GenericGroupAssignmentEntity assignmentEntity1 = GenericGroupAssignmentEntity.builder()
			.userId("userId1")
			.genericGroupId(saved.getId())
			.memberSince(LocalDate.now().atStartOfDay().plusDays(2))
			.build();
		GenericGroupAssignmentEntity savedAssignment1 = assignmentEntityRepository.save(assignmentEntity1);


		GenericGroupWithAssignments groupWithAssignments = databaseRepository.findGroupWithAssignments(communityId.toString(), new GenericGroupId(saved.getId())).get();

		assertThat(groupWithAssignments.group.id.id).isEqualTo(saved.getId());
		assertEquals(groupWithAssignments.group.communityId, communityId.toString());
		assertThat(groupWithAssignments.group.name).isEqualTo("name");
		assertThat(groupWithAssignments.group.description).isEqualTo("description");
		assertEquals(2, groupWithAssignments.assignments.size());
	}

	@Test
	void shouldFindAllGroupWithAssignmentsAmount() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);
		GenericGroupEntity saved2 = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name2")
			.description("description2")
			.build()
		);
		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(saved.getId())
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		GenericGroupAssignmentEntity assignmentEntity1 = GenericGroupAssignmentEntity.builder()
			.userId("userId1")
			.genericGroupId(saved.getId())
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment1 = assignmentEntityRepository.save(assignmentEntity1);

		GenericGroupAssignmentEntity assignmentEntity2 = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(saved2.getId())
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment2 = assignmentEntityRepository.save(assignmentEntity2);

		GenericGroupAssignmentEntity assignmentEntity3 = GenericGroupAssignmentEntity.builder()
			.userId("userId1")
			.genericGroupId(saved2.getId())
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment3 = assignmentEntityRepository.save(assignmentEntity3);


		Set<GenericGroupWithAssignmentAmount> groupsWithAssignmentsAmount = databaseRepository.findAllGroupWithAssignmentsAmount(communityId.toString());

		assertEquals(2, groupsWithAssignmentsAmount.size());
		Iterator<GenericGroupWithAssignmentAmount> iterator = groupsWithAssignmentsAmount.iterator();

		GenericGroupWithAssignmentAmount genericGroup = iterator.next();
		assertThat(genericGroup.group.id.id).isIn(saved.getId(), saved2.getId());
		assertEquals(genericGroup.group.communityId, communityId.toString());
		assertThat(genericGroup.group.name).isIn("name", "name2");
		assertThat(genericGroup.group.description).isIn("description", "description2");
		assertEquals(2, genericGroup.amount);

		GenericGroupWithAssignmentAmount genericGroup2 = iterator.next();
		assertThat(genericGroup2.group.id.id).isIn(saved.getId(), saved2.getId());
		assertEquals(genericGroup2.group.communityId, communityId.toString());
		assertThat(genericGroup2.group.name).isIn("name", "name2");
		assertThat(genericGroup2.group.description).isIn("description", "description2");
		assertEquals(2, genericGroup2.amount);
	}

	@Test
	void shouldFindAllByCommunityId() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);
		GenericGroupEntity saved2 = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name2")
			.description("description2")
			.build()
		);
		Set<GenericGroup> genericGroups = databaseRepository.findAllBy(communityId.toString());

		assertEquals(2, genericGroups.size());
		GenericGroup genericGroup = genericGroups.iterator().next();
		assertThat(genericGroup.id.id).isIn(saved.getId(), saved2.getId());
		assertEquals(saved.communityId.toString(), genericGroup.communityId);
		assertThat(genericGroup.name).isIn("name", "name2");
		assertThat(genericGroup.description).isIn("description", "description2");

		GenericGroup genericGroup1 = genericGroups.iterator().next();
		assertThat(genericGroup1.id.id).isIn(saved.getId(), saved2.getId());
		assertEquals(saved2.communityId.toString(), genericGroup1.communityId);
		assertThat(genericGroup1.name).isIn("name", "name2");
		assertThat(genericGroup1.description).isIn("description", "description2");
	}

	@Test
	void shouldFindAllByGenericGroup() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);

		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(saved.getId())
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		GenericGroupAssignmentEntity assignmentEntity1 = GenericGroupAssignmentEntity.builder()
			.userId("userId1")
			.genericGroupId(saved.getId())
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment1 = assignmentEntityRepository.save(assignmentEntity1);

		Set<GenericGroupAssignment> groupAssignments = databaseRepository.findAllBy(new GenericGroupId(saved.getId()));

		assertEquals(2, groupAssignments.size());
		Iterator<GenericGroupAssignment> iterator = groupAssignments.iterator();
		GenericGroupAssignment groupAssignment = iterator.next();
		assertThat(groupAssignment.id.id).isIn(savedAssignment.getId(), savedAssignment1.getId());
		assertThat(groupAssignment.fenixUserId.id).isIn("userId", "userId1");
		assertThat(groupAssignment.utcMemberSince).isIn(assignmentEntity.memberSince, assignmentEntity1.memberSince);

		GenericGroupAssignment groupAssignment2 = iterator.next();
		assertThat(groupAssignment2.id.id).isIn(savedAssignment.getId(), savedAssignment1.getId());
		assertThat(groupAssignment2.fenixUserId.id).isIn("userId", "userId1");
		assertThat(groupAssignment2.utcMemberSince).isIn(assignmentEntity.memberSince, assignmentEntity1.memberSince);
	}

	@Test
	void shouldFindAllByFenixUserId() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);
		GenericGroupEntity saved2 = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name2")
			.description("description")
			.build()
		);

		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(saved.getId())
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		GenericGroupAssignmentEntity assignmentEntity1 = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(saved2.getId())
			.memberSince(LocalDate.now().atStartOfDay().plusDays(2))
			.build();
		GenericGroupAssignmentEntity savedAssignment1 = assignmentEntityRepository.save(assignmentEntity1);


		Set<GroupAccess> groupAccesses = databaseRepository.findAllBy(new FenixUserId("userId"));
		assertEquals(1, groupAccesses.size());
		GroupAccess next = groupAccesses.iterator().next();
		assertEquals(communityId.toString(), next.communityId);
		assertThat(next.groups).isEqualTo(Set.of("name", "name2"));
	}

	@Test
	void shouldCreateGenericGroup() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		GenericGroupEntity savedGroup = entityRepository.save(genericGroupEntity);

		GenericGroupAssignment assignmentEntity = GenericGroupAssignment.builder()
			.fenixUserId("userId")
			.genericGroupId(savedGroup.getId())
			.utcMemberSince(LocalDate.now().atStartOfDay())
			.build();

		GenericGroupAssignmentId assignmentId = databaseRepository.create(assignmentEntity);

		GenericGroupAssignmentEntity groupAssignmentEntity = assignmentEntityRepository.findById(assignmentId.id).get();

		assertEquals(assignmentId.id, groupAssignmentEntity.getId());
		assertEquals(assignmentEntity.genericGroupId.id, groupAssignmentEntity.genericGroupId);
		assertEquals(assignmentEntity.fenixUserId.id, groupAssignmentEntity.userId);
		assertEquals(assignmentEntity.utcMemberSince, groupAssignmentEntity.memberSince);
	}

	@Test
	void shouldCreateGenericGroupAssignment() {
		GenericGroup genericGroupEntity = GenericGroup.builder()
			.name("name")
			.communityId(communityId.toString())
			.description("description")
			.build();

		GenericGroupId groupId = databaseRepository.create(genericGroupEntity);

		GenericGroup group = databaseRepository.findBy(groupId).get();

		assertEquals(groupId, group.id);
		assertEquals(genericGroupEntity.communityId, communityId.toString());
		assertEquals(genericGroupEntity.name, "name");
		assertEquals(genericGroupEntity.description, "description");
	}

	@Test
	void shouldUpdateGenericGroup() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		GenericGroupEntity savedGroup = entityRepository.save(genericGroupEntity);

		GenericGroup genericGroupToUpdate = GenericGroup.builder()
			.id(savedGroup.getId())
			.name("name")
			.communityId(communityId.toString())
			.description("description")
			.build();

		databaseRepository.update(genericGroupToUpdate);

		GenericGroup group = databaseRepository.findBy(genericGroupToUpdate.id).get();

		assertEquals(genericGroupToUpdate, group);
	}

	@Test
	void shouldDeleteGenericGroup() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		GenericGroupEntity savedGroup = entityRepository.save(genericGroupEntity);

		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(savedGroup.getId())
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		databaseRepository.delete(new GenericGroupAssignmentId(savedAssignment.getId()));

		Optional<GenericGroupAssignmentEntity> found = assignmentEntityRepository.findById(savedGroup.getId());

		assertTrue(found.isEmpty());
	}

	@Test
	void shouldDeleteGenericGroupAssignment() {
		GenericGroupEntity genericGroupEntity = GenericGroupEntity.builder()
			.name("name")
			.communityId(communityId)
			.description("description")
			.build();
		GenericGroupEntity savedGroup = entityRepository.save(genericGroupEntity);

		databaseRepository.delete(new GenericGroupId(genericGroupEntity.getId()));

		Optional<GenericGroupEntity> found = entityRepository.findById(savedGroup.getId());

		assertTrue(found.isEmpty());
	}

	@Test
	void shouldExistByCommunityIdAndGroupId() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);

		boolean existsBy = databaseRepository.existsBy(communityId.toString(), new GenericGroupId(saved.getId()));

		assertTrue(existsBy);
	}

	@Test
	void shouldNotExistByCommunityIdAndGroupId() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);

		boolean existsBy = databaseRepository.existsBy(communityId2.toString(), new GenericGroupId(saved.getId()));

		assertFalse(existsBy);
	}

	@Test
	void shouldExistByCommunityIdAndGroupAssignmentId() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);

		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(saved.getId())
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		boolean existsBy = databaseRepository.existsBy(communityId.toString(), new GenericGroupAssignmentId(assignmentEntity.getId()));

		assertTrue(existsBy);
	}

	@Test
	void shouldNotExistByCommunityIdAndGroupAssignmentId() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);

		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(saved.getId())
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		boolean existsBy = databaseRepository.existsBy(communityId2.toString(), new GenericGroupAssignmentId(assignmentEntity.getId()));

		assertFalse(existsBy);
	}

	@Test
	void shouldExistByGenericGroupIdAndUserId() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);

		GenericGroupAssignmentEntity assignmentEntity = GenericGroupAssignmentEntity.builder()
			.userId("userId")
			.genericGroupId(saved.getId())
			.memberSince(LocalDate.now().atStartOfDay())
			.build();
		GenericGroupAssignmentEntity savedAssignment = assignmentEntityRepository.save(assignmentEntity);

		boolean existsBy = databaseRepository.existsBy(new GenericGroupId(saved.getId()), new FenixUserId("userId"));

		assertTrue(existsBy);
	}

	@Test
	void shouldNotExistByGenericGroupIdAndUserId() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);

		boolean existsBy = databaseRepository.existsBy(new GenericGroupId(saved.getId()), new FenixUserId("userId"));

		assertFalse(existsBy);
	}

	@Test
	void shouldExistByCommunityIdAndName() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("description")
			.build()
		);

		boolean existsBy = databaseRepository.existsBy(communityId.toString(), "name");

		assertTrue(existsBy);
	}

	@Test
	void shouldNotExistByCommunityIdAndName() {
		GenericGroupEntity saved = entityRepository.save(GenericGroupEntity.builder()
			.communityId(communityId)
			.name("name2")
			.description("description")
			.build()
		);

		boolean existsBy = databaseRepository.existsBy(communityId.toString(), "name");

		assertFalse(existsBy);
	}
}