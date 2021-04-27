/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationRequest;

import java.time.ZoneOffset;

import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

class ProjectAllocationMapper {
	static AgentProjectAllocationInstallationRequest map(ProjectAllocationResolved projectAllocation){
		return AgentProjectAllocationInstallationRequest.builder()
			.projectIdentifier(projectAllocation.projectId)
			.allocationIdentifier(projectAllocation.id)
			.resourceCreditIdentifier(projectAllocation.resourceCredit.id)
			.resourceType(projectAllocation.resourceType.name)
			.amount(projectAllocation.amount.doubleValue())
			.validFrom(convertToZoneTime(projectAllocation.resourceCredit.utcStartTime, ZoneOffset.UTC))
			.validTo(convertToZoneTime(projectAllocation.resourceCredit.utcEndTime, ZoneOffset.UTC))
			.build();
	}
}
