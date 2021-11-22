/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.status_updater.SiteAgentUserStatusFlowUpdater;
import org.springframework.stereotype.Service;

@Service
class SiteAgentUserAccountStatusFlowUpdater implements SiteAgentUserStatusFlowUpdater {

	@Override
	public void updateSetUserStatusRequestAck(CorrelationId correlationId) {
	}

	@Override
	public void updateSetUserStatusResult(CorrelationId correlationId) {
	}
}
