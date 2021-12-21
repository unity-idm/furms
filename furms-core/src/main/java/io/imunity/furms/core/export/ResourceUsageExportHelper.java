/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.export;

import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
class ResourceUsageExportHelper {

	private final ResourceUsageRepository resourceUsageRepository;
	private final ProjectAllocationRepository projectAllocationRepository;
	private final CommunityAllocationRepository communityAllocationRepository;

	ResourceUsageExportHelper(ResourceUsageRepository resourceUsageRepository, ProjectAllocationRepository projectAllocationRepository, CommunityAllocationRepository communityAllocationRepository) {
		this.resourceUsageRepository = resourceUsageRepository;
		this.projectAllocationRepository = projectAllocationRepository;
		this.communityAllocationRepository = communityAllocationRepository;
	}

	Map<LocalDateTime, BigDecimal> getCumulativeUsageForCommunityAlloc(String communityAllocationId) {
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

	void assertProjectAndAllocationAreRelated(String projectId, String projectAllocationId) {
		projectAllocationRepository.findById(projectAllocationId)
			.filter(allocation -> allocation.projectId.equals(projectId))
			.orElseThrow(() -> new IllegalArgumentException(String.format(
				"Project id %s and project allocation id %s are not related", projectId, projectAllocationId)
			));
	}

	void assertCommunityAndAllocationAreRelated(String communityId, String communityAllocationId) {
		communityAllocationRepository.findById(communityAllocationId)
			.filter(allocation -> allocation.communityId.equals(communityId))
			.orElseThrow(() -> new IllegalArgumentException(String.format(
				"Community id %s and community allocation id %s are not related", communityId, communityAllocationId)
			));
	}
}
