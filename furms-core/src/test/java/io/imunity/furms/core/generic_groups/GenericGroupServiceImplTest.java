/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.generic_groups;

import io.imunity.furms.api.validation.exceptions.GroupNotBelongingToCommunityException;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupAssignmentWithUser;
import io.imunity.furms.domain.generic_groups.GenericGroupCreateEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.generic_groups.GenericGroupMembership;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenericGroupServiceImplTest {

	private final static Instant LOCAL_DATE = Instant.now();

	@Mock
	private GenericGroupRepository genericGroupRepository;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private ApplicationEventPublisher publisher;

	private Clock fixedClock;

	private GenericGroupServiceImpl genericGroupService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		fixedClock = Clock.fixed(LOCAL_DATE, ZoneId.systemDefault());
		genericGroupService = new GenericGroupServiceImpl(genericGroupRepository, usersDAO, fixedClock, publisher);
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

		assertThrows(GroupNotBelongingToCommunityException.class, () -> genericGroupService.findBy("communityId2", genericGroupId));
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
		GenericGroupMembership genericGroupMembership = GenericGroupMembership.builder()
			.genericGroupId(genericGroupId)
			.fenixUserId("userId")
			.build();

		when(genericGroupRepository.existsBy("communityId", genericGroupId)).thenReturn(true);
		when(genericGroupRepository.findAllBy(genericGroupId)).thenReturn(Set.of(genericGroupMembership));
		when(usersDAO.getAllUsers()).thenReturn(List.of(furmsUser));

		Set<GenericGroupAssignmentWithUser> genericGroupServiceAll = genericGroupService.findAll("communityId", genericGroupId);
		assertEquals(1, genericGroupServiceAll.size());
		GenericGroupAssignmentWithUser next = genericGroupServiceAll.iterator().next();
		assertEquals(furmsUser, next.furmsUser);
		assertEquals(genericGroupMembership, next.membership);
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
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		FenixUserId userId = new FenixUserId("fenixUserId");

		when(genericGroupRepository.existsBy("communityId", genericGroupId)).thenReturn(true);

		genericGroupService.createMembership("communityId", genericGroupId, userId);

		verify(genericGroupRepository).createMembership(GenericGroupMembership.builder()
			.genericGroupId(genericGroupId)
			.fenixUserId(userId)
			.utcMemberSince(ZonedDateTime.now(fixedClock).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime())
			.build()
		);
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
		GenericGroupId groupId = new GenericGroupId(UUID.randomUUID());
		FenixUserId userId = new FenixUserId("userId");

		when(genericGroupRepository.existsBy("communityId", groupId)).thenReturn(true);

		genericGroupService.deleteMembership("communityId", groupId, userId);

		verify(genericGroupRepository).deleteMembership(groupId, userId);
	}
}