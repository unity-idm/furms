/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rabbitmq.site.models.CumulativeResourceUsageRecord;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Status;
import io.imunity.furms.rabbitmq.site.models.UserResourceUsageRecord;
import io.imunity.furms.site.api.message_resolver.ResourceUsageUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class SiteAgentResourceUsageService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ResourceUsageUpdater resourceUsageUpdater;

	SiteAgentResourceUsageService(ResourceUsageUpdater resourceUsageUpdater) {
		this.resourceUsageUpdater = resourceUsageUpdater;
	}

	@EventListener
	void receiveCumulativeResourceUsageRecord(Payload<CumulativeResourceUsageRecord> record) {
		if(record.header.status.equals(Status.FAILED)){
			LOG.info("Usage Message with correlation {} is failed - it's not supported", record.header.messageCorrelationId);
			return;
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
			LOG.info("Usage Message with correlation {} is failed - it's not supported", record.header.messageCorrelationId);
			return;
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
