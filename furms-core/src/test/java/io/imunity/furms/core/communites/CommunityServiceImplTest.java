/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.communities.*;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.InviteUserEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.RemoveUserRoleEvent;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CommunityServiceImplTest {
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private CommunityGroupsDAO communityGroupsDAO;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private AuthzService authzService;

	private CommunityServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		CommunityServiceValidator validator = new CommunityServiceValidator(communityRepository, projectRepository);
		service = new CommunityServiceImpl(communityRepository, communityGroupsDAO, usersDAO, validator, authzService, publisher);
		orderVerifier = inOrder(communityRepository, communityGroupsDAO, publisher);
	}

	@Test
	void shouldReturnCommunityIfExistsInRepository() {
		//given
		String id = "id";
		when(communityRepository.findById(id)).thenReturn(Optional.of(Community.builder()
			.id(id)
			.name("userFacingName")
			.build())
		);

		//when
		Optional<Community> byId = service.findById(id);
		Optional<Community> otherId = service.findById("otherId");

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id);
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllCommunitysIfExistsInRepository() {
		//given
		when(communityRepository.findAll()).thenReturn(Set.of(
			Community.builder().id("id1").name("userFacingName").build(),
			Community.builder().id("id2").name("userFacingName2").build()));

		//when
		Set<Community> allCommunitys = service.findAll();

		//then
		assertThat(allCommunitys).hasSize(2);
	}

	@Test
	void shouldAllowToCreateCommunity() {
		//given
		Community request = Community.builder()
			.id("id")
			.name("userFacingName")
			.build();
		CommunityGroup groupRequest = CommunityGroup.builder()
			.id("id")
			.name("userFacingName")
			.build();
		when(communityRepository.isUniqueName(request.getName())).thenReturn(true);
		when(communityRepository.create(request)).thenReturn("id");

		//when
		service.create(request);

		orderVerifier.verify(communityRepository).create(eq(request));
		orderVerifier.verify(communityGroupsDAO).create(eq(groupRequest));
		orderVerifier.verify(publisher).publishEvent(eq(new CreateCommunityEvent("id")));
	}

	@Test
	void shouldNotAllowToCreateCommunityDueToNonUniqueuserFacingName() {
		//given
		Community request = Community.builder()
			.name("userFacingName")
			.build();
		when(communityRepository.isUniqueName(request.getName())).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new CreateCommunityEvent("id")));
	}

	@Test
	void shouldAllowToUpdateCommunity() {
		//given
		Community request = Community.builder()
			.id("id")
			.name("userFacingName")
			.build();
		CommunityGroup groupRequest = CommunityGroup.builder()
			.id("id")
			.name("userFacingName")
			.build();
		when(communityRepository.exists(request.getId())).thenReturn(true);
		when(communityRepository.isUniqueName(request.getName())).thenReturn(true);

		//when
		service.update(request);

		orderVerifier.verify(communityRepository).update(eq(request));
		orderVerifier.verify(communityGroupsDAO).update(eq(groupRequest));
		orderVerifier.verify(publisher).publishEvent(eq(new UpdateCommunityEvent("id")));
	}

	@Test
	void shouldAllowToDeleteCommunity() {
		//given
		String id = "id";
		when(communityRepository.exists(id)).thenReturn(true);

		//when
		service.delete(id);

		orderVerifier.verify(communityRepository).delete(eq(id));
		orderVerifier.verify(communityGroupsDAO).delete(eq(id));
		orderVerifier.verify(publisher).publishEvent(eq(new RemoveCommunityEvent("id")));
	}

	@Test
	void shouldNotAllowToDeleteCommunityDueToCommunityNotExists() {
		//given
		String id = "id";
		when(communityRepository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new RemoveCommunityEvent("id")));
	}

	@Test
	void shouldReturnAllCommunityAdmins() {
		//given
		String communityId = "id";
		when(communityGroupsDAO.getAllAdmins(communityId)).thenReturn(List.of(new FURMSUser("id", "firstName", "lastName", "email", Map.of())));

		//when
		List<FURMSUser> allAdmins = service.findAllAdmins(communityId);

		//then
		assertThat(allAdmins).hasSize(1);
	}

	@Test
	void shouldAddAdminToCommunity() {
		//given
		String communityId = UUID.randomUUID().toString();
		PersistentId userId = new PersistentId("userId");

		//when
		service.addAdmin(communityId, userId);

		//then
		verify(communityGroupsDAO, times(1)).addAdmin(communityId, userId);
		verify(communityGroupsDAO, times(1)).addAdmin(communityId, userId);
		orderVerifier.verify(publisher).publishEvent(eq(new InviteUserEvent(userId, new ResourceId(communityId, COMMUNITY))));
	}

	@Test
	void shouldRemoveAdminFromCommunity() {
		//given
		String communityId = UUID.randomUUID().toString();
		PersistentId userId = new PersistentId("userId");

		//when
		service.removeAdmin(communityId, userId);

		//then
		verify(communityGroupsDAO, times(1)).removeAdmin(communityId, userId);
		orderVerifier.verify(publisher).publishEvent(eq(new RemoveUserRoleEvent(userId, new ResourceId(communityId, COMMUNITY))));
	}

	@Test
	void shouldThrowExceptionWhenWebClientFailedForRemoveAdmin() {
		//given
		String communityId = UUID.randomUUID().toString();
		PersistentId userId = new PersistentId("userId");
		doThrow(UnityFailureException.class).when(communityGroupsDAO).removeAdmin(communityId, userId);

		//then
		assertThrows(UnityFailureException.class, () -> service.removeAdmin(communityId, userId));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new InviteUserEvent(new PersistentId("id"), new ResourceId(communityId, COMMUNITY))));
	}

	@Test
	void allPublicMethodsShouldBeSecured() {
		Method[] declaredMethods = CommunityServiceImpl.class.getDeclaredMethods();
		Stream.of(declaredMethods)
			.filter(method -> Modifier.isPublic(method.getModifiers()))
			.forEach(method -> {
				assertThat(method.isAnnotationPresent(FurmsAuthorize.class)).isTrue();
				assertThat(method.getAnnotation(FurmsAuthorize.class).resourceType()).isEqualTo(COMMUNITY);
			});
	}
}