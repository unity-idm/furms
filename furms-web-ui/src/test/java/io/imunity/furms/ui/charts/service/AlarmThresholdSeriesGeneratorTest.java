/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AlarmThresholdSeriesGeneratorTest {
	private AlarmThresholdSeriesGenerator alarmThresholdSeriesGenerator;

	@BeforeEach
	void setUp() {
		alarmThresholdSeriesGenerator = new AlarmThresholdSeriesGenerator();
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

		List<Double> values = alarmThresholdSeriesGenerator.prepareYValuesForThresholdLine(xTimeAxis, 7);

		assertThat(values).isEqualTo(List.of(
			7D, 7D, 7D, 7D, 7D, 7D, 7D, 7D, 7D
		));
	}

	@Test
	void shouldPrepareThresholdWhenAlarmIsSet() {
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.amount(BigDecimal.TEN)
			.build();

		AlarmWithUserEmails alarm = AlarmWithUserEmails.builder()
			.threshold(7)
			.build();

		double threshold = alarmThresholdSeriesGenerator.prepareThresholdValue(projectAllocationResolved, Optional.of(alarm));

		assertThat(threshold).isEqualTo(0.7);
	}

	@Test
	void shouldPrepareThresholdWhenAlarmIsEmpty() {
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.amount(BigDecimal.TEN)
			.build();

		double threshold = alarmThresholdSeriesGenerator.prepareThresholdValue(projectAllocationResolved, Optional.empty());

		assertThat(threshold).isEqualTo(0D);
	}
}
