/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.communities.CommunityId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectsRestServiceTest
{
	@Mock
	private ProjectService projectService;
	@Mock
	private ProjectAllocationService projectAllocationService;
	@Mock
	private ProjectInstallationsService projectInstallationsService;
	@Mock
	private CommunityService communityService;
	@Mock
	private ProjectsRestConverter converter;

	@InjectMocks
	private ProjectsRestService projectsRestService;

	@Test
	void shouldThrowExceptionWhenValidityIsNull()
	{
		String communityId = UUID.randomUUID().toString();
		ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest(
			communityId,
			"acrontm",
			"gid",
			"name",
			"desc",
			null,
			"resarches",
			"id"
		);
		when(communityService.existsById(new CommunityId(communityId))).thenReturn(true);

		String message = assertThrows(IllegalArgumentException.class,
			() -> projectsRestService.create(projectCreateRequest)).getMessage();

		assertThat(message).isEqualTo("Validity cannot be null");
	}

	@Test
	void shouldThrowExceptionWhenCommunityIdDoesntExist()
	{
		String communityId = UUID.randomUUID().toString();
		ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest(
			communityId,
			"acrontm",
			"gid",
			"name",
			"desc",
			new Validity(LocalDateTime.MAX, LocalDateTime.MAX),
			"resarches",
			"id"
		);

		String message = assertThrows(IllegalArgumentException.class,
			() -> projectsRestService.create(projectCreateRequest)).getMessage();

		assertThat(message).isEqualTo("CommunityId doesn't exist");
	}

	@Test
	void shouldThrowExceptionWhenCommunityIdIsNull()
	{
		ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest(
			null,
			"acrontm",
			"gid",
			"name",
			"desc",
			new Validity(LocalDateTime.MAX, LocalDateTime.MAX),
			"resarches",
			"id"
		);

		String message = assertThrows(IllegalArgumentException.class,
			() -> projectsRestService.create(projectCreateRequest)).getMessage();

		assertThat(message).isEqualTo("CommunityId cannot be null or empty");
	}

	@Test
	void shouldThrowExceptionWhenValidityToIsNull()
	{
		String communityId = UUID.randomUUID().toString();
		ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest(
			communityId,
			"acrontm",
			"gid",
			"name",
			"desc",
			new Validity(LocalDateTime.MAX, null),
			"resarches",
			"id"
		);
		when(communityService.existsById(new CommunityId(communityId))).thenReturn(true);

		String message = assertThrows(IllegalArgumentException.class,
			() -> projectsRestService.create(projectCreateRequest)).getMessage();

		assertThat(message).isEqualTo("Validity.to cannot be null");
	}

	@Test
	void shouldThrowExceptionWhenValidityFormIsNull()
	{
		String communityId = UUID.randomUUID().toString();
		ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest(
			communityId,
			"acrontm",
			"gid",
			"name",
			"desc",
			new Validity(null, LocalDateTime.MAX),
			"resarches",
			"id"
		);
		when(communityService.existsById(new CommunityId(communityId))).thenReturn(true);

		String message = assertThrows(IllegalArgumentException.class,
			() -> projectsRestService.create(projectCreateRequest)).getMessage();

		assertThat(message).isEqualTo("Validity.from cannot be null");
	}

	@Test
	void shouldThrowExceptionWhenProjectLeaderIdIsNull()
	{
		String communityId = UUID.randomUUID().toString();
		ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest(
			communityId,
			"acrontm",
			"gid",
			"name",
			"desc",
			new Validity(LocalDateTime.MAX, LocalDateTime.MAX),
			"resarches",
			null
		);
		when(communityService.existsById(new CommunityId(communityId))).thenReturn(true);

		String message = assertThrows(IllegalArgumentException.class,
			() -> projectsRestService.create(projectCreateRequest)).getMessage();

		assertThat(message).isEqualTo("ProjectLeaderId cannot be null or empty");
	}
}