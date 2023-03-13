/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.api.projects.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
	private ProjectsRestConverter converter;

	@InjectMocks
	private ProjectsRestService projectsRestService;

	@Test
	void shouldThrowExceptionWhenValidityIsNull()
	{
		ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest(
			UUID.randomUUID().toString(),
			"acrontm",
			"gid",
			"name",
			"desc",
			null,
			"resarches",
			"id"
		);

		String message = assertThrows(IllegalArgumentException.class,
			() -> projectsRestService.create(projectCreateRequest)).getMessage();

		assertThat(message).isEqualTo("Validity cannot be null");
	}

	@Test
	void shouldThrowExceptionWhenValidityToIsNull()
	{
		ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest(
			UUID.randomUUID().toString(),
			"acrontm",
			"gid",
			"name",
			"desc",
			new Validity(LocalDateTime.MAX, null),
			"resarches",
			"id"
		);

		String message = assertThrows(IllegalArgumentException.class,
			() -> projectsRestService.create(projectCreateRequest)).getMessage();

		assertThat(message).isEqualTo("Validity.to cannot be null");
	}

	@Test
	void shouldThrowExceptionWhenValidityFormIsNull()
	{
		ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest(
			UUID.randomUUID().toString(),
			"acrontm",
			"gid",
			"name",
			"desc",
			new Validity(null, LocalDateTime.MAX),
			"resarches",
			"id"
		);

		String message = assertThrows(IllegalArgumentException.class,
			() -> projectsRestService.create(projectCreateRequest)).getMessage();

		assertThat(message).isEqualTo("Validity.from cannot be null");
	}

	@Test
	void shouldThrowExceptionWhenProjectLeaderIdIsNull()
	{
		ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest(
			UUID.randomUUID().toString(),
			"acrontm",
			"gid",
			"name",
			"desc",
			new Validity(LocalDateTime.MAX, LocalDateTime.MAX),
			"resarches",
			null
		);

		String message = assertThrows(IllegalArgumentException.class,
			() -> projectsRestService.create(projectCreateRequest)).getMessage();

		assertThat(message).isEqualTo("ProjectLeaderId cannot be null or empty");
	}
}