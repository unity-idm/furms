/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.domain.project_allocation.CreateProjectAllocationEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.RemoveProjectAllocationEvent;
import io.imunity.furms.domain.project_allocation.UpdateProjectAllocationEvent;
import io.imunity.furms.site.api.ProjectInstallationService;
import io.imunity.furms.site.api.SiteAgentService;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

class ProjectAllocationServiceImplTest {
	@Mock
	private ProjectAllocationServiceValidator validator;
	@Mock
	private ProjectAllocationRepository projectAllocationRepository;
	@Mock
	private ProjectInstallationService projectInstallationService;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private SiteAgentService siteAgentService;

	private ProjectAllocationServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ProjectAllocationServiceImpl(projectAllocationRepository, projectInstallationService, validator, usersDAO, siteAgentService, publisher);
		orderVerifier = inOrder(projectAllocationRepository, publisher);
	}

	@Test
	void shouldReturnCommunityAllocation() {
		//given
		String id = "id";
		when(projectAllocationRepository.findById(id)).thenReturn(Optional.of(ProjectAllocation.builder()
			.id(id)
			.name("name")
			.build())
		);

		//when
		Optional<ProjectAllocation> byId = service.findById(id);

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(id);
	}

	@Test
	void shouldNotReturnNotExisting() {
		//when
		Optional<ProjectAllocation> otherId = service.findById("otherId");

		//then
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllProjectAllocationsExistingInRepository() {
		//given
		when(projectAllocationRepository.findAll()).thenReturn(Set.of(
			ProjectAllocation.builder().id("id1").name("name").build(),
			ProjectAllocation.builder().id("id2").name("name2").build()));

		//when
		Set<ProjectAllocation> allProjectAllocations = service.findAll();

		//then
		assertThat(allProjectAllocations).hasSize(2);
	}

	@Test
	void shouldAllowToCreateProjectAllocation() {
		//given
		ProjectAllocation request = ProjectAllocation.builder()
			.id("id")
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		//when
		service.create(request);

		orderVerifier.verify(projectAllocationRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new CreateProjectAllocationEvent("id")));
	}

	@Test
	void shouldAllowToUpdateProjectAllocation() {
		//given
		ProjectAllocation request = ProjectAllocation.builder()
			.id("id")
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		//when
		service.update(request);

		orderVerifier.verify(projectAllocationRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new UpdateProjectAllocationEvent("id")));
	}

	@Test
	void shouldAllowToDeleteProjectAllocation() {
		//given
		String id = "id";
		when(projectAllocationRepository.exists(id)).thenReturn(true);

		//when
		service.delete("projectId", id);

		orderVerifier.verify(projectAllocationRepository).delete(eq(id));
		orderVerifier.verify(publisher).publishEvent(eq(new RemoveProjectAllocationEvent("id")));
	}
}