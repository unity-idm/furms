/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import io.imunity.furms.ui.charts.service.XChartArgumentsPreparer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class XChartArgumentsPreparerTest {
	private XChartArgumentsPreparer xChartArgumentsPreparer;

	@BeforeEach
	void setUp() {
		xChartArgumentsPreparer = new XChartArgumentsPreparer();
	}

	@Test
	void shouldPrepareTimes(){
		LocalDate startTime = LocalDate.now();
		LocalDate today = startTime.plusDays(2);
		LocalDate lastChunk = startTime.plusDays(35);
		Set<Set<LocalDate>> dates = Set.of(
			Set.of(startTime.plusDays(5), startTime.plusDays(15), startTime.plusDays(25)),
			Set.of(startTime.plusDays(10), startTime.plusDays(20), startTime.plusDays(30))
		);
		List<LocalDate> localDates = xChartArgumentsPreparer.prepareArguments(startTime, today, lastChunk, dates.stream().flatMap(Collection::stream).collect(Collectors.toSet()));

		assertThat(localDates).isEqualTo(List.of(
			startTime.minusDays(1), startTime, today, startTime.plusDays(5), startTime.plusDays(10), startTime.plusDays(15),
			startTime.plusDays(20), startTime.plusDays(25), startTime.plusDays(30), startTime.plusDays(35)
		));
	}
}
