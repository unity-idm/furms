/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.api.authz.FURMSAuthenticationProvider;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.ResourceType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
public class AuthTest {

	@Autowired
	private ServiceMock mockService;

	@MockBean
	AuthenticationManager authenticationManager;

	@MockBean
	ProjectRepository projectRepository;

	@Mock
	Authentication authentication;

	Map<ResourceId, Set<Role>> roles = new HashMap<>();

	@BeforeAll
	public void configSecurityContext(){
		FURMSUser furmsUser = new FURMSUser("id", "Ala", "Kot", "a@a.pl", roles);
		FURMSAuthenticationProvider provider = mock(FURMSAuthenticationProvider.class);
		when(authentication.getPrincipal()).thenReturn(provider);
		when(provider.getFURMSUser()).thenReturn(furmsUser);
		when(authentication.isAuthenticated()).thenReturn(true);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@BeforeEach
	public void clearContext(){
		roles.clear();
	}

	@Test
	public void authShouldPassFenixAdminHasAccessToAPPLevel(){
		roles.put(new ResourceId((String) null, APP_LEVEL), Set.of(Role.FENIX_ADMIN));

		Throwable throwable = catchThrowable(() -> mockService.findAll());
		assertThat(throwable).isNull();
	}

	@Test
	public void authShouldPassFenixAdminHasAccessToResource(){
		UUID uuid = UUID.randomUUID();
		roles.put(new ResourceId(uuid, APP_LEVEL), Set.of(Role.FENIX_ADMIN));

		Throwable throwable = catchThrowable(() -> mockService.findById(uuid.toString()));
		assertThat(throwable).isNull();
	}

	@Test
	public void authShouldPassFenixAdminHasSpecialAdminRights(){
		UUID uuid = UUID.randomUUID();
		roles.put(new ResourceId(uuid, APP_LEVEL), Set.of(Role.FENIX_ADMIN));

		Throwable throwable = catchThrowable(() -> mockService.getCommunity(uuid.toString()));
		assertThat(throwable).isNull();
	}


	@Test
	public void authShouldNotPassWrongResourceId(){
		UUID uuid = UUID.randomUUID();
		UUID uuid1 = UUID.randomUUID();
		roles.put(new ResourceId(uuid, APP_LEVEL), Set.of(Role.FENIX_ADMIN));

		assertThrows(AccessDeniedException.class, () -> mockService.findById(uuid1.toString()));
	}

	@Test
	public void authShouldNotPassWrongResourceType(){
		UUID uuid = UUID.randomUUID();
		roles.put(new ResourceId(uuid, PROJECT), Set.of(Role.FENIX_ADMIN));

		assertThrows(AccessDeniedException.class, () -> mockService.findById(uuid.toString()));
	}

	@Test
	public void authShouldNotPassWrongRole(){
		UUID uuid = UUID.randomUUID();
		roles.put(new ResourceId(uuid, COMMUNITY), Set.of(Role.SITE_ADMIN));

		assertThrows(AccessDeniedException.class, () -> mockService.getCommunity(uuid.toString()));
	}

	@Test
	public void authShouldNotPassCommunityAdminDoesntOwnProject(){
		UUID uuid = UUID.randomUUID();
		UUID uuid2 = UUID.randomUUID();
		roles.put(new ResourceId(uuid, COMMUNITY), Set.of(Role.COMMUNITY_ADMIN));
		Project project = Project.builder().id(uuid2.toString()).build();

		Mockito.when(projectRepository.findAll(uuid.toString())).thenReturn(Set.of(project));

		assertThrows(AccessDeniedException.class, () -> mockService.getProject(UUID.randomUUID().toString()));
	}

	@Test
	public void authShouldPassCommunityAdminOwnProject(){
		UUID communityUUID = UUID.randomUUID();
		UUID projectUUID = UUID.randomUUID();
		roles.put(new ResourceId(communityUUID, COMMUNITY), Set.of(Role.COMMUNITY_ADMIN));
		Project project = Project.builder().id(projectUUID.toString()).build();

		Mockito.when(projectRepository.findAll(communityUUID.toString())).thenReturn(Set.of(project));

		Throwable throwable = catchThrowable(() -> mockService.getProject(projectUUID.toString()));
		assertThat(throwable).isNull();
	}

	@Test
	public void authShouldNotPassCommunityAdminHasNotAccessToAppLevel(){
		roles.put(new ResourceId((String) null, APP_LEVEL), Set.of(Role.COMMUNITY_ADMIN));

		assertThrows(AccessDeniedException.class, () -> mockService.findAllWithClassScopeAuthorization());
	}
}