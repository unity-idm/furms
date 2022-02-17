/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import io.imunity.furms.domain.resource_usage.ResourceUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ProjectAllocUsageSeriesGeneratorTest {
	private ProjectAllocUsageSeriesGenerator projectAllocUsageSeriesGenerator;

	@BeforeEach
	void setUp() {
		projectAllocUsageSeriesGenerator = new ProjectAllocUsageSeriesGenerator();
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
		LocalDateTime date = startDate.atStartOfDay();
		Set<ResourceUsage> allocations = Set.of(
			createUsage("alloc1", date.plusDays(1), BigDecimal.valueOf(2)),
			createUsage("alloc1", date.plusDays(2), BigDecimal.valueOf(5)),
			createUsage("alloc1", date.plusDays(4), BigDecimal.valueOf(10)),
			createUsage("alloc1", date.plusDays(6), BigDecimal.valueOf(12))
		);

		List<Double> values = projectAllocUsageSeriesGenerator.prepareYValuesForUserUsageLine(xTimeAxis, allocations, startDate.plusDays(4), startDate.plusDays(6), startDate.plusDays(5));

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
		Set<ResourceUsage> allocations = Set.of();

		List<Double> values = projectAllocUsageSeriesGenerator.prepareYValuesForUserUsageLine(xTimeAxis, allocations, null, startDate.plusDays(6), startDate.plusDays(3));

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
		LocalDateTime date = startDate.atStartOfDay();
		Set<ResourceUsage> allocations = Set.of(
			createUsage("alloc1", date.plusDays(1), BigDecimal.valueOf(2))
		);

		List<Double> values = projectAllocUsageSeriesGenerator.prepareYValuesForUserUsageLine(xTimeAxis, allocations, startDate.plusDays(1), startDate.plusDays(6), startDate.plusDays(3));

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
		LocalDateTime date = startDate.atStartOfDay();
		Set<ResourceUsage> allocations = Set.of(
			createUsage("alloc1", date.plusDays(1), BigDecimal.valueOf(2)),
			createUsage("alloc1", date.plusDays(2), BigDecimal.valueOf(5)),
			createUsage("alloc1", date.plusDays(4), BigDecimal.valueOf(10)),
			createUsage("alloc1", date.plusDays(6), BigDecimal.valueOf(12))
		);

		List<Double> values = projectAllocUsageSeriesGenerator.prepareYValuesForUserUsageLine(xTimeAxis, allocations, startDate.plusDays(4), null, startDate.plusDays(6));

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
		LocalDateTime date = startDate.atStartOfDay();
		Set<ResourceUsage> allocations = Set.of(
			createUsage("alloc1", date.plusDays(1), BigDecimal.valueOf(2)),
			createUsage("alloc1", date.plusDays(2), BigDecimal.valueOf(5)),
			createUsage("alloc1", date.plusDays(4), BigDecimal.valueOf(10)),
			createUsage("alloc1", date.plusDays(6), BigDecimal.valueOf(12))
		);

		List<Double> values = projectAllocUsageSeriesGenerator.prepareYValuesForUserUsageLine(xTimeAxis, allocations, startDate.plusDays(6), startDate.plusDays(4), startDate.plusDays(5));

		assertThat(values).isEqualTo(List.of(
			0D, 0D, 2D, 5D, 5D, 10D
		));
	}

	private ResourceUsage createUsage(String projectAllocId, LocalDateTime startDate, BigDecimal amount) {
		return ResourceUsage.builder()
			.projectAllocationId(projectAllocId)
			.cumulativeConsumption(amount)
			.probedAt(startDate)
			.build();
	}
}
