/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

@Component
class XSeriesPreparer {
	List<LocalDate> prepareSumOfAllDataTimesForXAxis(LocalDate startTime,
	                                                         LocalDate today,
	                                                         LocalDate lastChunkTime,
	                                                         Set<Set<LocalDate>> allTimes) {
		return Stream.of(
				Stream.of(
					startTime.minusDays(1), startTime, today, lastChunkTime
				),
				allTimes.stream()
					.flatMap(Collection::stream)
			)
			.flatMap(identity())
			.filter(Objects::nonNull)
			.distinct()
			.filter(date -> Optional.ofNullable(lastChunkTime).map(chunkTime -> chunkTime.isAfter(date) || chunkTime.isEqual(date)).orElse(true))
			.sorted(comparing(identity()))
			.collect(toList());
	}
}
