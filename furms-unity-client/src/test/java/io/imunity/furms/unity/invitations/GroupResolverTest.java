/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.invitations;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class GroupResolverTest {

	@Mock
	private ProjectRepository projectRepository;

	@InjectMocks
	private GroupResolver groupResolver;

	@Test
	void shouldResolveFenixAdminGroup(){
		ResourceId resourceId = new ResourceId(UUID.randomUUID(), ResourceType.APP_LEVEL);
		Role role = Role.FENIX_ADMIN;

		String group = groupResolver.resolveGroup(resourceId, role);

		assertEquals("/fenix/users", group);
	}

	@Test
	void shouldResolveSiteAdminGroup(){
		UUID id = UUID.randomUUID();
		ResourceId resourceId = new ResourceId(id, ResourceType.SITE);
		Role role = Role.SITE_ADMIN;

		String group = groupResolver.resolveGroup(resourceId, role);

		assertEquals("/fenix/sites/" + id + "/users", group);
	}

	@Test
	void shouldResolveSiteSupportGroup(){
		UUID id = UUID.randomUUID();
		ResourceId resourceId = new ResourceId(id, ResourceType.SITE);
		Role role = Role.SITE_SUPPORT;

		String group = groupResolver.resolveGroup(resourceId, role);

		assertEquals("/fenix/sites/" + id + "/users", group);
	}

	@Test
	void shouldResolveCommunityAdminGroup(){
		UUID id = UUID.randomUUID();
		ResourceId resourceId = new ResourceId(id, ResourceType.COMMUNITY);
		Role role = Role.COMMUNITY_ADMIN;

		String group = groupResolver.resolveGroup(resourceId, role);

		assertEquals("/fenix/communities/" + id + "/users", group);
	}

	@Test
	void shouldResolveProjectAdminGroup(){
		UUID id = UUID.randomUUID();
		UUID communityId = UUID.randomUUID();
		ResourceId resourceId = new ResourceId(id, ResourceType.PROJECT);
		Role role = Role.PROJECT_ADMIN;
		Project project = Project.builder()
			.id(id.toString())
			.communityId(communityId.toString())
			.build();

		when(projectRepository.findById(id.toString())).thenReturn(Optional.of(project));
		String group = groupResolver.resolveGroup(resourceId, role);

		assertEquals("/fenix/communities/" + communityId + "/projects/" + id + "/users", group);
	}

	@Test
	void shouldResolveProjectUserGroup(){
		UUID id = UUID.randomUUID();
		UUID communityId = UUID.randomUUID();
		ResourceId resourceId = new ResourceId(id, ResourceType.PROJECT);
		Role role = Role.PROJECT_USER;
		Project project = Project.builder()
			.id(id.toString())
			.communityId(communityId.toString())
			.build();

		when(projectRepository.findById(id.toString())).thenReturn(Optional.of(project));
		String group = groupResolver.resolveGroup(resourceId, role);

		assertEquals("/fenix/communities/" + communityId + "/projects/" + id + "/users", group);
	}
}
