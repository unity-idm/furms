/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.api.authz.FURMSUserProvider;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.ResourceType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class AuthTest {

	@Autowired
	private ServiceMock mockService;

	@MockBean
	AuthenticationManager authenticationManager;

	@MockBean
	ProjectRepository projectRepository;

	@Mock
	Authentication authentication;

	@Mock
	FURMSUserProvider provider;

	@BeforeEach
	void configSecurityContext(){
		when(authentication.getPrincipal()).thenReturn(provider);
		when(authentication.isAuthenticated()).thenReturn(true);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	void authShouldPassFenixAdminHasAccessToAPPLevel(){
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId((String) null, APP_LEVEL), Set.of(Role.FENIX_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		Throwable throwable = catchThrowable(() -> mockService.findAll());
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldPassUserHasAccessToProjectLimitedOperations(){
		UUID uuid = UUID.randomUUID();
		Map<ResourceId, Set<Role>> roles = Map.of();
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		Throwable throwable = catchThrowable(() -> mockService.getLimitedProject(uuid.toString()));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldPassFenixAdminHasAccessToResource(){
		UUID uuid = UUID.randomUUID();
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(uuid, APP_LEVEL), Set.of(Role.FENIX_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		Throwable throwable = catchThrowable(() -> mockService.findById(uuid.toString()));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldPassFenixAdminHasSpecialAdminRights(){
		UUID uuid = UUID.randomUUID();
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(uuid, APP_LEVEL), Set.of(Role.FENIX_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		Throwable throwable = catchThrowable(() -> mockService.getCommunity(uuid.toString()));
		assertThat(throwable).isNull();
	}


	@Test
	void authShouldNotPassWrongResourceId(){
		UUID uuid = UUID.randomUUID();
		UUID uuid1 = UUID.randomUUID();
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(uuid, APP_LEVEL), Set.of(Role.FENIX_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		assertThrows(AccessDeniedException.class, () -> mockService.findById(uuid1.toString()));
	}

	@Test
	void authShouldNotPassWrongResourceType(){
		UUID uuid = UUID.randomUUID();
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(uuid, PROJECT), Set.of(Role.FENIX_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		assertThrows(AccessDeniedException.class, () -> mockService.findById(uuid.toString()));
	}

	@Test
	void authShouldNotPassWrongRole(){
		UUID uuid = UUID.randomUUID();
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(uuid, COMMUNITY), Set.of(Role.SITE_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		assertThrows(AccessDeniedException.class, () -> mockService.getCommunity(uuid.toString()));
	}

	@Test
	void authShouldNotPassCommunityAdminDoesntOwnProject(){
		UUID uuid = UUID.randomUUID();
		UUID uuid2 = UUID.randomUUID();
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(uuid, COMMUNITY), Set.of(Role.COMMUNITY_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();
		Project project = Project.builder().id(uuid2.toString()).build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);
		when(projectRepository.findAllByCommunityId(uuid.toString())).thenReturn(Set.of(project));

		assertThrows(AccessDeniedException.class, () -> mockService.getProject(UUID.randomUUID().toString()));
	}

	@Test
	void authShouldPassCommunityAdminOwnProject(){
		UUID communityUUID = UUID.randomUUID();
		UUID projectUUID = UUID.randomUUID();
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(communityUUID, COMMUNITY), Set.of(Role.COMMUNITY_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();
		Project project = Project.builder().id(projectUUID.toString()).build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);
		when(projectRepository.findAllByCommunityId(communityUUID.toString())).thenReturn(Set.of(project));

		Throwable throwable = catchThrowable(() -> mockService.getProject(projectUUID.toString()));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldNotPassCommunityAdminHasNotAccessToAppLevel(){
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId((String) null, APP_LEVEL), Set.of(Role.COMMUNITY_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		assertThrows(AccessDeniedException.class, () -> mockService.findAllWithClassScopeAuthorization());
	}
}