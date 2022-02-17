/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import io.imunity.furms.api.users.UserService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
class UserUsageSeriesGenerator {
	private final UserService userService;

	UserUsageSeriesGenerator(UserService userService) {
		this.userService = userService;
	}

	List<UserResourceUsage> prepareYValesForUsersUsagesLines(List<LocalDate> xArguments,
	                                                         Set<io.imunity.furms.domain.resource_usage.UserResourceUsage> allUserResourceUsageHistory,
	                                                         LocalDate today) {
		Map<String, Map<LocalDate, Double>> groupedByEmailUsageValuesByProbedAtDate =
			prepareGroupedByEmailUsageValuesByProbedAtDate(allUserResourceUsageHistory);

		return groupedByEmailUsageValuesByProbedAtDate.entrySet().stream()
			.map(usageMap -> new UserResourceUsage(
				usageMap.getKey(),
				prepareYValesForUserUsageLine(xArguments, usageMap.getValue(), today))
			)
			.sorted(comparing(userUsage -> userUsage.userEmail))
			.collect(toList());
	}

	private static List<Double> prepareYValesForUserUsageLine(List<LocalDate> xArguments,
	                                                          Map<LocalDate, Double> usageValuesByProbedAtDate,
	                                                          LocalDate today) {
		double lastValue = 0;
		List<Double> values = new ArrayList<>();
		for (LocalDate xArgument : xArguments) {
			if (xArgument.isAfter(today))
				break;
			double value = usageValuesByProbedAtDate.getOrDefault(xArgument, lastValue);
			values.add(value);
			lastValue = value;
		}
		return values;
	}

	private Map<String, Map<LocalDate, Double>> prepareGroupedByEmailUsageValuesByProbedAtDate(Set<io.imunity.furms.domain.resource_usage.UserResourceUsage> allUserResourceUsageHistory) {
		Map<String, String> userIdsToEmails = getUserIdsToEmails();
		return allUserResourceUsageHistory.stream()
			.collect(
				groupingBy(
					usage -> userIdsToEmails.getOrDefault(usage.fenixUserId.id, usage.fenixUserId.id),
					collectingAndThen(
						toMap(
							usage -> usage.utcConsumedUntil.toLocalDate(),
							identity(),
							getOlderUserUsageMerger()
						),
						usageMap -> usageMap.entrySet().stream()
							.collect(toMap(Map.Entry::getKey,
								entry -> entry.getValue().cumulativeConsumption.doubleValue()))
					)
				)
			);
	}

	private BinaryOperator<io.imunity.furms.domain.resource_usage.UserResourceUsage> getOlderUserUsageMerger() {
		return (usage, usage1) -> usage.utcConsumedUntil.isAfter(usage1.utcConsumedUntil) ? usage : usage1;
	}

	private Map<String, String> getUserIdsToEmails() {
		return userService.getAllUsers().stream()
			.filter(user -> user.fenixUserId.isPresent())
			.collect(toMap(user -> user.fenixUserId.get().id, user -> user.email));
	}
}
