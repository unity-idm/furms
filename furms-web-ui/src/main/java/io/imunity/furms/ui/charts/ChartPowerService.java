/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static java.util.Comparator.comparing;

@Service
public class ChartPowerService {
	private final ProjectAllocationService projectAllocationService;
	private final CommunityAllocationService communityAllocationService;
	private final AlarmService alarmService;
	private final ResourceUsageService resourceUsageService;
	private final DataMapper dataMapper;
	private final XSeriesPreparer xSeriesPreparer;
	private final YSeriesPreparer ySeriesPreparer;

	ChartPowerService(ProjectAllocationService projectAllocationService, CommunityAllocationService communityAllocationService,
	                  AlarmService alarmService, ResourceUsageService resourceUsageService, DataMapper dataMapper,
	                  XSeriesPreparer xSeriesPreparer, YSeriesPreparer ySeriesPreparer) {
		this.projectAllocationService = projectAllocationService;
		this.communityAllocationService = communityAllocationService;
		this.alarmService = alarmService;
		this.resourceUsageService = resourceUsageService;
		this.dataMapper = dataMapper;
		this.xSeriesPreparer = xSeriesPreparer;
		this.ySeriesPreparer = ySeriesPreparer;
	}

	public ChartData getChartDataForProjectAlloc(String projectId, String projectAllocationId){
		ProjectAllocationResolved projectAllocation = projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		Set<ProjectAllocationChunk> allChunks = projectAllocationService.findAllChunks(projectId, projectAllocationId);
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistory(projectId, projectAllocationId);
		Optional<AlarmWithUserEmails> alarm = alarmService.find(projectId, projectAllocationId);

		Map<LocalDate, Double> timedChunkAmounts = dataMapper.prepareTimedChunkAmounts(allChunks);
		Map<LocalDate, Double> timedUsageAmounts = dataMapper.prepareTimedUsageAmounts(allResourceUsageHistory);
		LocalDate lastChunkTime = getLastChunkTime(allChunks);
		LocalDate lastUsageTime = getLastUsageTime(allResourceUsageHistory);

		LocalDate today = getToday();
		List<LocalDate> xAxis = getXAxisForProjectAlloc(projectAllocation, allChunks, allResourceUsageHistory, lastChunkTime, today);

		double threshold = dataMapper.prepareThreshold(projectAllocation, alarm);
		List<Double> chunks = ySeriesPreparer.prepareChunkSeries(xAxis, timedChunkAmounts);
		List<Double> usages = ySeriesPreparer.prepareUsagesSeries(xAxis, timedUsageAmounts, lastUsageTime, lastChunkTime, today);
		List<Double> thresholdSeries = ySeriesPreparer.prepareThresholdSeries(xAxis, threshold);

		return ChartData.builder()
			.endTime(projectAllocation.resourceCredit.utcEndTime.toLocalDate())
			.projectAllocationName(projectAllocation.name)
			.unit(projectAllocation.resourceType.unit.getSuffix())
			.threshold(threshold)
			.chunks(chunks)
			.resourceUsages(usages)
			.times(xAxis)
			.thresholds(thresholdSeries)
			.build();
	}

	public ChartData getChartDataForProjectAllocWithUserUsages(String projectId, String projectAllocationId){
		ProjectAllocationResolved projectAllocation = projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		Optional<AlarmWithUserEmails> alarm = alarmService.find(projectId, projectAllocationId);
		Set<ProjectAllocationChunk> allChunks = projectAllocationService.findAllChunks(projectId, projectAllocationId);
		Set<UserResourceUsage> allUserResourceUsageHistory = resourceUsageService.findAllUserUsagesHistory(projectId, projectAllocationId);
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistory(projectId, projectAllocationId);

		Map<LocalDate, Double> timedChunkAmounts = dataMapper.prepareTimedChunkAmounts(allChunks);
		Map<LocalDate, Double> timedUsageAmounts = dataMapper.prepareTimedUsageAmounts(allResourceUsageHistory);
		Map<String, Map<LocalDate, Double>> timedUserUsagesGroupedByEmail = dataMapper.prepareTimedUserUsagesGroupedByEmails(allUserResourceUsageHistory);
		LocalDate lastChunkTime = getLastChunkTime(allChunks);
		LocalDate lastUsageTime = getLastUsageTime(allResourceUsageHistory);

		LocalDate today = getToday();
		List<LocalDate> xAxis = getXAxisForProjectAllocWithUserUsages(projectAllocation, allChunks, allUserResourceUsageHistory, allResourceUsageHistory, lastChunkTime, today);

		double threshold = dataMapper.prepareThreshold(projectAllocation, alarm);
		List<Double> chunks = ySeriesPreparer.prepareChunkSeries(xAxis, timedChunkAmounts);
		List<Double> usages = ySeriesPreparer.prepareUsagesSeries(xAxis, timedUsageAmounts, lastUsageTime, lastChunkTime, today);
		List<Double> thresholdSeries = ySeriesPreparer.prepareThresholdSeries(xAxis, threshold);
		List<UserUsage> usersUsages = ySeriesPreparer.prepareUserUsagesSeries(xAxis, timedUserUsagesGroupedByEmail, today);

		return ChartData.builder()
			.endTime(projectAllocation.resourceCredit.utcEndTime.toLocalDate())
			.projectAllocationName(projectAllocation.name)
			.unit(projectAllocation.resourceType.unit.getSuffix())
			.threshold(threshold)
			.chunks(chunks)
			.resourceUsages(usages)
			.times(xAxis)
			.thresholds(thresholdSeries)
			.usersUsages(usersUsages)
			.build();
	}

	public ChartData getChartDataForCommunityAlloc(String communityId, String communityAllocationId){
		CommunityAllocationResolved communityAllocation = communityAllocationService.findByIdWithRelatedObjects(communityAllocationId).get();
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistoryByCommunity(communityId, communityAllocationId);

		LocalDate today = getToday();
		List<LocalDate> xAxisTimes = getXAxisForCommunityAlloc(communityAllocation, allResourceUsageHistory, today);
		Collection<Map<LocalDate, Double>> timedProjectsUsages = dataMapper.prepareTimedProjectsUsages(allResourceUsageHistory);
		List<Double> usage = ySeriesPreparer.prepareProjectsUsageSeries(xAxisTimes, timedProjectsUsages);

		return ChartData.builder()
			.endTime(communityAllocation.resourceCredit.utcEndTime.toLocalDate())
			.projectAllocationName(communityAllocation.name)
			.unit(communityAllocation.resourceType.unit.getSuffix())
			.threshold(0)
			.chunks(List.of())
			.resourceUsages(usage)
			.times(xAxisTimes)
			.build();
	}

	private LocalDate getLastUsageTime(Set<ResourceUsage> allResourceUsageHistory) {
		return allResourceUsageHistory.stream()
			.map(x -> x.utcProbedAt.toLocalDate())
			.max(comparing(LocalDate::toEpochDay))
			.orElse(null);
	}

	private LocalDate getLastChunkTime(Set<ProjectAllocationChunk> allChunks) {
		return allChunks.stream()
			.map(x -> x.validTo.toLocalDate())
			.max(comparing(LocalDate::toEpochDay))
			.orElse(null);
	}

	private LocalDate getToday() {
		return convertToUTCTime(ZonedDateTime.now()).toLocalDate();
	}

	private List<LocalDate> getXAxisForProjectAlloc(ProjectAllocationResolved projectAllocation, Set<ProjectAllocationChunk> allChunks, Set<ResourceUsage> allResourceUsageHistory, LocalDate lastChunkTime, LocalDate today) {
		return xSeriesPreparer.prepareSumOfAllDataTimesForXAxis(
			projectAllocation.resourceCredit.utcStartTime.toLocalDate(),
			today,
			lastChunkTime,
			Set.of(
				allChunks.stream().map(chunk -> chunk.validFrom.toLocalDate()).collect(Collectors.toSet()),
				allResourceUsageHistory.stream().map(usage -> usage.utcProbedAt.toLocalDate()).collect(Collectors.toSet())
			)
		);
	}

	private List<LocalDate> getXAxisForProjectAllocWithUserUsages(ProjectAllocationResolved projectAllocation, Set<ProjectAllocationChunk> allChunks, Set<UserResourceUsage> allUserResourceUsageHistory, Set<ResourceUsage> allResourceUsageHistory, LocalDate lastChunkTime, LocalDate today) {
		return xSeriesPreparer.prepareSumOfAllDataTimesForXAxis(
			projectAllocation.resourceCredit.utcStartTime.toLocalDate(),
			today,
			lastChunkTime,
			Stream.of(
				allChunks.stream().map(chunk -> chunk.validFrom.toLocalDate()).collect(Collectors.toSet()),
				allResourceUsageHistory.stream().map(usage -> usage.utcProbedAt.toLocalDate()).collect(Collectors.toSet()),
				allUserResourceUsageHistory.stream().map(usage -> usage.utcConsumedUntil.toLocalDate()).collect(Collectors.toSet())
			).collect(Collectors.toSet())
		);
	}

	private List<LocalDate> getXAxisForCommunityAlloc(CommunityAllocationResolved communityAllocation, Set<ResourceUsage> allResourceUsageHistory, LocalDate today) {
		return xSeriesPreparer.prepareSumOfAllDataTimesForXAxis(
			communityAllocation.resourceCredit.utcStartTime.toLocalDate(),
			today,
			null,
			Set.of(allResourceUsageHistory.stream().map(usage -> usage.utcProbedAt.toLocalDate()).collect(Collectors.toSet()))
		);
	}
}
