/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.communities.CreateCommunityEvent;
import io.imunity.furms.domain.communities.RemoveCommunityEvent;
import io.imunity.furms.domain.communities.UpdateCommunityEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.RemoveUserRoleEvent;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityServiceImplTest {
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private CommunityGroupsDAO communityGroupsDAO;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private AuthzService authzService;
	@Mock
	private CapabilityCollector capabilityCollector;
	@Mock
	private InvitatoryService invitatoryService;

	private CommunityServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		CommunityServiceValidator validator = new CommunityServiceValidator(communityRepository, projectRepository);
		service = new CommunityServiceImpl(communityRepository, communityGroupsDAO, validator, authzService,
				publisher, capabilityCollector, invitatoryService);
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
		when(communityGroupsDAO.getAllAdmins(communityId)).thenReturn(List.of(
			FURMSUser.builder()
				.id(new PersistentId("id"))
				.firstName("firstName")
				.lastName("lastName")
				.email("email")
				.build())
		);

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
	}

	@Test
	void allPublicMethodsShouldBeSecured() {
		Method[] declaredMethods = CommunityServiceImpl.class.getDeclaredMethods();
		Stream.of(declaredMethods)
			.filter(method -> Modifier.isPublic(method.getModifiers()))
			.forEach(method -> assertThat(method.isAnnotationPresent(FurmsAuthorize.class)).isTrue());
	}
}