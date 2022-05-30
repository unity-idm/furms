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
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.ui.charts.ChartData;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static java.util.Comparator.comparing;


@Service
public class ChartPoweringService {
	private final ProjectAllocationService projectAllocationService;
	private final CommunityAllocationService communityAllocationService;
	private final AlarmService alarmService;
	private final ResourceUsageService resourceUsageService;
	private final XChartUnionArgumentsGenerator xChartUnionArgumentsGenerator;
	private final AlarmThresholdSeriesGenerator alarmThresholdSeriesGenerator;
	private final ChunkSeriesGenerator chunkSeriesGenerator;
	private final ProjectAllocUsageSeriesGenerator projectAllocUsageSeriesGenerator;
	private final UserUsageSeriesGenerator userUsageSeriesGenerator;
	private final CommunityAllocUsageSeriesGenerator communityAllocUsageSeriesGenerator;

	ChartPoweringService(ProjectAllocationService projectAllocationService,
	                     CommunityAllocationService communityAllocationService,
	                     AlarmService alarmService, ResourceUsageService resourceUsageService,
	                     XChartUnionArgumentsGenerator xChartUnionArgumentsGenerator,
	                     AlarmThresholdSeriesGenerator alarmThresholdSeriesGenerator,
	                     ChunkSeriesGenerator chunkSeriesGenerator,
	                     ProjectAllocUsageSeriesGenerator projectAllocUsageSeriesGenerator,
	                     UserUsageSeriesGenerator userUsageSeriesGenerator,
	                     CommunityAllocUsageSeriesGenerator communityAllocUsageSeriesGenerator) {
		this.projectAllocationService = projectAllocationService;
		this.communityAllocationService = communityAllocationService;
		this.alarmService = alarmService;
		this.resourceUsageService = resourceUsageService;
		this.xChartUnionArgumentsGenerator = xChartUnionArgumentsGenerator;
		this.alarmThresholdSeriesGenerator = alarmThresholdSeriesGenerator;
		this.chunkSeriesGenerator = chunkSeriesGenerator;
		this.projectAllocUsageSeriesGenerator = projectAllocUsageSeriesGenerator;
		this.userUsageSeriesGenerator = userUsageSeriesGenerator;
		this.communityAllocUsageSeriesGenerator = communityAllocUsageSeriesGenerator;
	}

	public ChartData getChartDataForProjectAlloc(ProjectId projectId, ProjectAllocationId projectAllocationId) {
		ProjectAllocationResolved projectAllocation =
			projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		Set<ProjectAllocationChunk> allChunks = projectAllocationService.findAllChunks(projectId, projectAllocationId);
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistory(projectId,
			projectAllocationId);
		Optional<AlarmWithUserEmails> alarm = alarmService.find(projectId, projectAllocationId);

		LocalDate lastChunkTime = getLastChunkDate(allChunks);
		LocalDate lastUsageTime = getLastUsageDate(allResourceUsageHistory);
		LocalDate today = getToday();

		List<LocalDate> xArguments = xChartUnionArgumentsGenerator.getXArgumentsForProjectAlloc(projectAllocation,
			allChunks, allResourceUsageHistory, lastChunkTime);

		double threshold = alarmThresholdSeriesGenerator.prepareThresholdValue(projectAllocation, alarm);
		List<Double> chunks = chunkSeriesGenerator.prepareYValuesForAllocationChunkLine(xArguments, allChunks);
		List<Double> usages = projectAllocUsageSeriesGenerator.prepareYValuesForUserUsageLine(xArguments,
			allResourceUsageHistory, lastUsageTime, lastChunkTime, today);
		List<Double> thresholdSeries = alarmThresholdSeriesGenerator.prepareYValuesForThresholdLine(xArguments,
			threshold);

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

	public ChartData getChartDataForProjectAllocWithUserUsages(ProjectId projectId, ProjectAllocationId projectAllocationId) {
		ProjectAllocationResolved projectAllocation =
			projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		Optional<AlarmWithUserEmails> alarm = alarmService.find(projectId, projectAllocationId);
		Set<ProjectAllocationChunk> allChunks = projectAllocationService.findAllChunks(projectId, projectAllocationId);
		Set<io.imunity.furms.domain.resource_usage.UserResourceUsage> allUserResourceUsageHistory = resourceUsageService.findAllUserUsagesHistory(projectId,
			projectAllocationId);
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistory(projectId,
			projectAllocationId);

		LocalDate lastChunkTime = getLastChunkDate(allChunks);
		LocalDate lastUsageTime = getLastUsageDate(allResourceUsageHistory);
		LocalDate today = getToday();

		List<LocalDate> xArguments =
			xChartUnionArgumentsGenerator.getXArgumentsForProjectAllocWithUserUsages(projectAllocation, allChunks,
				allUserResourceUsageHistory, allResourceUsageHistory, lastChunkTime);

		double threshold = alarmThresholdSeriesGenerator.prepareThresholdValue(projectAllocation, alarm);
		List<Double> chunks = chunkSeriesGenerator.prepareYValuesForAllocationChunkLine(xArguments, allChunks);
		List<Double> usages = projectAllocUsageSeriesGenerator.prepareYValuesForUserUsageLine(xArguments,
			allResourceUsageHistory, lastUsageTime, lastChunkTime, today);
		List<Double> thresholdSeries = alarmThresholdSeriesGenerator.prepareYValuesForThresholdLine(xArguments,
			threshold);
		List<UserResourceUsage> usersUsages = userUsageSeriesGenerator.prepareYValesForUsersUsagesLines(xArguments,
			allUserResourceUsageHistory, today);

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

	public ChartData getChartDataForCommunityAlloc(CommunityId communityId, CommunityAllocationId communityAllocationId) {
		CommunityAllocationResolved communityAllocation =
			communityAllocationService.findByIdWithRelatedObjects(communityAllocationId).get();
		Set<ResourceUsage> allResourceUsageHistory =
			resourceUsageService.findAllResourceUsageHistoryByCommunity(communityId, communityAllocationId);

		List<LocalDate> xArguments = xChartUnionArgumentsGenerator.getXArgumentsForCommunityAlloc(communityAllocation,
			allResourceUsageHistory);
		List<Double> usage =
			communityAllocUsageSeriesGenerator.prepareYValuesForCommunityAllocationUsageLine(xArguments,
				allResourceUsageHistory);

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
}
