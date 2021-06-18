/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import java.time.LocalDateTime;
import java.util.Objects;

class AllocationTimestamp {
	private final LocalDateTime startTime;
	private final LocalDateTime endTime;

	AllocationTimestamp(LocalDateTime startTime, LocalDateTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	boolean overlaps(AllocationTimestamp allocationTimestamp){
		return isBetween(startTime, allocationTimestamp.startTime, allocationTimestamp.endTime) ||
			isBetween(endTime, allocationTimestamp.startTime, allocationTimestamp.endTime)      ||
			(startTime.isBefore(allocationTimestamp.startTime) && endTime.isAfter(allocationTimestamp.endTime));
	}

	private boolean isBetween(LocalDateTime toCheck, LocalDateTime leftBoundary, LocalDateTime rightBoundary){
		return (toCheck.isAfter(leftBoundary) || toCheck.isEqual(leftBoundary)) && (toCheck.isBefore(rightBoundary) || leftBoundary.isEqual(rightBoundary));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AllocationTimestamp that = (AllocationTimestamp) o;
		return Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(startTime, endTime);
	}

	@Override
	public String toString() {
		return "AllocationTimestamp{" +
			"startTime=" + startTime +
			", endTime=" + endTime +
			'}';
	}
}
