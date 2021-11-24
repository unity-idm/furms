/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.status_updater;

import io.imunity.furms.domain.site_agent.CorrelationId;

public interface SiteAgentUserStatusFlowUpdater {

	void updateSetUserStatusRequestAck(CorrelationId correlationId);
	void updateSetUserStatusResult(CorrelationId correlationId);

}
