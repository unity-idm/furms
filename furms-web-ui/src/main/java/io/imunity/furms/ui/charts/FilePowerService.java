/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.ui.utils.NotificationUtils;
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
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class FilePowerService {
	private final ProjectAllocationService projectAllocationService;
	private final CommunityAllocationService communityAllocationService;
	private final ResourceUsageService resourceUsageService;
	private final ObjectMapper objectMapper;

	FilePowerService(ProjectAllocationService projectAllocationService, CommunityAllocationService communityAllocationService, ResourceUsageService resourceUsageService) {
		this.projectAllocationService = projectAllocationService;
		this.communityAllocationService = communityAllocationService;
		this.resourceUsageService = resourceUsageService;
		this.objectMapper = new ObjectMapper().findAndRegisterModules();
	}

	public byte[] getJsonFileForProjectAlloc(String projectId, String projectAllocationId) {
		ProjectAllocationResolved projectAllocation = projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		List<Consumption> consumption = resourceUsageService.findAllResourceUsageHistory(projectId, projectAllocationId).stream()
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
			NotificationUtils.showErrorNotification("base.error.message");
			throw new RuntimeException(e);
		}
	}

	public byte[] getJsonFileForCommunity(String communityId, String communityAllocationId) {
		CommunityAllocationResolved communityAllocation = communityAllocationService.findByIdWithRelatedObjects(communityAllocationId).get();
		List<Consumption> consumption = getCumulativeUsageForCommunityAlloc(communityId, communityAllocationId).entrySet().stream()
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
			NotificationUtils.showErrorNotification("base.error.message");
			throw new RuntimeException(e);
		}
	}
	public byte[] getCsvFileForProjectAlloc(String projectId, String projectAllocationId) {
		String header = "Allocation,Consumption until,Amount,Unit";
		ProjectAllocationResolved projectAllocation = projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		List<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistory(projectId, projectAllocationId)
			.stream().sorted(Comparator.comparing(usage -> usage.utcProbedAt)).collect(Collectors.toList());
		StringBuilder file = new StringBuilder(header);
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

	public byte[] getCsvFileForCommunity(String communityId, String communityAllocationId) {
		String header = "Allocation,Consumption until,Amount,Unit";
		CommunityAllocationResolved communityAllocationResolved = communityAllocationService.findByIdWithRelatedObjects(communityAllocationId).get();
		Map<LocalDateTime, BigDecimal> consumption = getCumulativeUsageForCommunityAlloc(communityId, communityAllocationId);
		StringBuilder file = new StringBuilder(header);
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

	private Map<LocalDateTime, BigDecimal> getCumulativeUsageForCommunityAlloc(String communityId, String communityAllocationId) {
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistoryByCommunity(communityId, communityAllocationId);

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
}
