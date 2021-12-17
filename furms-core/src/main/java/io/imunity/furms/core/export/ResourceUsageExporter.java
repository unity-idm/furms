/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.export;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.export.ResourceUsageCSVExporter;
import io.imunity.furms.api.export.ResourceUsageJSONExporter;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class ResourceUsageExporter implements ResourceUsageCSVExporter, ResourceUsageJSONExporter {
	private static final String HEADER = "Allocation,Consumption until,Amount,Unit";
	private final ProjectAllocationRepository projectAllocationRepository;
	private final CommunityAllocationRepository communityAllocationRepository;
	private final ResourceUsageRepository resourceUsageRepository;
	private final ObjectMapper objectMapper;

	ResourceUsageExporter(ProjectAllocationRepository projectAllocationRepository,
	                      CommunityAllocationRepository communityAllocationRepository,
	                      ResourceUsageRepository resourceUsageRepository) {
		this.projectAllocationRepository = projectAllocationRepository;
		this.communityAllocationRepository = communityAllocationRepository;
		this.resourceUsageRepository = resourceUsageRepository;
		this.objectMapper = new ObjectMapper().findAndRegisterModules();
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public byte[] getJsonFileForProjectAllocation(String projectId, String projectAllocationId) {
		validProjectAndAllocationAreRelated(projectId, projectAllocationId);
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

		try {
			return objectMapper.writeValueAsBytes(projectResourceUsage);
		} catch (JsonProcessingException e) {
			throw new ExportException(e);
		}
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public byte[] getJsonFileForCommunityAllocation(String communityId, String communityAllocationId) {
		validCommunityAndAllocationAreRelated(communityId, communityAllocationId);
		CommunityAllocationResolved communityAllocation = communityAllocationRepository.findByIdWithRelatedObjects(communityAllocationId).get();
		List<Consumption> consumption = getCumulativeUsageForCommunityAlloc(communityAllocationId).entrySet().stream()
			.map(usage -> new Consumption(usage.getKey(), usage.getValue()))
			.collect(toList());

		CommunityResourceUsage projectResourceUsage = CommunityResourceUsage.builder()
			.communityId(communityAllocation.communityId)
			.community(communityAllocation.communityName)
			.allocationId(communityAllocation.id)
			.allocation(communityAllocation.name)
			.unit(communityAllocation.resourceType.unit.getSuffix())
			.consumption(consumption)
			.build();

		try {
			return objectMapper.writeValueAsBytes(projectResourceUsage);
		} catch (JsonProcessingException e) {
			throw new ExportException(e);
		}
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public byte[] getCsvFileForProjectAllocation(String projectId, String projectAllocationId) {
		validProjectAndAllocationAreRelated(projectId, projectAllocationId);
		ProjectAllocationResolved projectAllocation = projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId).get();
		List<ResourceUsage> allResourceUsageHistory = resourceUsageRepository.findResourceUsagesHistory(UUID.fromString(projectAllocationId))
			.stream().sorted(Comparator.comparing(usage -> usage.utcProbedAt)).collect(Collectors.toList());
		StringBuilder file = new StringBuilder(HEADER);
		for(ResourceUsage usage : allResourceUsageHistory){
			file.append("\r\n")
				.append(projectAllocation.name)
				.append(",")
				.append(usage.utcProbedAt)
				.append(",")
				.append(usage.cumulativeConsumption)
				.append(",")
				.append(projectAllocation.resourceType.unit.getSuffix());
		}
		return file.toString().getBytes(StandardCharsets.UTF_8);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public byte[] getCsvFileForCommunityAllocation(String communityId, String communityAllocationId) {
		validCommunityAndAllocationAreRelated(communityId, communityAllocationId);
		CommunityAllocationResolved communityAllocationResolved = communityAllocationRepository.findByIdWithRelatedObjects(communityAllocationId).get();
		Map<LocalDateTime, BigDecimal> consumption = getCumulativeUsageForCommunityAlloc(communityAllocationId);
		StringBuilder file = new StringBuilder(HEADER);
		for(Map.Entry<LocalDateTime, BigDecimal> usage : consumption.entrySet()){
			file.append("\r\n")
				.append(communityAllocationResolved.name)
				.append(",")
				.append(usage.getKey())
				.append(",")
				.append(usage.getValue())
				.append(",")
				.append(communityAllocationResolved.resourceType.unit.getSuffix());
		}
		return file.toString().getBytes(StandardCharsets.UTF_8);
	}

	private Map<LocalDateTime, BigDecimal> getCumulativeUsageForCommunityAlloc(String communityAllocationId) {
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageRepository.findResourceUsagesHistoryByCommunityAllocationId(UUID.fromString(communityAllocationId));

		List<LocalDateTime> dates = allResourceUsageHistory
			.stream().map(usage -> usage.utcProbedAt)
			.distinct()
			.sorted(Comparator.comparing(identity()))
			.collect(toList());

		List<List<BigDecimal>> orderedUsagesGroupedByAllocationId = allResourceUsageHistory.stream()
			.collect(
				groupingBy(
					usage -> usage.projectAllocationId,
					toMap(
						usage -> usage.utcProbedAt,
						identity(),
						(usage, usage1) -> usage.utcProbedAt.isAfter(usage1.utcProbedAt) ? usage : usage1)
				)
			).values().stream()
			.map(usageMap -> usageMap.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> entry.getValue().cumulativeConsumption)))
			.map(usageMap -> prepareDataForCommunity(dates, usageMap))
			.collect(toList());

		Map<LocalDateTime, BigDecimal> usage = new TreeMap<>();
		for(int i = 0; i < dates.size(); i++) {
			BigDecimal tmp = BigDecimal.ZERO;
			for (List<BigDecimal> doubles : orderedUsagesGroupedByAllocationId) {
				tmp = tmp.add(doubles.get(i));
			}
			usage.put(dates.get(i), tmp);
		}
		return usage;
	}

	private List<BigDecimal> prepareDataForCommunity(List<LocalDateTime> dates, Map<LocalDateTime, BigDecimal> orderedAmountsByTime) {
		BigDecimal last = BigDecimal.ZERO;
		List<BigDecimal> values = new ArrayList<>();
		for(LocalDateTime date : dates){
			BigDecimal value = orderedAmountsByTime.getOrDefault(date, last);
			values.add(value);
			last = value;
		}
		return values;
	}

	private void validProjectAndAllocationAreRelated(String projectId, String projectAllocationId) {
		projectAllocationRepository.findById(projectAllocationId)
			.filter(allocation -> allocation.projectId.equals(projectId))
			.orElseThrow(() -> new IllegalArgumentException(String.format(
				"Project id %s and project allocation id %s are not related", projectId, projectAllocationId)
			));
	}

	private void validCommunityAndAllocationAreRelated(String communityId, String communityAllocationId){
		communityAllocationRepository.findById(communityAllocationId)
			.filter(allocation -> allocation.communityId.equals(communityId))
			.orElseThrow(() -> new IllegalArgumentException(String.format(
				"Community id %s and community allocation id %s are not related", communityId, communityAllocationId)
			));
	}
}
