/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class ChartPowerService {
	public final ProjectAllocationService projectAllocationService;
	public final AlarmService alarmService;
	public final ResourceUsageService resourceUsageService;

	ChartPowerService(ProjectAllocationService projectAllocationService, AlarmService alarmService, ResourceUsageService resourceUsageService) {
		this.projectAllocationService = projectAllocationService;
		this.alarmService = alarmService;
		this.resourceUsageService = resourceUsageService;
	}

	public ChartData generate(String projectId, String projectAllocationId){
		ProjectAllocationResolved projectAllocation = projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		Optional<AlarmWithUserEmails> alarm = alarmService.find(projectId, projectAllocationId);

		Set<ProjectAllocationChunk> allChunks = projectAllocationService.findAllChunks(projectId, projectAllocationId);
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistory(projectId, projectAllocationId);

		List<LocalDate> dates = Stream.of(
			Stream.of(projectAllocation.resourceCredit.utcStartTime.toLocalDate()),
			allChunks.stream().map(chunk -> chunk.receivedTime.toLocalDate()),
			allResourceUsageHistory.stream().map(usage -> usage.utcProbedAt.toLocalDate())
		)
			.flatMap(identity())
			.distinct()
			.sorted(Comparator.comparing(identity()))
			.collect(toList());

		Map<LocalDate, Double> orderedChunksAmountByTime = allChunks.stream()
			.collect(toMap(chunk -> chunk.receivedTime.toLocalDate(), identity(), (chunk, chunk1) -> chunk.receivedTime.isAfter(chunk1.receivedTime) ? chunk : chunk1))
			.entrySet().stream()
			.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().amount.doubleValue()));
		Map<LocalDate, Double> orderedUsagesAmountByTime = allResourceUsageHistory.stream()
			.collect(toMap(usage -> usage.utcProbedAt.toLocalDate(), identity(), (usage, usage1) -> usage.utcProbedAt.isAfter(usage1.utcProbedAt) ? usage : usage1))
			.entrySet().stream()
			.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().cumulativeConsumption.doubleValue()));
		List<Double> chunks = prepareData(dates, orderedChunksAmountByTime);
		List<Double> usage = prepareData(dates, orderedUsagesAmountByTime);

		double amount = projectAllocation.amount.doubleValue();
		int thresholdPercentage = alarm.map(x -> x.threshold).orElse(0);
		double threshold = thresholdPercentage > 0 ? amount * thresholdPercentage / 100 : 0;

		return ChartData.builder()
			.endTime(projectAllocation.resourceCredit.utcEndTime.toLocalDate())
			.projectAllocationName(projectAllocation.name)
			.unit(projectAllocation.resourceType.unit.getSuffix())
			.threshold(threshold)
			.chunks(chunks)
			.resourceUsages(usage)
			.times(dates)
			.thresholds(IntStream.range(0, dates.size()).boxed().map(x -> threshold).collect(toList()))
			.build();
	}

	private List<Double> prepareData(List<LocalDate> dates, Map<LocalDate, Double> orderedAmountsByTime) {
		double last = 0;
		List<Double> values = new ArrayList<>();
		for(LocalDate date : dates){
			double value = orderedAmountsByTime.getOrDefault(date, last);
			values.add(value);
			last = value;
		}
		return values;
	}
}
