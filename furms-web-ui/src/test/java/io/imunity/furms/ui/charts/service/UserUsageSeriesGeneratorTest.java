/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts.service;

import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUsageSeriesGeneratorTest {
	private UserUsageSeriesGenerator usageSeriesGenerator;

	@Mock
	UserService userService;

	@BeforeEach
	void setUp() {
		usageSeriesGenerator = new UserUsageSeriesGenerator(userService);
	}

	@Test
	void shouldPrepareUserUsageSeriesForTwoUsersAndTwoAllocations() {
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

		when(userService.getAllUsers()).thenReturn(List.of(
			FURMSUser.builder()
				.fenixUserId(new FenixUserId("id"))
				.email("email1")
				.build(),
			FURMSUser.builder()
				.fenixUserId(new FenixUserId("id2"))
				.email("email2")
				.build()
		));

		LocalDateTime date = startDate.atStartOfDay();
		ProjectAllocationId allocationId = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocationId allocationId2 = new ProjectAllocationId(UUID.randomUUID());

		Set<io.imunity.furms.domain.resource_usage.UserResourceUsage> allocations = Set.of(
			createUserUsage(allocationId.id.toString(), date.plusDays(1), BigDecimal.valueOf(2), new FenixUserId("id")),
			createUserUsage(allocationId.id.toString(), date.plusDays(2), BigDecimal.valueOf(5), new FenixUserId("id")),
			createUserUsage(allocationId.id.toString(), date.plusDays(4), BigDecimal.valueOf(10), new FenixUserId("id")),
			createUserUsage(allocationId.id.toString(), date.plusDays(6), BigDecimal.valueOf(12), new FenixUserId("id")),
			createUserUsage(allocationId2.id.toString(), date.plusDays(1), BigDecimal.valueOf(2), new FenixUserId(
				"id2")),
			createUserUsage(allocationId2.id.toString(), date.plusDays(2), BigDecimal.valueOf(5), new FenixUserId("id2")),
			createUserUsage(allocationId2.id.toString(), date.plusDays(4), BigDecimal.valueOf(10), new FenixUserId("id2")),
			createUserUsage(allocationId2.id.toString(), date.plusDays(6), BigDecimal.valueOf(12), new FenixUserId("id2"))
		);

		List<UserResourceUsage> values = usageSeriesGenerator.prepareYValesForUsersUsagesLines(xTimeAxis, allocations, startDate.plusDays(5));

		assertThat(values.get(0).userEmail).isEqualTo("email1");
		assertThat(values.get(0).yUserCumulativeUsageValues).isEqualTo(List.of(
			0D, 0D, 2D, 5D, 5D, 10D, 10D
		));
		assertThat(values.get(1).userEmail).isEqualTo("email2");
		assertThat(values.get(1).yUserCumulativeUsageValues).isEqualTo(List.of(
			0D, 0D, 2D, 5D, 5D, 10D, 10D
		));
	}

	private io.imunity.furms.domain.resource_usage.UserResourceUsage createUserUsage(String projectAllocId, LocalDateTime startDate, BigDecimal amount, FenixUserId userId) {
		return io.imunity.furms.domain.resource_usage.UserResourceUsage.builder()
			.projectAllocationId(projectAllocId)
			.cumulativeConsumption(amount)
			.consumedUntil(startDate)
			.fenixUserId(userId)
			.build();
	}
}
