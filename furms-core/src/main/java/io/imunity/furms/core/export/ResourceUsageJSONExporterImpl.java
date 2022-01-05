/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.export;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.export.ResourceUsageJSONExporter;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static java.util.stream.Collectors.toList;

@Service
class ResourceUsageJSONExporterImpl implements ResourceUsageJSONExporter {
	private final ProjectAllocationRepository projectAllocationRepository;
	private final CommunityAllocationRepository communityAllocationRepository;
	private final ResourceUsageRepository resourceUsageRepository;
	private final ResourceUsageExportHelper exportHelper;
	private final ObjectMapper objectMapper;

	ResourceUsageJSONExporterImpl(ProjectAllocationRepository projectAllocationRepository,
	                              CommunityAllocationRepository communityAllocationRepository,
	                              ResourceUsageRepository resourceUsageRepository,
	                              ResourceUsageExportHelper exportHelper) {
		this.projectAllocationRepository = projectAllocationRepository;
		this.communityAllocationRepository = communityAllocationRepository;
		this.resourceUsageRepository = resourceUsageRepository;
		this.objectMapper = new ObjectMapper().findAndRegisterModules();
		this.exportHelper = exportHelper;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Supplier<String> getJsonForProjectAllocation(String projectId, String projectAllocationId) {
		exportHelper.assertProjectAndAllocationAreRelated(projectId, projectAllocationId);
		ProjectAllocationResolved projectAllocation = projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId).get();
		List<Consumption> consumption = resourceUsageRepository.findResourceUsagesHistory(UUID.fromString(projectAllocationId)).stream()
			.sorted(Comparator.comparing(usage -> usage.utcProbedAt))
			.map(usage -> new Consumption(usage.utcProbedAt, usage.cumulativeConsumption))
			.collect(toList());

		ProjectResourceUsage projectResourceUsage = ProjectResourceUsage.builder()
			.projectId(projectAllocation.projectId)
			.project(projectAllocation.projectName)
			.allocationId(projectAllocation.id)
			.allocation(projectAllocation.name)
			.unit(projectAllocation.resourceType.unit.getSuffix())
			.consumption(consumption)
			.build();

			return () -> {
				try {
				return objectMapper.writeValueAsString(projectResourceUsage);
				} catch (JsonProcessingException e) {
					throw new ExportException(e);
				}
			};
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Supplier<String> getJsonForCommunityAllocation(String communityId, String communityAllocationId) {
		exportHelper.assertCommunityAndAllocationAreRelated(communityId, communityAllocationId);
		CommunityAllocationResolved communityAllocation = communityAllocationRepository.findByIdWithRelatedObjects(communityAllocationId).get();
		List<Consumption> consumption = exportHelper.getCumulativeUsageForCommunityAlloc(communityAllocationId).entrySet().stream()
			.map(usage -> new Consumption(usage.getKey(), usage.getValue()))
			.collect(toList());

		CommunityResourceUsage communityResourceUsage = CommunityResourceUsage.builder()
			.communityId(communityAllocation.communityId)
			.community(communityAllocation.communityName)
			.allocationId(communityAllocation.id)
			.allocation(communityAllocation.name)
			.unit(communityAllocation.resourceType.unit.getSuffix())
			.consumption(consumption)
			.build();

		return () -> {
			try {
				return objectMapper.writeValueAsString(communityResourceUsage);
			} catch (JsonProcessingException e) {
				throw new ExportException(e);
			}
		};
	}
}