/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.site_agent.IllegalStateTransitionException;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rabbitmq.site.models.CumulativeResourceUsageRecord;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Status;
import io.imunity.furms.rabbitmq.site.models.UserResourceUsageRecord;
import io.imunity.furms.site.api.message_resolver.ResourceUsageUpdater;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class SiteAgentResourceUsageService {
	private final ResourceUsageUpdater resourceUsageUpdater;

	SiteAgentResourceUsageService(ResourceUsageUpdater resourceUsageUpdater) {
		this.resourceUsageUpdater = resourceUsageUpdater;
	}

	@EventListener
	void receiveCumulativeResourceUsageRecord(Payload<CumulativeResourceUsageRecord> record) {
		if(record.header.status.equals(Status.FAILED)){
			throw new IllegalStateTransitionException("Usage is failed - it's not supported");
		}
		resourceUsageUpdater.updateUsage(
			ResourceUsage.builder()
				.projectId(record.body.projectIdentifier)
				.projectAllocationId(record.body.allocationIdentifier)
				.cumulativeConsumption(record.body.cumulativeConsumption)
				.probedAt(convertToUTCTime(record.body.probedAt))
				.build()
		);
	}

	@EventListener
	void receiveUserResourceUsageRecord(Payload<UserResourceUsageRecord> record) {
		if(record.header.status.equals(Status.FAILED)){
			throw new IllegalStateTransitionException("Usage is failed - it's not supported");
		}
		resourceUsageUpdater.updateUsage(
			UserResourceUsage.builder()
				.projectId(record.body.projectIdentifier)
				.projectAllocationId(record.body.allocationIdentifier)
				.fenixUserId(new FenixUserId(record.body.fenixUserId))
				.cumulativeConsumption(record.body.cumulativeConsumption)
				.consumedUntil(convertToUTCTime(record.body.consumedUntil))
				.build()
		);
	}
}
