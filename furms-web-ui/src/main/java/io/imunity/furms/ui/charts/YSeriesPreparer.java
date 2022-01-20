/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

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
class YSeriesPreparer {

	List<Double> prepareChunkSeries(List<LocalDate> xAsisTimes, Map<LocalDate, Double> chunksAmountsByTime) {
		double last = 0;
		List<Double> values = new ArrayList<>();
		for(LocalDate date : xAsisTimes){
			double value = chunksAmountsByTime.getOrDefault(date, last);
			values.add(value);
			last = value;
		}
		return values;
	}

	List<Double> prepareUsagesSeries(List<LocalDate> xAsisTimes,
	                                         Map<LocalDate, Double> timeToUsageAmounts,
	                                         LocalDate lastUsageTime,
	                                         LocalDate lastChunkTime,
	                                         LocalDate today) {
		if(lastUsageTime == null)
			return List.of();
		LocalDate endTime;
		if(lastUsageTime.isBefore(today))
			endTime = today;
		else if(lastChunkTime != null && lastUsageTime.isAfter(lastChunkTime))
			endTime = lastChunkTime;
		else
			endTime = lastUsageTime;

		double last = 0;
		List<Double> values = new ArrayList<>();
		for(LocalDate date : xAsisTimes){
			if(date.isAfter(endTime))
				break;
			double value = timeToUsageAmounts.getOrDefault(date, last);
			values.add(value);
			last = value;
		}
		return values;
	}

	List<Double> prepareThresholdSeries(List<LocalDate> dates, double threshold) {
		return IntStream.range(0, dates.size())
			.boxed()
			.map(x -> threshold)
			.collect(toList());
	}

	List<Double> prepareProjectsUsageSeries(List<LocalDate> xAxisTimes, Collection<Map<LocalDate, Double>> map) {
		List<List<Double>> orderedUsagesGroupedByAllocationId = map.stream()
			.map(usageMap -> prepareYAxis(xAxisTimes, usageMap))
			.collect(toList());

		List<Double> usage = new ArrayList<>();
		for(int i = 0; i < xAxisTimes.size(); i++) {
			double tmp = 0;
			for (List<Double> doubles : orderedUsagesGroupedByAllocationId) {
				tmp += doubles.get(i);
			}
			usage.add(tmp);
		}
		return usage;
	}

	List<UserUsage> prepareUserUsagesSeries(List<LocalDate> dates, Map<String, Map<LocalDate, Double>> groupedTimeToUserUsages, LocalDate today) {
		return groupedTimeToUserUsages.entrySet().stream()
			.map(usageMap -> new UserUsage(
				usageMap.getKey(),
				prepareYAxis(dates, usageMap.getValue(), today))
			)
			.sorted(comparing(userUsage -> userUsage.email))
			.collect(toList());
	}

	private static List<Double> prepareYAxis(List<LocalDate> xAxisTimes, Map<LocalDate, Double> timeToAmounts) {
		double last = 0;
		List<Double> values = new ArrayList<>();
		for(LocalDate date : xAxisTimes){
			double value = timeToAmounts.getOrDefault(date, last);
			values.add(value);
			last = value;
		}
		return values;
	}

	private static List<Double> prepareYAxis(List<LocalDate> xAxisTimes, Map<LocalDate, Double> timeToAmounts, LocalDate today) {
		double last = 0;
		List<Double> values = new ArrayList<>();
		for(LocalDate date : xAxisTimes){
			if(date.isAfter(today))
				break;
			double value = timeToAmounts.getOrDefault(date, last);
			values.add(value);
			last = value;
		}
		return values;
	}
}
