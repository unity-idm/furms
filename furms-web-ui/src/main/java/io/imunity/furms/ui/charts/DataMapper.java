/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
class DataMapper {
	private final UserService userService;

	DataMapper(UserService userService) {
		this.userService = userService;
	}

	Map<LocalDate, Double> prepareTimedChunkAmounts(Set<ProjectAllocationChunk> allChunks) {
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

	Map<LocalDate, Double> prepareTimedUsageAmounts(Set<ResourceUsage> allResourceUsageHistory) {
		return allResourceUsageHistory.stream()
			.collect(toMap(usage -> usage.utcProbedAt.toLocalDate(), identity(), (usage, usage1) -> usage.utcProbedAt.isAfter(usage1.utcProbedAt) ? usage : usage1))
			.entrySet().stream()
			.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().cumulativeConsumption.doubleValue()));
	}

	Collection<Map<LocalDate, Double>> prepareTimedProjectsUsages(Set<ResourceUsage> allResourceUsageHistory) {
		return allResourceUsageHistory.stream()
			.collect(
				groupingBy(
					usage -> usage.projectAllocationId,
					collectingAndThen(
						toMap(usage ->
								usage.utcProbedAt.toLocalDate(),
							identity(),
							(usage, usage1) -> usage.utcProbedAt.isAfter(usage1.utcProbedAt) ? usage : usage1
						),
						usageMap -> usageMap.entrySet().stream()
							.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().cumulativeConsumption.doubleValue()))
					))
			).values();
	}

	Map<String, Map<LocalDate, Double>> prepareTimedUserUsagesGroupedByEmails(Set<UserResourceUsage> allUserResourceUsageHistory) {
		Map<String, String> userIdsToEmails = getUserIdsToEmails();
		return allUserResourceUsageHistory.stream()
			.collect(
				groupingBy(
					usage -> userIdsToEmails.getOrDefault(usage.fenixUserId.id, usage.fenixUserId.id),
					collectingAndThen(
						toMap(usage ->
								usage.utcConsumedUntil.toLocalDate(),
							identity(),
							(usage, usage1) -> usage.utcConsumedUntil.isAfter(usage1.utcConsumedUntil) ? usage : usage1
						),
						usageMap -> usageMap.entrySet().stream()
							.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().cumulativeConsumption.doubleValue()))
					)
				)
			);
	}

	double prepareThreshold(ProjectAllocationResolved projectAllocation, Optional<AlarmWithUserEmails> alarm) {
		double amount = projectAllocation.amount.doubleValue();
		int thresholdPercentage = alarm.map(x -> x.threshold).orElse(0);
		return thresholdPercentage > 0 ? amount * thresholdPercentage / 100 : 0;
	}

	private Map<String, String> getUserIdsToEmails() {
		return userService.getAllUsers().stream()
			.filter(user -> user.fenixUserId.isPresent())
			.collect(toMap(user -> user.fenixUserId.get().id, user -> user.email));
	}
}
