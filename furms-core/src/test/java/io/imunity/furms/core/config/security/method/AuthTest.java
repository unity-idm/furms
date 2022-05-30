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

		Throwable throwable = catchThrowable(() -> mockService.findById(new SiteId(uuid)));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldPassFenixAdminHasSpecialAdminRights(){
		CommunityId uuid = new CommunityId(UUID.randomUUID());
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(uuid.id, APP_LEVEL), Set.of(Role.FENIX_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		Throwable throwable = catchThrowable(() -> mockService.getCommunity(uuid));
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

		assertThrows(AccessDeniedException.class, () -> mockService.findById(new SiteId(uuid1)));
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

		assertThrows(AccessDeniedException.class, () -> mockService.findById(new SiteId(uuid)));
	}

	@Test
	void authShouldNotPassWrongRole(){
		CommunityId uuid = new CommunityId(UUID.randomUUID());
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(uuid.id, COMMUNITY), Set.of(Role.SITE_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);

		assertThrows(AccessDeniedException.class, () -> mockService.getCommunity(uuid));
	}

	@Test
	void authShouldNotPassCommunityAdminDoesntOwnProject(){
		CommunityId uuid = new CommunityId(UUID.randomUUID());
		ProjectId uuid2 = new ProjectId(UUID.randomUUID());
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(uuid.id, COMMUNITY), Set.of(Role.COMMUNITY_ADMIN)
		);
		FURMSUser furmsUser = FURMSUser.builder()
			.id(new PersistentId("id"))
			.firstName("Ala")
			.lastName("Kot")
			.email("a@a.pl")
			.roles(roles)
			.build();
		Project project = Project.builder().id(uuid2).build();

		when(provider.getFURMSUser()).thenReturn(furmsUser);
		when(projectRepository.findAllByCommunityId(uuid)).thenReturn(Set.of(project));

		assertThrows(AccessDeniedException.class, () -> mockService.getProject(new ProjectId(UUID.randomUUID())));
	}

	@Test
	void authShouldPassCommunityAdminOwnProject(){
		CommunityId communityUUID = new CommunityId(UUID.randomUUID());
		ProjectId projectUUID = new ProjectId(UUID.randomUUID());
		Map<ResourceId, Set<Role>> roles = Map.of(
			new ResourceId(communityUUID.id, COMMUNITY), Set.of(Role.COMMUNITY_ADMIN)
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