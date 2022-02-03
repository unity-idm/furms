/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class YChartValuesPreparerTest {
	private YChartValuesPreparer yChartValuesPreparer;

	@BeforeEach
	void setUp() {
		yChartValuesPreparer = new YChartValuesPreparer();
	}

	@Test
	void shouldPrepareChunkSeries() {
		LocalDate startDate = LocalDate.now();
		List<LocalDate> xTimeAxis = List.of(
			startDate.minusDays(1),
			startDate,
			startDate.plusDays(1),
			startDate.plusDays(2),
			startDate.plusDays(3),
			startDate.plusDays(4),
			startDate.plusDays(5),
			startDate.plusDays(6)
		);
		Map<LocalDate, Double> timeToChunksValue = Map.ofEntries(
			Map.entry(startDate.plusDays(1), 2D),
			Map.entry(startDate.plusDays(2), 5D),
			Map.entry(startDate.plusDays(4), 10D),
			Map.entry(startDate.plusDays(6), 12D)
		);

		List<Double> values = yChartValuesPreparer.prepareYValuesForAllocationChunkLine(xTimeAxis, timeToChunksValue);

		assertThat(values).isEqualTo(List.of(
			0D, 0D, 2D, 5D, 5D, 10D, 10D, 12D
		));
	}

	@Test
	void shouldPrepareUsageSeriesWhenLastDateIsNow() {
		LocalDate startDate = LocalDate.now();
		List<LocalDate> xTimeAxis = List.of(
			startDate.minusDays(1),
			startDate,
			startDate.plusDays(1),
			startDate.plusDays(2),
			startDate.plusDays(3),
			startDate.plusDays(4),
			startDate.plusDays(5),
			startDate.plusDays(6)
		);
		Map<LocalDate, Double> timeToChunksValue = Map.ofEntries(
			Map.entry(startDate.plusDays(1), 2D),
			Map.entry(startDate.plusDays(2), 5D),
			Map.entry(startDate.plusDays(4), 10D),
			Map.entry(startDate.plusDays(6), 12D)
		);

		List<Double> values = yChartValuesPreparer.prepareYValuesForUserUsageLine(xTimeAxis, timeToChunksValue, startDate.plusDays(4), startDate.plusDays(6), startDate.plusDays(5));

		assertThat(values).isEqualTo(List.of(
			0D, 0D, 2D, 5D, 5D, 10D, 10D
		));
	}

	@Test
	void shouldPrepareUsageSeriesWhenIsZero() {
		LocalDate startDate = LocalDate.now();
		List<LocalDate> xTimeAxis = List.of(
			startDate.minusDays(1),
			startDate,
			startDate.plusDays(1),
			startDate.plusDays(2),
			startDate.plusDays(3),
			startDate.plusDays(4),
			startDate.plusDays(5),
			startDate.plusDays(6)
		);
		Map<LocalDate, Double> timeToChunksValue = Map.ofEntries(
		);

		List<Double> values = yChartValuesPreparer.prepareYValuesForUserUsageLine(xTimeAxis, timeToChunksValue, null, startDate.plusDays(6), startDate.plusDays(3));

		assertThat(values).isEqualTo(List.of());
	}

	@Test
	void shouldPrepareUsageSeriesWhenIsOneUsage() {
		LocalDate startDate = LocalDate.now();
		List<LocalDate> xTimeAxis = List.of(
			startDate.minusDays(1),
			startDate,
			startDate.plusDays(1),
			startDate.plusDays(2),
			startDate.plusDays(3),
			startDate.plusDays(4),
			startDate.plusDays(5),
			startDate.plusDays(6)
		);
		Map<LocalDate, Double> timeToChunksValue = Map.ofEntries(
			Map.entry(startDate.plusDays(1), 2D)
		);

		List<Double> values = yChartValuesPreparer.prepareYValuesForUserUsageLine(xTimeAxis, timeToChunksValue, startDate.plusDays(1), startDate.plusDays(6), startDate.plusDays(3));

		assertThat(values).isEqualTo(List.of(
			0D, 0D, 2D, 2D, 2D
		));
	}

	@Test
	void shouldPrepareUsageSeriesWhenChunkTimeIsNull() {
		LocalDate startDate = LocalDate.now();
		List<LocalDate> xTimeAxis = List.of(
			startDate.minusDays(1),
			startDate,
			startDate.plusDays(1),
			startDate.plusDays(2),
			startDate.plusDays(3),
			startDate.plusDays(4),
			startDate.plusDays(5),
			startDate.plusDays(6)
		);
		Map<LocalDate, Double> timeToChunksValue = Map.ofEntries(
			Map.entry(startDate.plusDays(1), 2D),
			Map.entry(startDate.plusDays(2), 5D),
			Map.entry(startDate.plusDays(4), 10D),
			Map.entry(startDate.plusDays(6), 12D)
		);

		List<Double> values = yChartValuesPreparer.prepareYValuesForUserUsageLine(xTimeAxis, timeToChunksValue, startDate.plusDays(4), null, startDate.plusDays(6));

		assertThat(values).isEqualTo(List.of(
			0D, 0D, 2D, 5D, 5D, 10D, 10D, 12D
		));
	}

	@Test
	void shouldPrepareUsageSeriesWhenLastUsageTimeIsAfterLastChunkTime() {
		LocalDate startDate = LocalDate.now();
		List<LocalDate> xTimeAxis = List.of(
			startDate.minusDays(1),
			startDate,
			startDate.plusDays(1),
			startDate.plusDays(2),
			startDate.plusDays(3),
			startDate.plusDays(4),
			startDate.plusDays(5),
			startDate.plusDays(6)
		);
		Map<LocalDate, Double> timeToChunksValue = Map.ofEntries(
			Map.entry(startDate.plusDays(1), 2D),
			Map.entry(startDate.plusDays(2), 5D),
			Map.entry(startDate.plusDays(4), 10D),
			Map.entry(startDate.plusDays(6), 12D)
		);

		List<Double> values = yChartValuesPreparer.prepareYValuesForUserUsageLine(xTimeAxis, timeToChunksValue, startDate.plusDays(6), startDate.plusDays(4), startDate.plusDays(5));

		assertThat(values).isEqualTo(List.of(
			0D, 0D, 2D, 5D, 5D, 10D
		));
	}

	@Test
	void shouldPrepareThresholdSeries() {
		LocalDate startDate = LocalDate.now();
		List<LocalDate> xTimeAxis = List.of(
			startDate.minusDays(1),
			startDate,
			startDate.plusDays(1),
			startDate.plusDays(2),
			startDate.plusDays(3),
			startDate.plusDays(4),
			startDate.plusDays(5),
			startDate.plusDays(6)
		);

		List<Double> values = yChartValuesPreparer.prepareYValuesForThresholdLine(xTimeAxis, 7);

		assertThat(values).isEqualTo(List.of(
			7D, 7D, 7D, 7D, 7D, 7D, 7D, 7D, 7D
		));
	}

	@Test
	void shouldPrepareUsageSumSeries() {
		LocalDate startDate = LocalDate.now();
		List<LocalDate> xTimeAxis = List.of(
			startDate.minusDays(1),
			startDate,
			startDate.plusDays(1),
			startDate.plusDays(2),
			startDate.plusDays(3),
			startDate.plusDays(4),
			startDate.plusDays(5),
			startDate.plusDays(6)
		);
		List<Map<LocalDate, Double>> timeToGroupedValues =
			List.of(
				Map.ofEntries(
					Map.entry(startDate.plusDays(1), 2D),
					Map.entry(startDate.plusDays(2), 5D),
					Map.entry(startDate.plusDays(4), 10D),
					Map.entry(startDate.plusDays(6), 12D)
				),
				Map.ofEntries(
					Map.entry(startDate.plusDays(1), 2D),
					Map.entry(startDate.plusDays(2), 5D),
					Map.entry(startDate.plusDays(4), 10D),
					Map.entry(startDate.plusDays(6), 12D)
				)
		);

		List<Double> values = yChartValuesPreparer.prepareYValuesForCommunityAllocationUsageLine(xTimeAxis, timeToGroupedValues);

		assertThat(values).isEqualTo(List.of(
			0D, 0D, 4D, 10D, 10D, 20D, 20D, 24D
		));
	}

	@Test
	void shouldPrepareUserUsageSeries() {
		LocalDate startDate = LocalDate.now();
		List<LocalDate> xTimeAxis = List.of(
			startDate.minusDays(1),
			startDate,
			startDate.plusDays(1),
			startDate.plusDays(2),
			startDate.plusDays(3),
			startDate.plusDays(4),
			startDate.plusDays(5),
			startDate.plusDays(6)
		);
		Map<String, Map<LocalDate, Double>> timeToGroupedValues =
			Map.of(
				"email1", Map.ofEntries(
					Map.entry(startDate.plusDays(1), 2D),
					Map.entry(startDate.plusDays(2), 5D),
					Map.entry(startDate.plusDays(4), 10D),
					Map.entry(startDate.plusDays(6), 12D)
				),
				"email2", Map.ofEntries(
					Map.entry(startDate.plusDays(1), 2D),
					Map.entry(startDate.plusDays(2), 5D),
					Map.entry(startDate.plusDays(4), 10D),
					Map.entry(startDate.plusDays(6), 12D)
				)
			);

		List<UserUsage> values = yChartValuesPreparer.prepareYValesForUsersUsagesLines(xTimeAxis, timeToGroupedValues, startDate.plusDays(5));

		assertThat(values.get(0).userEmail).isEqualTo("email1");
		assertThat(values.get(0).yUserUsageValues).isEqualTo(List.of(
			0D, 0D, 2D, 5D, 5D, 10D, 10D
		));
		assertThat(values.get(1).userEmail).isEqualTo("email2");
		assertThat(values.get(1).yUserUsageValues).isEqualTo(List.of(
			0D, 0D, 2D, 5D, 5D, 10D, 10D
		));
	}
}
