/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_usage;

import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;

import java.math.BigDecimal;
import java.util.Objects;

public class ResourceUsageUpdatedEvent implements FurmsEvent {
	public final BigDecimal amount;
	public final BigDecimal cumulativeConsumption;
	public final ProjectAllocationId projectAllocationId;

	public ResourceUsageUpdatedEvent(BigDecimal amount, BigDecimal cumulativeConsumption, ProjectAllocationId projectAllocationId) {
		this.amount = amount;
		this.cumulativeConsumption = cumulativeConsumption;
		this.projectAllocationId = projectAllocationId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceUsageUpdatedEvent that = (ResourceUsageUpdatedEvent) o;
		return Objects.equals(amount, that.amount) && Objects.equals(cumulativeConsumption, that.cumulativeConsumption) && Objects.equals(projectAllocationId, that.projectAllocationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, cumulativeConsumption, projectAllocationId);
	}

	@Override
	public String toString() {
		return "ResourceUsageUpdatedEvent{" +
			"amount=" + amount +
			", cumulativeConsumption=" + cumulativeConsumption +
			", projectAllocationId='" + projectAllocationId + '\'' +
			'}';
	}
}
