/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.message_remover;

import io.imunity.furms.domain.site_agent.CorrelationId;

public interface PendingMessageRemover {
	void remove(CorrelationId correlationId);
}
