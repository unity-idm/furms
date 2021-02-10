/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.users.User;
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

	private CommunityServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		CommunityServiceValidator validator = new CommunityServiceValidator(communityRepository, projectRepository);
		service = new CommunityServiceImpl(communityRepository, communityGroupsDAO, usersDAO, validator);
		orderVerifier = inOrder(communityRepository, communityGroupsDAO);
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
	}

	@Test
	void shouldNotAllowToDeleteCommunityDueToCommunityNotExists() {
		//given
		String id = "id";
		when(communityRepository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id));
	}

	@Test
	void shouldReturnAllCommunityAdmins() {
		//given
		String communityId = "id";
		when(communityGroupsDAO.getAllAdmins(communityId)).thenReturn(List.of(new User("id", "firstName", "lastName", "email")));

		//when
		List<User> allAdmins = service.findAllAdmins(communityId);

		//then
		assertThat(allAdmins).hasSize(1);
	}

	@Test
	void shouldAddAdminToCommunity() {
		//given
		String communityId = "communityId";
		String userId = "userId";

		//when
		service.addAdmin(communityId, userId);

		//then
		verify(communityGroupsDAO, times(1)).addAdmin(communityId, userId);
	}

	@Test
	void shouldRemoveAdminFromCommunity() {
		//given
		String communityId = "communityId";
		String userId = "userId";

		//when
		service.removeAdmin(communityId, userId);

		//then
		verify(communityGroupsDAO, times(1)).removeAdmin(communityId, userId);
	}

	@Test
	void shouldThrowExceptionWhenWebClientFailedForRemoveAdmin() {
		//given
		String communityId = "communityId";
		String userId = "userId";
		doThrow(UnityFailureException.class).when(communityGroupsDAO).removeAdmin(communityId, userId);

		//then
		assertThrows(UnityFailureException.class, () -> service.removeAdmin(communityId, userId));
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