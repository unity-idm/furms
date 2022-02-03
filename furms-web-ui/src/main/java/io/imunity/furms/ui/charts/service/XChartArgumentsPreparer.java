/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

@Component
class XChartArgumentsPreparer {
	List<LocalDate> prepareArguments(LocalDate firstChartDate,
	                                 LocalDate today,
	                                 LocalDate lastChartDate,
	                                 Set<LocalDate> datesToMarking) {
		return getSumOfAllChartDates(firstChartDate, today, lastChartDate, datesToMarking)
			.flatMap(identity())
			.filter(Objects::nonNull)
			.distinct()
			.filter(date -> isValidDate(lastChartDate, date))
			.sorted(comparing(identity()))
			.collect(toList());
	}

	private Stream<Stream<LocalDate>> getSumOfAllChartDates(LocalDate firstChartDate, LocalDate today,
	                                                        LocalDate lastChartDate, Set<LocalDate> datesToMarking) {
		return Stream.of(
			Stream.of(firstChartDate.minusDays(1), firstChartDate, today, lastChartDate),
			datesToMarking.stream()
		);
	}

	private Boolean isValidDate(LocalDate lastChartDate, LocalDate date) {
		return ofNullable(lastChartDate).map(localDate -> localDate.isAfter(date) || localDate.isEqual(date))
			.orElse(true);
	}
}
