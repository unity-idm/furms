/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Component
class AlarmThresholdSeriesGenerator {
	double prepareThresholdValue(ProjectAllocationResolved projectAllocation, Optional<AlarmWithUserEmails> alarm) {
		double amount = projectAllocation.amount.doubleValue();
		int thresholdPercentage = alarm.map(x -> x.threshold).orElse(0);
		return thresholdPercentage > 0 ? amount * thresholdPercentage / 100 : 0;
	}

	List<Double> prepareYValuesForThresholdLine(List<LocalDate> xArguments, double thresholdValue) {
		int size = xArguments.size() > 0 ? xArguments.size() + 1 : 0;
		return IntStream.range(0, size)
			.boxed()
			.map(x -> thresholdValue)
			.collect(toList());
	}
}
