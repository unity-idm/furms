/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AllocationTimespanTest {

	@Test
	void shouldReturnsTrueWhenTimesAreTheSame(){
		LocalDateTime now = LocalDateTime.now();
		AllocationTimespan allocationTimespan = new AllocationTimespan(now.minusDays(2), now.plusDays(2));

		boolean overlaps = allocationTimespan.overlaps(new AllocationTimespan(now.minusDays(2), now.plusDays(2)));
		assertTrue(overlaps);
	}

	@Test
	void shouldReturnsTrueWhenStartTimeIsBefore(){
		LocalDateTime now = LocalDateTime.now();
		AllocationTimespan allocationTimespan = new AllocationTimespan(now.minusDays(2), now.plusDays(2));

		boolean overlaps = allocationTimespan.overlaps(new AllocationTimespan(now.minusDays(4), now.plusDays(2)));
		assertTrue(overlaps);
	}

	@Test
	void shouldReturnsTrueWhenEndTimeIsAfter(){
		LocalDateTime now = LocalDateTime.now();
		AllocationTimespan allocationTimespan = new AllocationTimespan(now.minusDays(2), now.plusDays(2));

		boolean overlaps = allocationTimespan.overlaps(new AllocationTimespan(now.minusDays(2), now.plusDays(4)));
		assertTrue(overlaps);
	}

	@Test
	void shouldReturnsFalseWhenTimesAreBefore(){
		LocalDateTime now = LocalDateTime.now();
		AllocationTimespan allocationTimespan = new AllocationTimespan(now.minusDays(2), now.plusDays(2));

		boolean overlaps = allocationTimespan.overlaps(new AllocationTimespan(now.minusDays(5), now.minusDays(3)));
		assertFalse(overlaps);
	}

	@Test
	void shouldReturnsFalseWhenTimesAreAfter(){
		LocalDateTime now = LocalDateTime.now();
		AllocationTimespan allocationTimespan = new AllocationTimespan(now.minusDays(2), now.plusDays(2));

		boolean overlaps = allocationTimespan.overlaps(new AllocationTimespan(now.plusDays(3), now.plusDays(5)));
		assertFalse(overlaps);
	}
}