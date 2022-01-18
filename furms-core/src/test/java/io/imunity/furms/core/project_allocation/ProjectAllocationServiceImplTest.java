/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.api.validation.exceptions.RemovalOfConsumedProjectAllocationIsFirbiddenException;
import io.imunity.furms.core.project_allocation_installation.ProjectAllocationInstallationService;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.domain.project_allocation.ProjectAllocationCreatedEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocationRemovedEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocationUpdatedEvent;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
	private SiteAgentProjectOperationService siteAgentProjectOperationService;
	@Mock
	private SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService;
	@Mock
	private ProjectAllocationInstallationService projectAllocationInstallationService;

	@InjectMocks
	private ProjectAllocationServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		orderVerifier = inOrder(projectAllocationRepository, projectAllocationInstallationService, publisher);
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
		Optional<ProjectAllocation> byId = service.findByProjectIdAndId("communityId", id);

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(id);
	}

	@Test
	void shouldNotReturnNotExisting() {
		//when
		Optional<ProjectAllocation> otherId = service.findByProjectIdAndId("communityId", "otherId");

		//then
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllProjectAllocationsExistingInRepository() {
		//given
		when(projectAllocationRepository.findAll("projectId")).thenReturn(Set.of(
			ProjectAllocation.builder().id("id1").name("name").build(),
			ProjectAllocation.builder().id("id2").name("name2").build()));

		//when
		Set<ProjectAllocation> allProjectAllocations = service.findAll("communityId", "projectId");

		//then
		assertThat(allProjectAllocations).hasSize(2);
	}

	@Test
	void shouldFindByProjectIdAndAllocationIdWithRelatedObjects() {
		//given
		final String allocationId = "allocationId";
		final String projectId = "projectId";
		when(projectAllocationRepository.findByIdWithRelatedObjects(allocationId)).thenReturn(Optional.of(
				ProjectAllocationResolved.builder().projectId(projectId).build()
		));

		//when
		Optional<ProjectAllocationResolved> projectAllocation = service.findByIdValidatingProjectsWithRelatedObjects(
				allocationId, projectId);

		//then
		assertThat(projectAllocation).isPresent();
		assertThat(projectAllocation.get().projectId).isEqualTo(projectId);
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
		when(projectInstallationService.findProjectInstallationOfProjectAllocation( "projectAllocationId")).thenReturn(
			ProjectInstallation.builder()
				.siteId("siteId")
				.build()
		);
		when(projectAllocationRepository.create(request)).thenReturn("projectAllocationId");
		when(projectAllocationRepository.findById("projectAllocationId")).thenReturn(Optional.of(request));

		service.create("communityId", request);

		orderVerifier.verify(projectAllocationRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new ProjectAllocationCreatedEvent(request)));
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

		when(projectInstallationService.findProjectInstallationOfProjectAllocation( "id")).thenReturn(
			ProjectInstallation.builder()
				.siteId("siteId")
				.build()
		);
		when(projectInstallationService.isProjectInstalled("siteId", "id")).thenReturn(true);
		when(projectAllocationRepository.findById("id")).thenReturn(Optional.of(request));

		//when
		service.update("communityId", request);

		orderVerifier.verify(projectAllocationRepository).update(eq(request));
		orderVerifier.verify(projectAllocationInstallationService).updateAndStartAllocation("id");
		orderVerifier.verify(publisher).publishEvent(eq(new ProjectAllocationUpdatedEvent( request, request)));
	}

	@Test
	void shouldAllowToDeleteProjectAllocationWhenProjectAllocationIsNotStartedConsuming() {
		//given
		String id = "id";
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.amount(BigDecimal.TEN)
			.consumed(BigDecimal.ZERO)
			.build();
		ProjectAllocation projectAllocation = ProjectAllocation.builder().build();
		when(projectAllocationRepository.findByIdWithRelatedObjects(id)).thenReturn(Optional.of(projectAllocationResolved));
		when(projectAllocationRepository.findById(id)).thenReturn(Optional.of(projectAllocation));

		//when
		service.delete("projectId", id);

		orderVerifier.verify(projectAllocationInstallationService).createDeallocation(projectAllocationResolved);
		orderVerifier.verify(publisher).publishEvent(eq(new ProjectAllocationRemovedEvent(projectAllocation)));
	}

	@Test
	void shouldNotAllowToDeleteProjectAllocationWhenAllocationIsConsumed() {
		String id = "id";
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.amount(BigDecimal.TEN)
			.consumed(BigDecimal.TEN)
			.build();
		when(projectAllocationRepository.findByIdWithRelatedObjects(id)).thenReturn(Optional.of(projectAllocationResolved));

		assertThrows(RemovalOfConsumedProjectAllocationIsFirbiddenException.class, () -> service.delete("projectId", id));
	}
}