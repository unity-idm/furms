/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.rabbitmq.site.models.AgentProjectResourceAllocationRequest;

class ProjectAllocationMapper {
	static AgentProjectResourceAllocationRequest map(ProjectAllocationResolved projectAllocation){
		return AgentProjectResourceAllocationRequest.builder()
			.projectIdentifier(projectAllocation.projectId)
			.allocationIdentifier(projectAllocation.id)
			.resourceCreditIdentifier(projectAllocation.resourceCredit.id)
			.resourceType(projectAllocation.resourceType.name)
			.amount(projectAllocation.amount.doubleValue())
			.validFrom(projectAllocation.resourceCredit.utcStartTime)
			.validTo(projectAllocation.resourceCredit.utcEndTime)
			.build();
	}
}
