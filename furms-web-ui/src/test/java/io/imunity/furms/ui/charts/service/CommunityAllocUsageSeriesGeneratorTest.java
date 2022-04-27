/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CommunityAllocUsageSeriesGeneratorTest {
	private CommunityAllocUsageSeriesGenerator communityAllocUsageSeriesGenerator;

	@BeforeEach
	void setUp() {
		communityAllocUsageSeriesGenerator = new CommunityAllocUsageSeriesGenerator();
	}

	@Test
	void shouldPrepareUsageSumSeriesForTwoProjectAllocations() {
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
		ProjectAllocationId allocationId = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocationId allocationId2 = new ProjectAllocationId(UUID.randomUUID());

		Set<ResourceUsage> allocations = Set.of(
			createUsage(allocationId.id.toString(), date.plusDays(1), BigDecimal.valueOf(2)),
			createUsage(allocationId.id.toString(), date.plusDays(2), BigDecimal.valueOf(5)),
			createUsage(allocationId.id.toString(), date.plusDays(4), BigDecimal.valueOf(10)),
			createUsage(allocationId.id.toString(), date.plusDays(6), BigDecimal.valueOf(12)),
			createUsage(allocationId2.id.toString(), date.plusDays(1), BigDecimal.valueOf(2)),
			createUsage(allocationId2.id.toString(), date.plusDays(2), BigDecimal.valueOf(5)),
			createUsage(allocationId2.id.toString(), date.plusDays(4), BigDecimal.valueOf(10)),
			createUsage(allocationId2.id.toString(), date.plusDays(6), BigDecimal.valueOf(12))
		);

		List<Double> values = communityAllocUsageSeriesGenerator.prepareYValuesForCommunityAllocationUsageLine(xTimeAxis, allocations);

		assertThat(values).isEqualTo(List.of(
			0D, 0D, 4D, 10D, 10D, 20D, 20D, 24D
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
