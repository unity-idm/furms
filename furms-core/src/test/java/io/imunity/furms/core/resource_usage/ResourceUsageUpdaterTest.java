/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_usage;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceUsageUpdaterTest {
	@Mock
	private ResourceUsageRepository repository;
	@Mock
	private ProjectAllocationRepository projectAllocationRepository;
	@Mock
	private ApplicationEventPublisher publisher;

	private ResourceUsageUpdaterImpl service;

	@BeforeEach
	void init() {
		service = new ResourceUsageUpdaterImpl(repository, projectAllocationRepository, publisher);
	}

	@Test
	void shouldUpdateResourceUsage() {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		ResourceUsage resourceUsage = ResourceUsage.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.cumulativeConsumption(BigDecimal.TEN)
			.probedAt(LocalDateTime.now().minusMinutes(5))
			.build();
		ProjectAllocationResolved build = ProjectAllocationResolved.builder().build();
		when(projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId)).thenReturn(Optional.of(build));

		service.updateUsage(resourceUsage);

		verify(repository).create(resourceUsage, build);
	}

	@Test
	void shouldUpdateUserResourceUsage() {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		UserResourceUsage resourceUsage = UserResourceUsage.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.fenixUserId(new FenixUserId("userId"))
			.cumulativeConsumption(BigDecimal.TEN)
			.consumedUntil(LocalDateTime.now().minusMinutes(5))
			.build();

		service.updateUsage(resourceUsage);

		verify(repository).create(resourceUsage);
	}

}