/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectDeallocationRequest;

import java.time.ZoneOffset;

class ProjectAllocationMapper {
	static AgentProjectAllocationInstallationRequest mapAllocation(ProjectAllocationResolved projectAllocation){
		return AgentProjectAllocationInstallationRequest.builder()
			.projectIdentifier(projectAllocation.projectId)
			.allocationIdentifier(projectAllocation.id)
			.resourceCreditIdentifier(projectAllocation.resourceCredit.id)
			.resourceType(projectAllocation.resourceType.name)
			.amount(projectAllocation.amount.doubleValue())
			.validFrom(projectAllocation.resourceCredit.utcStartTime.atOffset(ZoneOffset.UTC))
			.validTo(projectAllocation.resourceCredit.utcEndTime.atOffset(ZoneOffset.UTC))
			.build();
	}

	static AgentProjectDeallocationRequest mapDeallocation(ProjectAllocationResolved projectAllocation){
		return AgentProjectDeallocationRequest.builder()
			.projectIdentifier(projectAllocation.projectId)
			.allocationIdentifier(projectAllocation.id)
			.resourceCreditIdentifier(projectAllocation.resourceCredit.id)
			.resourceType(projectAllocation.resourceType.name)
			.build();
	}
}
