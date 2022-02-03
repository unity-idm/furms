/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

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
import io.imunity.furms.ui.charts.ChartData;
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
	private final DataPreparer dataPreparer;
	private final XChartArgumentsPreparer xChartArgumentsPreparer;
	private final YChartValuesPreparer yChartValuesPreparer;

	ChartPowerService(ProjectAllocationService projectAllocationService, CommunityAllocationService communityAllocationService,
	                  AlarmService alarmService, ResourceUsageService resourceUsageService, DataPreparer dataPreparer,
	                  XChartArgumentsPreparer xChartArgumentsPreparer, YChartValuesPreparer yChartValuesPreparer) {
		this.projectAllocationService = projectAllocationService;
		this.communityAllocationService = communityAllocationService;
		this.alarmService = alarmService;
		this.resourceUsageService = resourceUsageService;
		this.dataPreparer = dataPreparer;
		this.xChartArgumentsPreparer = xChartArgumentsPreparer;
		this.yChartValuesPreparer = yChartValuesPreparer;
	}

	public ChartData getChartDataForProjectAlloc(String projectId, String projectAllocationId){
		ProjectAllocationResolved projectAllocation = projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		Set<ProjectAllocationChunk> allChunks = projectAllocationService.findAllChunks(projectId, projectAllocationId);
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistory(projectId, projectAllocationId);
		Optional<AlarmWithUserEmails> alarm = alarmService.find(projectId, projectAllocationId);

		Map<LocalDate, Double> sumOfChunkValuesByValidToDates = dataPreparer.prepareSumOfChunkValuesByValidToDates(allChunks);
		Map<LocalDate, Double> usageValuesByProbedAtDates = dataPreparer.prepareUsageValuesByProbedAtDates(allResourceUsageHistory);
		LocalDate lastChunkTime = getLastChunkDate(allChunks);
		LocalDate lastUsageTime = getLastUsageDate(allResourceUsageHistory);

		LocalDate today = getToday();
		List<LocalDate> xArguments = getXAxisForProjectAlloc(projectAllocation, allChunks, allResourceUsageHistory, lastChunkTime, today);

		double threshold = dataPreparer.prepareThresholdValue(projectAllocation, alarm);
		List<Double> chunks = yChartValuesPreparer.prepareYValuesForAllocationChunkLine(xArguments, sumOfChunkValuesByValidToDates);
		List<Double> usages = yChartValuesPreparer.prepareYValuesForUserUsageLine(xArguments, usageValuesByProbedAtDates, lastUsageTime, lastChunkTime, today);
		List<Double> thresholdSeries = yChartValuesPreparer.prepareYValuesForThresholdLine(xArguments, threshold);

		return ChartData.builder()
			.endTime(projectAllocation.resourceCredit.utcEndTime.toLocalDate())
			.projectAllocationName(projectAllocation.name)
			.unit(projectAllocation.resourceType.unit.getSuffix())
			.yChunkLineValues(chunks)
			.yResourceUsageLineValues(usages)
			.xArguments(xArguments)
			.yThresholdLineValues(thresholdSeries)
			.build();
	}

	public ChartData getChartDataForProjectAllocWithUserUsages(String projectId, String projectAllocationId){
		ProjectAllocationResolved projectAllocation = projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		Optional<AlarmWithUserEmails> alarm = alarmService.find(projectId, projectAllocationId);
		Set<ProjectAllocationChunk> allChunks = projectAllocationService.findAllChunks(projectId, projectAllocationId);
		Set<UserResourceUsage> allUserResourceUsageHistory = resourceUsageService.findAllUserUsagesHistory(projectId, projectAllocationId);
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistory(projectId, projectAllocationId);

		Map<LocalDate, Double> timedChunkAmounts = dataPreparer.prepareSumOfChunkValuesByValidToDates(allChunks);
		Map<LocalDate, Double> timedUsageAmounts = dataPreparer.prepareUsageValuesByProbedAtDates(allResourceUsageHistory);
		Map<String, Map<LocalDate, Double>> groupedByEmailUsageValuesByProbedAtDate = dataPreparer.prepareGroupedByEmailUsageValuesByProbedAtDate(allUserResourceUsageHistory);
		LocalDate lastChunkTime = getLastChunkDate(allChunks);
		LocalDate lastUsageTime = getLastUsageDate(allResourceUsageHistory);

		LocalDate today = getToday();
		List<LocalDate> xArguments = getXArgumentsForProjectAllocWithUserUsages(projectAllocation, allChunks, allUserResourceUsageHistory, allResourceUsageHistory, lastChunkTime, today);

		double threshold = dataPreparer.prepareThresholdValue(projectAllocation, alarm);
		List<Double> chunks = yChartValuesPreparer.prepareYValuesForAllocationChunkLine(xArguments, timedChunkAmounts);
		List<Double> usages = yChartValuesPreparer.prepareYValuesForUserUsageLine(xArguments, timedUsageAmounts, lastUsageTime, lastChunkTime, today);
		List<Double> thresholdSeries = yChartValuesPreparer.prepareYValuesForThresholdLine(xArguments, threshold);
		List<UserUsage> usersUsages = yChartValuesPreparer.prepareYValesForUsersUsagesLines(xArguments, groupedByEmailUsageValuesByProbedAtDate, today);

		return ChartData.builder()
			.endTime(projectAllocation.resourceCredit.utcEndTime.toLocalDate())
			.projectAllocationName(projectAllocation.name)
			.unit(projectAllocation.resourceType.unit.getSuffix())
			.yChunkLineValues(chunks)
			.yResourceUsageLineValues(usages)
			.xArguments(xArguments)
			.yThresholdLineValues(thresholdSeries)
			.yUsersUsagesValues(usersUsages)
			.build();
	}

	public ChartData getChartDataForCommunityAlloc(String communityId, String communityAllocationId){
		CommunityAllocationResolved communityAllocation = communityAllocationService.findByIdWithRelatedObjects(communityAllocationId).get();
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistoryByCommunity(communityId, communityAllocationId);

		LocalDate today = getToday();
		List<LocalDate> xArguments = getXArgumentsForCommunityAlloc(communityAllocation, allResourceUsageHistory, today);
		Collection<Map<LocalDate, Double>> allocationsUsageValuesByProbedAtDates = dataPreparer.prepareAllocationsUsageValuesByProbedAtDates(allResourceUsageHistory);
		List<Double> usage = yChartValuesPreparer.prepareYValuesForCommunityAllocationUsageLine(xArguments, allocationsUsageValuesByProbedAtDates);

		return ChartData.builder()
			.endTime(communityAllocation.resourceCredit.utcEndTime.toLocalDate())
			.projectAllocationName(communityAllocation.name)
			.unit(communityAllocation.resourceType.unit.getSuffix())
			.yChunkLineValues(List.of())
			.yResourceUsageLineValues(usage)
			.xArguments(xArguments)
			.build();
	}

	private LocalDate getLastUsageDate(Set<ResourceUsage> allResourceUsageHistory) {
		return allResourceUsageHistory.stream()
			.map(x -> x.utcProbedAt.toLocalDate())
			.max(comparing(LocalDate::toEpochDay))
			.orElse(null);
	}

	private LocalDate getLastChunkDate(Set<ProjectAllocationChunk> allChunks) {
		return allChunks.stream()
			.map(x -> x.validTo.toLocalDate())
			.max(comparing(LocalDate::toEpochDay))
			.orElse(null);
	}

	private LocalDate getToday() {
		return convertToUTCTime(ZonedDateTime.now()).toLocalDate();
	}

	private List<LocalDate> getXAxisForProjectAlloc(ProjectAllocationResolved projectAllocation, Set<ProjectAllocationChunk> allChunks, Set<ResourceUsage> allResourceUsageHistory, LocalDate lastChunkTime, LocalDate today) {
		return xChartArgumentsPreparer.prepareArguments(
			projectAllocation.resourceCredit.utcStartTime.toLocalDate(),
			today,
			lastChunkTime,
			Stream.of(
				allChunks.stream().map(chunk -> chunk.validFrom.toLocalDate()).collect(Collectors.toSet()),
				allResourceUsageHistory.stream().map(usage -> usage.utcProbedAt.toLocalDate()).collect(Collectors.toSet())
			).flatMap(Collection::stream)
				.collect(Collectors.toSet())
		);
	}

	private List<LocalDate> getXArgumentsForProjectAllocWithUserUsages(ProjectAllocationResolved projectAllocation, Set<ProjectAllocationChunk> allChunks, Set<UserResourceUsage> allUserResourceUsageHistory, Set<ResourceUsage> allResourceUsageHistory, LocalDate lastChunkTime, LocalDate today) {
		return xChartArgumentsPreparer.prepareArguments(
			projectAllocation.resourceCredit.utcStartTime.toLocalDate(),
			today,
			lastChunkTime,
			Stream.of(
				allChunks.stream().map(chunk -> chunk.validFrom.toLocalDate()).collect(Collectors.toSet()),
				allResourceUsageHistory.stream().map(usage -> usage.utcProbedAt.toLocalDate()).collect(Collectors.toSet()),
				allUserResourceUsageHistory.stream().map(usage -> usage.utcConsumedUntil.toLocalDate()).collect(Collectors.toSet())
			).flatMap(Collection::stream)
				.collect(Collectors.toSet())
		);
	}

	private List<LocalDate> getXArgumentsForCommunityAlloc(CommunityAllocationResolved communityAllocation, Set<ResourceUsage> allResourceUsageHistory, LocalDate today) {
		return xChartArgumentsPreparer.prepareArguments(
			communityAllocation.resourceCredit.utcStartTime.toLocalDate(),
			today,
			null,
			allResourceUsageHistory.stream().map(usage -> usage.utcProbedAt.toLocalDate()).collect(Collectors.toSet())
		);
	}
}
