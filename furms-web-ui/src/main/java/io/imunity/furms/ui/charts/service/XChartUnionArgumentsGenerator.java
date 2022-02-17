/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

@Component
class XChartUnionArgumentsGenerator {

	List<LocalDate> getXArgumentsForProjectAlloc(ProjectAllocationResolved projectAllocation,
	                                             Set<ProjectAllocationChunk> allChunks,
	                                             Set<ResourceUsage> allResourceUsageHistory, LocalDate lastChunkTime) {
		return prepareXArguments(
			projectAllocation.resourceCredit.utcStartTime.toLocalDate(),
			lastChunkTime,
			Stream.of(
					allChunks.stream().map(chunk -> chunk.validFrom.toLocalDate()).collect(Collectors.toSet()),
					allResourceUsageHistory.stream().map(usage -> usage.utcProbedAt.toLocalDate()).collect(Collectors.toSet()),
					Set.of(getToday())
				).flatMap(Collection::stream)
				.collect(Collectors.toSet())
		);
	}

	List<LocalDate> getXArgumentsForProjectAllocWithUserUsages(ProjectAllocationResolved projectAllocation,
	                                                           Set<ProjectAllocationChunk> allChunks,
	                                                           Set<UserResourceUsage> allUserResourceUsageHistory,
	                                                           Set<ResourceUsage> allResourceUsageHistory,
	                                                           LocalDate lastChunkTime) {
		return prepareXArguments(
			projectAllocation.resourceCredit.utcStartTime.toLocalDate(),
			lastChunkTime,
			Stream.of(
					allChunks.stream().map(chunk -> chunk.validFrom.toLocalDate()).collect(Collectors.toSet()),
					allResourceUsageHistory.stream().map(usage -> usage.utcProbedAt.toLocalDate()).collect(Collectors.toSet()),
					allUserResourceUsageHistory.stream().map(usage -> usage.utcConsumedUntil.toLocalDate()).collect(Collectors.toSet()),
					Set.of(getToday())
				).flatMap(Collection::stream)
				.collect(Collectors.toSet())
		);
	}

	List<LocalDate> getXArgumentsForCommunityAlloc(CommunityAllocationResolved communityAllocation,
	                                               Set<ResourceUsage> allResourceUsageHistory) {
		Set<LocalDate> collect =
			allResourceUsageHistory.stream()
				.map(usage -> usage.utcProbedAt.toLocalDate())
				.collect(Collectors.toSet());
		collect.add(getToday());
		return prepareXArguments(
			communityAllocation.resourceCredit.utcStartTime.toLocalDate(),
			null,
			collect
		);
	}

	List<LocalDate> prepareXArguments(LocalDate minChartDate,
	                                  LocalDate maxChartDate,
	                                  Set<LocalDate> datesToMark) {
		return getUnionOfAllChartDates(minChartDate, maxChartDate, datesToMark)
			.filter(Objects::nonNull)
			.distinct()
			.filter(date -> isAfterOrEqual(maxChartDate, date))
			.sorted(comparing(identity()))
			.collect(toList());
	}

	private Stream<LocalDate> getUnionOfAllChartDates(LocalDate minChartDate, LocalDate maxChartDate,
	                                                          Set<LocalDate> datesToMark) {
		return Stream.of(
			Stream.of(minChartDate.minusDays(1), minChartDate, maxChartDate),
			datesToMark.stream()
		).flatMap(identity());
	}

	private Boolean isAfterOrEqual(LocalDate endDate, LocalDate date) {
		return ofNullable(endDate)
			.map(localDate -> localDate.isAfter(date) || localDate.isEqual(date))
			.orElse(true);
	}

	private LocalDate getToday() {
		return convertToUTCTime(ZonedDateTime.now()).toLocalDate();
	}
}
