/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.api.authz.FURMSUserProvider;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
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
			new ResourceId(null, APP_LEVEL), Set.of(Role.FENIX_ADMIN)
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
		ProjectId uuid = new ProjectId(UUID.randomUUID());
		Map<ResourceId, Set<Role>> roles = Map.of();
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		Throwable throwable = catchThrowable(() -> mockService.getLimitedProject(uuid));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldPassFenixAdminHasAccessToResource(){
		UUID uuid = UUID.randomUUID();
		SiteId siteId = new SiteId(uuid);
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(siteId, APP_LEVEL), Set.of(Role.FENIX_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		Throwable throwable = catchThrowable(() -> mockService.findById(siteId));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldPassFenixAdminHasSpecialAdminRights(){
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(communityId, APP_LEVEL), Set.of(Role.FENIX_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		Throwable throwable = catchThrowable(() -> mockService.getCommunity(communityId));
		assertThat(throwable).isNull();
	}


	@Test
	void authShouldNotPassWrongResourceId(){
		UUID uuid = UUID.randomUUID();
		UUID uuid1 = UUID.randomUUID();
		SiteId siteId = new SiteId(uuid1);
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(new SiteId(uuid), APP_LEVEL), Set.of(Role.FENIX_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);
		assertThrows(AccessDeniedException.class, () -> mockService.findById(siteId));
	}

	@Test
	void authShouldNotPassWrongResourceType(){
		UUID uuid = UUID.randomUUID();
		SiteId siteId = new SiteId(uuid);
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(siteId, PROJECT), Set.of(Role.FENIX_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		assertThrows(AccessDeniedException.class, () -> mockService.findById(siteId));
	}

	@Test
	void authShouldNotPassWrongRole(){
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(communityId, COMMUNITY), Set.of(Role.SITE_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		assertThrows(AccessDeniedException.class, () -> mockService.getCommunity(communityId));
	}

	@Test
	void authShouldNotPassCommunityAdminDoesntOwnProject(){
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(communityId, COMMUNITY), Set.of(Role.COMMUNITY_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();
		Project project = Project.builder().id(projectId).build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);
		when(projectRepository.findAllByCommunityId(communityId)).thenReturn(Set.of(project));

		assertThrows(AccessDeniedException.class, () -> mockService.getProject(new ProjectId(UUID.randomUUID())));
	}

	@Test
	void authShouldPassCommunityAdminOwnProject(){
		CommunityId communityUUID = new CommunityId(UUID.randomUUID());
		ProjectId projectUUID = new ProjectId(UUID.randomUUID());
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
		Project project = Project.builder()
			.id(projectUUID)
			.communityId(communityUUID)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);
		when(projectRepository.findAllByCommunityIds(Set.of(communityUUID))).thenReturn(Set.of(project));

		Throwable throwable = catchThrowable(() -> mockService.getProject(projectUUID));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldNotPassCommunityAdminHasNotAccessToAppLevel(){
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(null, APP_LEVEL), Set.of(Role.COMMUNITY_ADMIN)
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