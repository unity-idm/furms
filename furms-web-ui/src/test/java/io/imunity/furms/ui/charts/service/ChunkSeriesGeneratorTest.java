/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ChunkSeriesGeneratorTest {
	private ChunkSeriesGenerator chunkSeriesGenerator;

	@BeforeEach
	void setUp() {
		chunkSeriesGenerator = new ChunkSeriesGenerator();
	}

	@Test
	void shouldPrepareChunkSeriesForFourChunks() {
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
		Set<ProjectAllocationChunk> chunks = Set.of(
			createChunk(date.plusDays(1), BigDecimal.valueOf(2)),
			createChunk(date.plusDays(2), BigDecimal.valueOf(3)),
			createChunk(date.plusDays(4), BigDecimal.valueOf(5)),
			createChunk(date.plusDays(6), BigDecimal.valueOf(2))
		);

		List<Double> values = chunkSeriesGenerator.prepareYValuesForAllocationChunkLine(xTimeAxis, chunks);

		assertThat(values).isEqualTo(List.of(
			0D, 0D, 2D, 5D, 5D, 10D, 10D, 12D
		));
	}

	private ProjectAllocationChunk createChunk(LocalDateTime startDate, BigDecimal amount) {
		return ProjectAllocationChunk.builder()
			.amount(amount)
			.validFrom(startDate)
			.validTo(startDate.plusWeeks(2))
			.build();
	}
}
