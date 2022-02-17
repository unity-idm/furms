/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class XChartUnionArgumentsGeneratorTest {
	private XChartUnionArgumentsGenerator xChartUnionArgumentsGenerator;

	@BeforeEach
	void setUp() {
		xChartUnionArgumentsGenerator = new XChartUnionArgumentsGenerator();
	}

	@Test
	void shouldLimitDatesByMinAndMax(){
		LocalDate startTime = LocalDate.now();
		LocalDate lastChunk = startTime.plusDays(35);
		Set<LocalDate> dates = Stream.of(
			Set.of(startTime.plusDays(5), startTime.plusDays(15), startTime.plusDays(25)),
			Set.of(startTime.plusDays(10), startTime.plusDays(20), startTime.plusDays(30))
		)
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());
		List<LocalDate> localDates = xChartUnionArgumentsGenerator.prepareXArguments(startTime, lastChunk, dates);

		assertThat(localDates).isEqualTo(List.of(
			startTime.minusDays(1), startTime, startTime.plusDays(5), startTime.plusDays(10), startTime.plusDays(15),
			startTime.plusDays(20), startTime.plusDays(25), startTime.plusDays(30), startTime.plusDays(35)
		));
	}

	@Test
	void shouldDistinctDates() {
		LocalDate startTime = LocalDate.now();
		LocalDate lastChunk = startTime.plusDays(35);
		Set<LocalDate> dates = Stream.of(
			Set.of(startTime, startTime.plusDays(5), startTime.plusDays(15), startTime.plusDays(25)),
			Set.of(startTime.plusDays(10), startTime.plusDays(20), startTime.plusDays(30), lastChunk)
		)
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());
		List<LocalDate> localDates = xChartUnionArgumentsGenerator.prepareXArguments(startTime, lastChunk, dates);

		assertThat(localDates).isEqualTo(List.of(
			startTime.minusDays(1), startTime, startTime.plusDays(5), startTime.plusDays(10), startTime.plusDays(15),
			startTime.plusDays(20), startTime.plusDays(25), startTime.plusDays(30), startTime.plusDays(35)
		));
	}
}
