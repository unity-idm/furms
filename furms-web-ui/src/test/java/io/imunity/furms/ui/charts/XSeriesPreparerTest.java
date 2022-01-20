/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class XSeriesPreparerTest {
	private XSeriesPreparer xSeriesPreparer;

	@BeforeEach
	void setUp() {
		xSeriesPreparer = new XSeriesPreparer();
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
		List<LocalDate> localDates = xSeriesPreparer.prepareSumOfAllDataTimesForXAxis(startTime, today, lastChunk, dates);

		assertThat(localDates).isEqualTo(List.of(
			startTime.minusDays(1), startTime, today, startTime.plusDays(5), startTime.plusDays(10), startTime.plusDays(15),
			startTime.plusDays(20), startTime.plusDays(25), startTime.plusDays(30), startTime.plusDays(35)
		));
	}
}
