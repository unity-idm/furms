/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.ui.utils.NotificationUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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
	private final ProjectAllocationService projectAllocationService;
	private final AlarmService alarmService;
	private final ResourceUsageService resourceUsageService;
	private final ObjectMapper objectMapper;

	ChartPowerService(ProjectAllocationService projectAllocationService, AlarmService alarmService, ResourceUsageService resourceUsageService) {
		this.projectAllocationService = projectAllocationService;
		this.alarmService = alarmService;
		this.resourceUsageService = resourceUsageService;
		this.objectMapper = new ObjectMapper();
		objectMapper.findAndRegisterModules();
	}

	public ChartData getChartData(String projectId, String projectAllocationId){
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

	public byte[] getJsonFile(String projectId, String projectAllocationId) {
		ProjectAllocationResolved projectAllocation = projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		List<Consumption> consumption = resourceUsageService.findAllResourceUsageHistory(projectId, projectAllocationId).stream()
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

	public byte[] getCsvFile(String projectId, String projectAllocationId) {
		String header = "Allocation,Consumption until,Amount,Unit";
		ProjectAllocationResolved projectAllocation = projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistory(projectId, projectAllocationId);
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
}
