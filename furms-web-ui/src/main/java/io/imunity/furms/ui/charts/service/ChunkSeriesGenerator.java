/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
class ChunkSeriesGenerator {

	List<Double> prepareYValuesForAllocationChunkLine(List<LocalDate> xArguments,
	                                                  Set<ProjectAllocationChunk> allChunks) {
		Map<LocalDate, Double> sumOfChunkValuesByValidToDates = prepareSumOfChunkValuesByValidDates(allChunks);
		double lastValue = 0;
		List<Double> yValues = new ArrayList<>();
		for (LocalDate xArgument : xArguments) {
			double value = sumOfChunkValuesByValidToDates.getOrDefault(xArgument, lastValue);
			yValues.add(value);
			lastValue = value;
		}
		return yValues;
	}

	private Map<LocalDate, Double> prepareSumOfChunkValuesByValidDates(Set<ProjectAllocationChunk> allChunks) {
		List<ProjectAllocationChunk> orderedChunks = allChunks.stream()
			.sorted(comparing(x -> x.validFrom))
			.collect(toList());

		double last = 0;
		Map<LocalDate, Double> sumOfChunkValuesByValidDates = new HashMap<>();
		for (ProjectAllocationChunk chunk : orderedChunks) {
			sumOfChunkValuesByValidDates.put(chunk.validFrom.toLocalDate(), chunk.amount.doubleValue() + last);
			last = chunk.amount.doubleValue() + last;
		}
		return sumOfChunkValuesByValidDates;
	}
}
