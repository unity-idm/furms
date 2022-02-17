/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import io.imunity.furms.domain.resource_usage.ResourceUsage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
class CommunityAllocUsageSeriesGenerator {

	List<Double> prepareYValuesForCommunityAllocationUsageLine(List<LocalDate> xArguments,
	                                                           Set<ResourceUsage> allResourceUsageHistory) {
		Collection<Map<LocalDate, Double>> allocationsUsageValuesByProbedAtDates =
			prepareAllocationsUsageValuesByProbedAtDates(allResourceUsageHistory);
		List<List<Double>> yValuesForEachProjectAllocation = allocationsUsageValuesByProbedAtDates.stream()
			.map(usageValuesAtTime -> prepareYPointsForUsageLine(xArguments, usageValuesAtTime))
			.collect(toList());

		return sumToOneLine(xArguments, yValuesForEachProjectAllocation);
	}

	private Collection<Map<LocalDate, Double>> prepareAllocationsUsageValuesByProbedAtDates(Set<ResourceUsage> allResourceUsageHistory) {
		Map<String, Map<LocalDate, Double>> usageDataByAllocationId = allResourceUsageHistory.stream()
			.collect(
				groupingBy(
					usage -> usage.projectAllocationId,
					collectingAndThen(
						toMap(usage ->
								usage.utcProbedAt.toLocalDate(),
							identity(),
							getOlderUsageMerger()
						),
						usageMap -> usageMap.entrySet().stream()
							.collect(toMap(Map.Entry::getKey,
								entry -> entry.getValue().cumulativeConsumption.doubleValue()))
					))
			);
		return usageDataByAllocationId.values();

	}

	private BinaryOperator<ResourceUsage> getOlderUsageMerger() {
		return (usage, usage1) -> usage.utcProbedAt.isAfter(usage1.utcProbedAt) ? usage : usage1;
	}

	private List<Double> prepareYPointsForUsageLine(List<LocalDate> xAxisPoints,
	                                                       Map<LocalDate, Double> usageValuesAtTime) {
		double lastValue = 0;
		List<Double> yValues = new ArrayList<>();
		for (LocalDate xPoint : xAxisPoints) {
			double value = usageValuesAtTime.getOrDefault(xPoint, lastValue);
			yValues.add(value);
			lastValue = value;
		}
		return yValues;
	}

	private List<Double> sumToOneLine(List<LocalDate> xArguments, List<List<Double>> yValesForEachProjectAllocation) {
		List<Double> values = new ArrayList<>();
		for (int i = 0; i < xArguments.size(); i++) {
			double tmp = 0;
			for (List<Double> yPoints : yValesForEachProjectAllocation) {
				tmp += yPoints.get(i);
			}
			values.add(tmp);
		}
		return values;
	}
}
