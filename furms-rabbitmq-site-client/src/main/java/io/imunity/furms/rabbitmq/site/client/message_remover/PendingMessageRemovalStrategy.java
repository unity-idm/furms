/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_remover;

import io.imunity.furms.domain.site_agent.CorrelationId;

interface PendingMessageRemovalStrategy {
	boolean isApplicable(String name);
	void remove(CorrelationId correlationId);
}
