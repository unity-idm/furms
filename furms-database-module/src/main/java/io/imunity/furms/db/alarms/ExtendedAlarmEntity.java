/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.alarms;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

class ExtendedAlarmEntity extends UUIDIdentifiable {
	public final UUID projectId;
	public final UUID projectAllocationId;
	public final String name;
	public final int threshold;
	public final boolean allUsers;
	public final String projectAllocationName;
	public final BigDecimal allocationAmount;
	public final BigDecimal cumulativeConsumption;

	ExtendedAlarmEntity(UUID id, UUID projectId, UUID projectAllocationId, String name, int threshold, boolean allUsers, String projectAllocationName, BigDecimal allocationAmount, BigDecimal cumulativeConsumption) {
		this.id = id;
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.name = name;
		this.threshold = threshold;
		this.allUsers = allUsers;
		this.projectAllocationName = projectAllocationName;
		this.allocationAmount = allocationAmount;
		this.cumulativeConsumption = cumulativeConsumption;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ExtendedAlarmEntity that = (ExtendedAlarmEntity) o;
		return threshold == that.threshold &&
			allUsers == that.allUsers &&
			Objects.equals(id, that.id) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(projectAllocationName, that.projectAllocationName) &&
			Objects.equals(allocationAmount, that.allocationAmount) &&
			Objects.equals(cumulativeConsumption, that.cumulativeConsumption);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, projectAllocationId, name, threshold, allUsers, projectAllocationName, allocationAmount, cumulativeConsumption);
	}
}
