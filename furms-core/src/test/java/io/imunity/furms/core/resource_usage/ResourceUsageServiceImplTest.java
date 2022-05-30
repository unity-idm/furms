/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_usage;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceUsageServiceImplTest {

	@Mock
	private ResourceUsageRepository resourceUsageRepository;

	@InjectMocks
	private ResourceUsageServiceImpl service;

	@Test
	void shouldReturnResourceUsageBySiteIdAndProjectIdAndInPeriod() {
		final ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		final ProjectAllocationId projectAllocationId1 = new ProjectAllocationId(UUID.randomUUID());
		final ProjectId projectId = new ProjectId(UUID.randomUUID());
		final SiteId siteId = new SiteId(UUID.randomUUID());

		final Set<ProjectAllocationId> projectAllocations = Set.of(projectAllocationId, projectAllocationId1);
		final LocalDateTime from = LocalDateTime.now().minusDays(1);
		final LocalDateTime to = from.plusDays(2);
		when(resourceUsageRepository.findUserResourceUsages(projectAllocations, from, to)).thenReturn(Set.of(
					UserResourceUsage.builder().projectAllocationId(projectAllocationId).projectId(projectId).build(),
					UserResourceUsage.builder().projectAllocationId(projectAllocationId1).projectId(projectId).build()));

		//when
		final Set<UserResourceUsage> userUsages = service.findAllUserUsages(siteId, projectAllocations, from, to);

		//then
		assertThat(userUsages).hasSize(2);
	}
}