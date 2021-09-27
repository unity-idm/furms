/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.generic_groups;

import io.imunity.furms.api.validation.exceptions.GroupNotBelongToCommunityError;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignment;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignmentId;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignmentWithUser;
import io.imunity.furms.domain.generic_groups.GenericGroupCreateEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.generic_groups.GenericGroupRemoveEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupUpdateEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.generic_groups.GenericGroupRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenericGroupServiceImplTest {

	@Mock
	private GenericGroupRepository genericGroupRepository;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private ApplicationEventPublisher publisher;

	private GenericGroupServiceImpl genericGroupService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		genericGroupService = new GenericGroupServiceImpl(genericGroupRepository, usersDAO, publisher);
	}

	@Test
	void shouldFindAll() {
		genericGroupService.findAll("communityId");
		Mockito.verify(genericGroupRepository).findAllBy("communityId");
	}

	@Test
	void shouldFindGroupWithAssignments() {
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		genericGroupService.findGroupWithAssignments("communityId", genericGroupId);
		Mockito.verify(genericGroupRepository).findGroupWithAssignments("communityId", genericGroupId);
	}

	@Test
	void shouldFindBy() {
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		when(genericGroupRepository.existsBy("communityId", genericGroupId)).thenReturn(true);

		genericGroupService.findBy("communityId", genericGroupId);
		Mockito.verify(genericGroupRepository).findBy(genericGroupId);
	}

	@Test
	void shouldNotFindBy() {
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		when(genericGroupRepository.existsBy("communityId", genericGroupId)).thenReturn(false);

		assertThrows(GroupNotBelongToCommunityError.class, () -> genericGroupService.findBy("communityId2", genericGroupId));
	}

	@Test
	void shouldFindAllGroupWithAssignmentsAmount() {
		genericGroupService.findAllGroupWithAssignmentsAmount("communityId");
		Mockito.verify(genericGroupRepository).findAllGroupWithAssignmentsAmount("communityId");
	}

	@Test
	void shouldFindAllGenericGroupAssignmentWithUser() {
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		FURMSUser furmsUser = FURMSUser.builder()
			.email("email")
			.fenixUserId(new FenixUserId("userId"))
			.build();
		GenericGroupAssignment genericGroupAssignment = GenericGroupAssignment.builder()
			.genericGroupId(genericGroupId)
			.fenixUserId("userId")
			.build();

		when(genericGroupRepository.existsBy("communityId", genericGroupId)).thenReturn(true);
		when(genericGroupRepository.findAllBy(genericGroupId)).thenReturn(Set.of(genericGroupAssignment));
		when(usersDAO.getAllUsers()).thenReturn(List.of(furmsUser));

		Set<GenericGroupAssignmentWithUser> genericGroupServiceAll = genericGroupService.findAll("communityId", genericGroupId);
		assertEquals(1, genericGroupServiceAll.size());
		GenericGroupAssignmentWithUser next = genericGroupServiceAll.iterator().next();
		assertEquals(furmsUser, next.furmsUser);
		assertEquals(genericGroupAssignment, next.assignment);
	}

	@Test
	void shouldCreateGroup() {
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		GenericGroup genericGroup = GenericGroup.builder()
			.communityId("communityId")
			.name("name")
			.description("description")
			.build();

		when(genericGroupRepository.existsBy("communityId", "name")).thenReturn(false);
		when(genericGroupRepository.create(genericGroup)).thenReturn(genericGroupId);

		GenericGroupId createdGenericGroupId = genericGroupService.create(genericGroup);

		assertEquals(genericGroupId, createdGenericGroupId);
		verify(publisher).publishEvent(new GenericGroupCreateEvent(genericGroupId));
	}

	@Test
	void shouldCreateAssignment() {
		GenericGroupAssignmentId genericGroupAssignmentId1 = new GenericGroupAssignmentId(UUID.randomUUID());
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		GenericGroupAssignment genericGroupAssignment = GenericGroupAssignment.builder()
			.fenixUserId("fenixUserId")
			.genericGroupId(genericGroupId)
			.utcMemberSince(LocalDate.now().atStartOfDay())
			.build();

		when(genericGroupRepository.existsBy("communityId", genericGroupId)).thenReturn(true);
		when(genericGroupRepository.create(genericGroupAssignment)).thenReturn(genericGroupAssignmentId1);

		GenericGroupAssignmentId genericGroupAssignmentId = genericGroupService.create("communityId", genericGroupAssignment);

		assertEquals(genericGroupAssignmentId1, genericGroupAssignmentId);
	}

	@Test
	void update() {
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		GenericGroup genericGroup = GenericGroup.builder()
			.id(genericGroupId)
			.communityId("communityId")
			.name("name")
			.description("description")
			.build();

		when(genericGroupRepository.existsBy("communityId", "name")).thenReturn(false);
		when(genericGroupRepository.create(genericGroup)).thenReturn(genericGroupId);

		genericGroupService.update(genericGroup);

		verify(genericGroupRepository).update(genericGroup);
		verify(publisher).publishEvent(new GenericGroupUpdateEvent(genericGroupId));
	}

	@Test
	void deleteGenericGroup() {
		GenericGroupId groupId = new GenericGroupId(UUID.randomUUID());
		when(genericGroupRepository.existsBy("communityId", groupId)).thenReturn(true);


		genericGroupService.delete("communityId", groupId);

		verify(genericGroupRepository).delete(groupId);
		verify(publisher).publishEvent(new GenericGroupRemoveEvent(groupId));
	}

	@Test
	void deleteGenericGroupAssignment() {
		GenericGroupAssignmentId assignmentId = new GenericGroupAssignmentId(UUID.randomUUID());
		when(genericGroupRepository.existsBy("communityId", assignmentId)).thenReturn(true);

		genericGroupService.delete("communityId", assignmentId);

		verify(genericGroupRepository).delete(assignmentId);
	}
}