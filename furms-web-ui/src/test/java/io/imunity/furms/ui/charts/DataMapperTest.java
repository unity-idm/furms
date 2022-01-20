/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataMapperTest {
	@Mock
	UserService userService;

	@InjectMocks
	DataMapper dataMapper;

	@Test
	void shouldPrepareTimedChunkAmounts() {
		LocalDateTime date = LocalDate.now().atStartOfDay();
		Set<ProjectAllocationChunk> chunks = Set.of(
			createChunk(date, BigDecimal.valueOf(2)),
			createChunk(date.plusDays(14), BigDecimal.valueOf(2)),
			createChunk(date.plusDays(28), BigDecimal.valueOf(3)),
			createChunk(date.plusDays(56), BigDecimal.valueOf(1))
		);

		Map<LocalDate, Double> timedChunkAmounts = dataMapper.prepareTimedChunkAmounts(chunks);

		assertThat(timedChunkAmounts).isEqualTo(
			Map.of(
				date.toLocalDate(), 2D,
				date.plusDays(14).toLocalDate(), 4D,
				date.plusDays(28).toLocalDate(), 7D,
				date.plusDays(56).toLocalDate(), 8D
		));
	}

	@Test
	void shouldPrepareTimedUsageAmounts() {
		LocalDateTime date = LocalDate.now().atStartOfDay();
		Set<ResourceUsage> allocations = Set.of(
			createUsage("alloc1", date, BigDecimal.valueOf(2)),
			createUsage("alloc1", date.plusDays(14), BigDecimal.valueOf(2)),
			createUsage("alloc1", date.plusDays(28), BigDecimal.valueOf(3)),
			createUsage("alloc1", date.plusDays(56), BigDecimal.valueOf(2))
		);

		Map<LocalDate, Double> timedChunkAmounts = dataMapper.prepareTimedUsageAmounts(allocations);

		assertThat(timedChunkAmounts).isEqualTo(
			Map.of(
				date.toLocalDate(), 2D,
				date.plusDays(14).toLocalDate(), 2D,
				date.plusDays(28).toLocalDate(), 3D,
				date.plusDays(56).toLocalDate(), 2D
			));
	}

	@Test
	void shouldPrepareTimedProjectsUsages() {
		LocalDateTime date = LocalDate.now().atStartOfDay();
		Set<ResourceUsage> allocations = Set.of(
			createUsage("alloc1", date, BigDecimal.valueOf(2)),
			createUsage("alloc2", date, BigDecimal.valueOf(3)),
			createUsage("alloc1", date.plusDays(14), BigDecimal.valueOf(2)),
			createUsage("alloc2", date.plusDays(14), BigDecimal.valueOf(2)),
			createUsage("alloc1", date.plusDays(28), BigDecimal.valueOf(3)),
			createUsage("alloc2", date.plusDays(30), BigDecimal.valueOf(3)),
			createUsage("alloc1", date.plusDays(56), BigDecimal.valueOf(2)),
			createUsage("alloc2", date.plusDays(54), BigDecimal.valueOf(2))
		);

		Collection<Map<LocalDate, Double>> timedChunkAmounts = dataMapper.prepareTimedProjectsUsages(allocations);
		assertThat(timedChunkAmounts.size()).isEqualTo(2);

		assertThat(new HashSet<>(timedChunkAmounts)).isEqualTo(
			Set.of(
				Map.of(
					date.toLocalDate(), 3D,
					date.plusDays(14).toLocalDate(), 2D,
					date.plusDays(30).toLocalDate(), 3D,
					date.plusDays(54).toLocalDate(), 2D
				),
				Map.of(
					date.toLocalDate(), 2D,
					date.plusDays(14).toLocalDate(), 2D,
					date.plusDays(28).toLocalDate(), 3D,
					date.plusDays(56).toLocalDate(), 2D
			)
		));
	}

	@Test
	void shouldPrepareTimedUserUsagesGroupedByEmails() {
		LocalDateTime date = LocalDate.now().atStartOfDay();
		Set<UserResourceUsage> allocations = Set.of(
			createUserUsage("alloc1", date, BigDecimal.valueOf(2), new FenixUserId("id")),
			createUserUsage("alloc2", date, BigDecimal.valueOf(3), new FenixUserId("id2")),
			createUserUsage("alloc1", date.plusDays(14), BigDecimal.valueOf(2), new FenixUserId("id")),
			createUserUsage("alloc2", date.plusDays(14), BigDecimal.valueOf(2), new FenixUserId("id2")),
			createUserUsage("alloc1", date.plusDays(28), BigDecimal.valueOf(3), new FenixUserId("id")),
			createUserUsage("alloc2", date.plusDays(30), BigDecimal.valueOf(3), new FenixUserId("id2")),
			createUserUsage("alloc1", date.plusDays(56), BigDecimal.valueOf(2), new FenixUserId("id")),
			createUserUsage("alloc2", date.plusDays(54), BigDecimal.valueOf(2), new FenixUserId("id2"))
		);

		when(userService.getAllUsers()).thenReturn(List.of(
			FURMSUser.builder()
				.fenixUserId(new FenixUserId("id"))
				.email("email")
				.build(),
			FURMSUser.builder()
				.fenixUserId(new FenixUserId("id2"))
				.email("email2")
				.build()
		));

		Map<String, Map<LocalDate, Double>> timedChunkAmounts = dataMapper.prepareTimedUserUsagesGroupedByEmails(allocations);

		assertThat(timedChunkAmounts).isEqualTo(
			Map.of(
				"email2", Map.of(
					date.toLocalDate(), 3D,
					date.plusDays(14).toLocalDate(), 2D,
					date.plusDays(30).toLocalDate(), 3D,
					date.plusDays(54).toLocalDate(), 2D
				),
				"email", Map.of(
					date.toLocalDate(), 2D,
					date.plusDays(14).toLocalDate(), 2D,
					date.plusDays(28).toLocalDate(), 3D,
					date.plusDays(56).toLocalDate(), 2D
				))
		);
	}

	@Test
	void shouldPrepareThreshold() {
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.amount(BigDecimal.TEN)
			.build();

		AlarmWithUserEmails alarm = AlarmWithUserEmails.builder()
			.threshold(7)
			.build();

		double threshold = dataMapper.prepareThreshold(projectAllocationResolved, Optional.of(alarm));

		assertThat(threshold).isEqualTo(0.7);
	}

	@Test
	void shouldPrepareThresholdWhenAlarmIsEmpty() {
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.amount(BigDecimal.TEN)
			.build();

		double threshold = dataMapper.prepareThreshold(projectAllocationResolved, Optional.empty());

		assertThat(threshold).isEqualTo(0D);
	}

	private ResourceUsage createUsage(String projectAllocId, LocalDateTime startDate, BigDecimal amount) {
		return ResourceUsage.builder()
			.projectAllocationId(projectAllocId)
			.cumulativeConsumption(amount)
			.probedAt(startDate)
			.build();
	}

	private UserResourceUsage createUserUsage(String projectAllocId, LocalDateTime startDate, BigDecimal amount, FenixUserId userId) {
		return UserResourceUsage.builder()
			.projectAllocationId(projectAllocId)
			.cumulativeConsumption(amount)
			.consumedUntil(startDate)
			.fenixUserId(userId)
			.build();
	}

	private ProjectAllocationChunk createChunk(LocalDateTime startDate, BigDecimal amount) {
		return ProjectAllocationChunk.builder()
			.amount(amount)
			.validFrom(startDate)
			.validTo(startDate.plusWeeks(2))
			.build();
	}
}
