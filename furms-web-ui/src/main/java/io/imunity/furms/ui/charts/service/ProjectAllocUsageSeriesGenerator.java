/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import io.imunity.furms.domain.resource_usage.ResourceUsage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Component
class ProjectAllocUsageSeriesGenerator {

	List<Double> prepareYValuesForUserUsageLine(List<LocalDate> xArguments,
	                                            Set<ResourceUsage> allResourceUsageHistory,
	                                            LocalDate lastUsageDate,
	                                            LocalDate lastChunkDate,
	                                            LocalDate today) {

		Map<LocalDate, Double> usageValuesByProbedAtDates = prepareUsageValuesByProbedAtDates(allResourceUsageHistory);
		if (lastUsageDate == null)
			return List.of();

		LocalDate endUsageLineDate = chooseUsageLineEndDate(lastUsageDate, lastChunkDate, today);
		double lastValue = 0;
		List<Double> yValues = new ArrayList<>();
		for (LocalDate xArgument : xArguments) {
			if (xArgument.isAfter(endUsageLineDate))
				break;
			double value = usageValuesByProbedAtDates.getOrDefault(xArgument, lastValue);
			yValues.add(value);
			lastValue = value;
		}
		return yValues;
	}

	private LocalDate chooseUsageLineEndDate(LocalDate lastUsageDate, LocalDate lastChunkDate, LocalDate today) {
		LocalDate endTime;
		if (lastUsageDate.isBefore(today))
			endTime = today;
		else if (lastChunkDate != null && lastUsageDate.isAfter(lastChunkDate))
			endTime = lastChunkDate;
		else
			endTime = lastUsageDate;
		return endTime;
	}

	private Map<LocalDate, Double> prepareUsageValuesByProbedAtDates(Set<ResourceUsage> allResourceUsageHistory) {
		return allResourceUsageHistory.stream()
			.collect(toMap(usage -> usage.utcProbedAt.toLocalDate(), identity(), getOlderUsageMerger()))
			.entrySet().stream()
			.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().cumulativeConsumption.doubleValue()));
	}

	private BinaryOperator<ResourceUsage> getOlderUsageMerger() {
		return (usage, usage1) -> usage.utcProbedAt.isAfter(usage1.utcProbedAt) ? usage : usage1;
	}
}
