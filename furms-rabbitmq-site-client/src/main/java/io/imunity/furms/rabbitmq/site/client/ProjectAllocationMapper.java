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
			.projectIdentifier(projectAllocation.projectId.id.toString())
			.allocationIdentifier(projectAllocation.id.id.toString())
			.resourceCreditIdentifier(projectAllocation.resourceCredit.id.id.toString())
			.resourceType(projectAllocation.resourceType.name)
			.amount(projectAllocation.amount)
			.validFrom(projectAllocation.resourceCredit.utcStartTime.atOffset(ZoneOffset.UTC))
			.validTo(projectAllocation.resourceCredit.utcEndTime.atOffset(ZoneOffset.UTC))
			.build();
	}

	static AgentProjectDeallocationRequest mapDeallocation(ProjectAllocationResolved projectAllocation){
		return AgentProjectDeallocationRequest.builder()
			.projectIdentifier(projectAllocation.projectId.id.toString())
			.allocationIdentifier(projectAllocation.id.id.toString())
			.resourceCreditIdentifier(projectAllocation.resourceCredit.id.id.toString())
			.resourceType(projectAllocation.resourceType.name)
			.build();
	}
}
