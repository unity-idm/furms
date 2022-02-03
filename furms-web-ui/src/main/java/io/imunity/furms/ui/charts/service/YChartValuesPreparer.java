/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
class YChartValuesPreparer {

	List<Double> prepareYValuesForAllocationChunkLine(List<LocalDate> xArguments, Map<LocalDate, Double> sumOfChunkValuesByValidToDates) {
		double lastValue = 0;
		List<Double> yValues = new ArrayList<>();
		for(LocalDate xArgument : xArguments){
			double value = sumOfChunkValuesByValidToDates.getOrDefault(xArgument, lastValue);
			yValues.add(value);
			lastValue = value;
		}
		return yValues;
	}

	List<Double> prepareYValuesForUserUsageLine(List<LocalDate> xArguments,
	                                            Map<LocalDate, Double> usageValuesByProbedAtDates,
	                                            LocalDate lastUsageDate,
	                                            LocalDate lastChunkDate,
	                                            LocalDate today) {
		if(lastUsageDate == null)
			return List.of();

		LocalDate endUsageLineDate = chooseUsageLineEndDate(lastUsageDate, lastChunkDate, today);
		double lastValue = 0;
		List<Double> yValues = new ArrayList<>();
		for(LocalDate xArgument : xArguments){
			if(xArgument.isAfter(endUsageLineDate))
				break;
			double value = usageValuesByProbedAtDates.getOrDefault(xArgument, lastValue);
			yValues.add(value);
			lastValue = value;
		}
		return yValues;
	}

	private LocalDate chooseUsageLineEndDate(LocalDate lastUsageDate, LocalDate lastChunkDate, LocalDate today) {
		LocalDate endTime;
		if(lastUsageDate.isBefore(today))
			endTime = today;
		else if(lastChunkDate != null && lastUsageDate.isAfter(lastChunkDate))
			endTime = lastChunkDate;
		else
			endTime = lastUsageDate;
		return endTime;
	}

	List<Double> prepareYValuesForThresholdLine(List<LocalDate> xArguments, double thresholdValue) {
		int size = xArguments.size() > 0 ? xArguments.size() + 1 : 0;
		return IntStream.range(0, size)
			.boxed()
			.map(x -> thresholdValue)
			.collect(toList());
	}

	List<Double> prepareYValuesForCommunityAllocationUsageLine(List<LocalDate> xArguments, Collection<Map<LocalDate, Double>> allocationsUsageValuesByProbedAtDates) {
		List<List<Double>> yValuesForEachProjectAllocation = allocationsUsageValuesByProbedAtDates.stream()
			.map(usageValuesAtTime -> prepareYPointsForUsageLine(xArguments, usageValuesAtTime))
			.collect(toList());

		return sumToOneLine(xArguments, yValuesForEachProjectAllocation);
	}

	private List<Double> sumToOneLine(List<LocalDate> xArguments, List<List<Double>> yValesForEachProjectAllocation) {
		List<Double> values = new ArrayList<>();
		for(int i = 0; i < xArguments.size(); i++) {
			double tmp = 0;
			for (List<Double> yPoints : yValesForEachProjectAllocation) {
				tmp += yPoints.get(i);
			}
			values.add(tmp);
		}
		return values;
	}

	List<UserUsage> prepareYValesForUsersUsagesLines(List<LocalDate> xArguments,
	                                                 Map<String, Map<LocalDate, Double>> groupedByEmailUsageValuesByProbedAtDate,
	                                                 LocalDate today) {
		return groupedByEmailUsageValuesByProbedAtDate.entrySet().stream()
			.map(usageMap -> new UserUsage(
				usageMap.getKey(),
				prepareYValesForUserUsageLine(xArguments, usageMap.getValue(), today))
			)
			.sorted(comparing(userUsage -> userUsage.userEmail))
			.collect(toList());
	}

	private static List<Double> prepareYPointsForUsageLine(List<LocalDate> xAxisPoints, Map<LocalDate, Double> usageValuesAtTime) {
		double lastValue = 0;
		List<Double> yValues = new ArrayList<>();
		for(LocalDate xPoint : xAxisPoints){
			double value = usageValuesAtTime.getOrDefault(xPoint, lastValue);
			yValues.add(value);
			lastValue = value;
		}
		return yValues;
	}

	private static List<Double> prepareYValesForUserUsageLine(List<LocalDate> xArguments,
	                                                          Map<LocalDate, Double> usageValuesByProbedAtDate,
	                                                          LocalDate today) {
		double lastValue = 0;
		List<Double> values = new ArrayList<>();
		for(LocalDate xArgument : xArguments){
			if(xArgument.isAfter(today))
				break;
			double value = usageValuesByProbedAtDate.getOrDefault(xArgument, lastValue);
			values.add(value);
			lastValue = value;
		}
		return values;
	}
}
