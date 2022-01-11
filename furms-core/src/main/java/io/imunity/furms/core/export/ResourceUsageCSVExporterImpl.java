/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.export;

import io.imunity.furms.api.export.ResourceUsageCSVExporter;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;

@Service
class ResourceUsageCSVExporterImpl implements ResourceUsageCSVExporter {
	private static final String HEADER = "Allocation,Consumption until,Amount,Unit";
	private final ProjectAllocationRepository projectAllocationRepository;
	private final CommunityAllocationRepository communityAllocationRepository;
	private final ResourceUsageRepository resourceUsageRepository;
	private final ResourceUsageExportHelper exportHelper;

	ResourceUsageCSVExporterImpl(ProjectAllocationRepository projectAllocationRepository,
	                             CommunityAllocationRepository communityAllocationRepository,
	                             ResourceUsageRepository resourceUsageRepository, ResourceUsageExportHelper exportHelper) {
		this.projectAllocationRepository = projectAllocationRepository;
		this.communityAllocationRepository = communityAllocationRepository;
		this.resourceUsageRepository = resourceUsageRepository;
		this.exportHelper = exportHelper;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Supplier<String> getCsvForProjectAllocation(String projectId, String projectAllocationId) {
		return () -> {
			exportHelper.assertProjectAndAllocationAreRelated(projectId, projectAllocationId);
			ProjectAllocationResolved projectAllocation = projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId).get();
			List<ResourceUsage> allResourceUsageHistory = resourceUsageRepository.findResourceUsagesHistory(UUID.fromString(projectAllocationId))
				.stream().sorted(Comparator.comparing(usage -> usage.utcProbedAt)).collect(Collectors.toList());
			StringBuilder file = new StringBuilder(HEADER);
			for (ResourceUsage usage : allResourceUsageHistory) {
				file.append("\r\n")
					.append(projectAllocation.name)
					.append(",")
					.append(usage.utcProbedAt)
					.append(",")
					.append(usage.cumulativeConsumption)
					.append(",")
					.append(projectAllocation.resourceType.unit.getSuffix());
			}
			return file.toString();
		};
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Supplier<String> getCsvForCommunityAllocation(String communityId, String communityAllocationId) {
		return () -> {
			exportHelper.assertCommunityAndAllocationAreRelated(communityId, communityAllocationId);
			CommunityAllocationResolved communityAllocationResolved = communityAllocationRepository.findByIdWithRelatedObjects(communityAllocationId).get();
			Map<LocalDateTime, BigDecimal> consumption = exportHelper.getCumulativeUsageForCommunityAlloc(communityAllocationId);
			StringBuilder file = new StringBuilder(HEADER);
			for (Map.Entry<LocalDateTime, BigDecimal> usage : consumption.entrySet()) {
				file.append("\r\n")
					.append(communityAllocationResolved.name)
					.append(",")
					.append(usage.getKey())
					.append(",")
					.append(usage.getValue())
					.append(",")
					.append(communityAllocationResolved.resourceType.unit.getSuffix());
			}
			return file.toString();
		};
	}
}
