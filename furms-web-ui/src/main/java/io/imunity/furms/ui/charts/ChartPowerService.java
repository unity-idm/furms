/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class ChartPowerService {
	private final ProjectAllocationService projectAllocationService;
	private final CommunityAllocationService communityAllocationService;
	private final AlarmService alarmService;
	private final ResourceUsageService resourceUsageService;
	private final UserService userService;

	ChartPowerService(ProjectAllocationService projectAllocationService, CommunityAllocationService communityAllocationService,
	                  AlarmService alarmService, ResourceUsageService resourceUsageService, UserService userService) {
		this.projectAllocationService = projectAllocationService;
		this.communityAllocationService = communityAllocationService;
		this.alarmService = alarmService;
		this.resourceUsageService = resourceUsageService;
		this.userService = userService;
	}

	public ChartData getChartDataForProjectAlloc(String projectId, String projectAllocationId){
		ProjectAllocationResolved projectAllocation = projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		Optional<AlarmWithUserEmails> alarm = alarmService.find(projectId, projectAllocationId);

		Set<ProjectAllocationChunk> allChunks = projectAllocationService.findAllChunks(projectId, projectAllocationId);
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistory(projectId, projectAllocationId);

		Optional<ProjectAllocationChunk> lastChunk = allChunks.stream()
			.max(comparing(chunk -> chunk.validFrom.toLocalDate().toEpochDay()));

		List<LocalDate> dates = Stream.of(
			Stream.of(convertToUTCTime(ZonedDateTime.now()).toLocalDate(), projectAllocation.resourceCredit.utcStartTime.toLocalDate().minusDays(1), projectAllocation.resourceCredit.utcStartTime.toLocalDate()),
			allChunks.stream().map(chunk -> chunk.validFrom.toLocalDate()),
			allResourceUsageHistory.stream().map(usage -> usage.utcProbedAt.toLocalDate())
		)
			.flatMap(identity())
			.distinct()
			.filter(date -> lastChunk.map(chunk -> chunk.validTo.toLocalDate().isAfter(date) || chunk.validTo.toLocalDate().isEqual(date)).orElse(false))
			.sorted(comparing(identity()))
			.collect(toList());

		Map<LocalDate, Double> orderedChunksAmountByTime = prepareChunks(allChunks);
		Map<LocalDate, Double> orderedUsagesAmountByTime = allResourceUsageHistory.stream()
			.collect(toMap(usage -> usage.utcProbedAt.toLocalDate(), identity(), (usage, usage1) -> usage.utcProbedAt.isAfter(usage1.utcProbedAt) ? usage : usage1))
			.entrySet().stream()
			.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().cumulativeConsumption.doubleValue()));
		List<Double> usage = prepareData(dates, orderedUsagesAmountByTime);
		List<Double> chunks = prepareData(dates, orderedChunksAmountByTime);
		lastChunk.ifPresent(chunk -> {
			chunks.add(chunks.get(chunks.size() - 1));
			dates.add(chunk.validTo.toLocalDate());
		});

		double threshold = getThreshold(projectAllocation, alarm);

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

	public ChartData getChartDataForProjectAllocWithUserUsages(String projectId, String projectAllocationId){
		Map<String, String> userIdsToEmails = userService.getAllUsers().stream()
			.filter(user -> user.fenixUserId.isPresent())
			.collect(toMap(user -> user.fenixUserId.get().id, user -> user.email));

		ProjectAllocationResolved projectAllocation = projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId).get();
		Optional<AlarmWithUserEmails> alarm = alarmService.find(projectId, projectAllocationId);

		Set<ProjectAllocationChunk> allChunks = projectAllocationService.findAllChunks(projectId, projectAllocationId);
		Set<UserResourceUsage> allUserResourceUsageHistory = resourceUsageService.findAllUserUsagesHistory(projectId, projectAllocationId);
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistory(projectId, projectAllocationId);

		Optional<ProjectAllocationChunk> lastChunk = allChunks.stream()
			.max(comparing(chunk -> chunk.validFrom.toLocalDate().toEpochDay()));

		List<LocalDate> dates = Stream.of(
				Stream.of(convertToUTCTime(ZonedDateTime.now()).toLocalDate(), projectAllocation.resourceCredit.utcStartTime.toLocalDate().minusDays(1), projectAllocation.resourceCredit.utcStartTime.toLocalDate()),
				allResourceUsageHistory.stream().map(usage -> usage.utcProbedAt.toLocalDate()),
				allChunks.stream().map(chunk -> chunk.validFrom.toLocalDate()),
				allUserResourceUsageHistory.stream().map(usage -> usage.utcConsumedUntil.toLocalDate())
			)
			.flatMap(identity())
			.distinct()
			.filter(date -> lastChunk.map(chunk -> chunk.validTo.toLocalDate().isAfter(date) || chunk.validTo.toLocalDate().isEqual(date)).orElse(false))
			.sorted(comparing(identity()))
			.collect(toList());

		Map<LocalDate, Double> orderedChunksAmountByTime = prepareChunks(allChunks);
		Map<LocalDate, Double> orderedUsagesAmountByTime = allResourceUsageHistory.stream()
			.collect(toMap(usage -> usage.utcProbedAt.toLocalDate(), identity(), (usage, usage1) -> usage.utcProbedAt.isAfter(usage1.utcProbedAt) ? usage : usage1))
			.entrySet().stream()
			.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().cumulativeConsumption.doubleValue()));
		List<UserUsage> usersUsages = prepareUserUsages(userIdsToEmails, allUserResourceUsageHistory, dates);
		List<Double> usages = prepareData(dates, orderedUsagesAmountByTime);
		List<Double> chunks = prepareData(dates, orderedChunksAmountByTime);
		lastChunk.ifPresent(chunk -> {
			chunks.add(chunks.get(chunks.size() - 1));
			dates.add(chunk.validTo.toLocalDate());
		});

		double threshold = getThreshold(projectAllocation, alarm);

		return ChartData.builder()
			.endTime(projectAllocation.resourceCredit.utcEndTime.toLocalDate())
			.projectAllocationName(projectAllocation.name)
			.unit(projectAllocation.resourceType.unit.getSuffix())
			.threshold(threshold)
			.chunks(chunks)
			.resourceUsages(usages)
			.times(dates)
			.thresholds(IntStream.range(0, dates.size()).boxed().map(x -> threshold).collect(toList()))
			.usersUsages(usersUsages)
			.build();
	}

	private double getThreshold(ProjectAllocationResolved projectAllocation, Optional<AlarmWithUserEmails> alarm) {
		double amount = projectAllocation.amount.doubleValue();
		int thresholdPercentage = alarm.map(x -> x.threshold).orElse(0);
		return thresholdPercentage > 0 ? amount * thresholdPercentage / 100 : 0;
	}

	private List<UserUsage> prepareUserUsages(Map<String, String> userIdsToEmails, Set<UserResourceUsage> allUserResourceUsageHistory, List<LocalDate> dates) {
		return allUserResourceUsageHistory.stream()
			.collect(
				groupingBy(
					usage -> usage.fenixUserId,
					toMap(usage ->
							usage.utcConsumedUntil.toLocalDate(),
						identity(),
						(usage, usage1) -> usage.utcConsumedUntil.isAfter(usage1.utcConsumedUntil) ? usage : usage1
					)
				)
			).entrySet().stream()
			.map(usageMap -> new UserUsage(
				userIdsToEmails.getOrDefault(usageMap.getKey().id, usageMap.getKey().id),
				prepareData(
					dates,
					usageMap.getValue()
						.entrySet().stream()
						.collect(toMap(
							Map.Entry::getKey,
							entry -> entry.getValue().cumulativeConsumption.doubleValue())
						)
				)))
			.sorted(comparing(userUsage -> userUsage.email))
			.collect(toList());
	}

	public ChartData getChartDataForCommunityAlloc(String communityId, String communityAllocationId){
		CommunityAllocationResolved communityAllocation = communityAllocationService.findByIdWithRelatedObjects(communityAllocationId).get();
		Set<ResourceUsage> allResourceUsageHistory = resourceUsageService.findAllResourceUsageHistoryByCommunity(communityId, communityAllocationId);

		List<LocalDate> dates = Stream.of(
				Stream.of(communityAllocation.resourceCredit.utcStartTime.toLocalDate()),
				allResourceUsageHistory.stream().map(usage -> usage.utcProbedAt.toLocalDate())
			)
			.flatMap(identity())
			.distinct()
			.sorted(comparing(identity()))
			.collect(toList());

		List<List<Double>> orderedUsagesGroupedByAllocationId = allResourceUsageHistory.stream()
			.collect(
				groupingBy(
					usage -> usage.projectAllocationId,
					toMap(usage ->
							usage.utcProbedAt.toLocalDate(),
						identity(),
						(usage, usage1) -> usage.utcProbedAt.isAfter(usage1.utcProbedAt) ? usage : usage1
					)
				)
			).values().stream()
			.map(usageMap -> usageMap.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().cumulativeConsumption.doubleValue()))
			)
			.map(usageMap -> prepareData(dates, usageMap))
			.collect(toList());

		List<Double> usage = new ArrayList<>();
		for(int i = 0; i < dates.size(); i++) {
			double tmp = 0;
			for (List<Double> doubles : orderedUsagesGroupedByAllocationId) {
				tmp += doubles.get(i);
			}
			usage.add(tmp);
		}
		return ChartData.builder()
			.endTime(communityAllocation.resourceCredit.utcEndTime.toLocalDate())
			.projectAllocationName(communityAllocation.name)
			.unit(communityAllocation.resourceType.unit.getSuffix())
			.threshold(0)
			.chunks(List.of())
			.resourceUsages(usage)
			.times(dates)
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

	private Map<LocalDate, Double> prepareChunks(Set<ProjectAllocationChunk> allChunks) {
		List<ProjectAllocationChunk> orderedChunks = allChunks.stream()
			.sorted(comparing(x -> x.validFrom))
			.collect(toList());

		double last = 0;
		Map<LocalDate, Double> map = new HashMap<>();
		for(ProjectAllocationChunk chunk : orderedChunks){
			map.put(chunk.validFrom.toLocalDate(), chunk.amount.doubleValue() + last);
			last = chunk.amount.doubleValue() + last;
		}
		return map;
	}
}
