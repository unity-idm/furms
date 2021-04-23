/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.message_resolver;

import java.util.Optional;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.MessageStatus;

public interface SSHKeyOperationMessageResolver {
	
	void addSSHKeyAck(CorrelationId correlationId);
	void onSSHKeyAddToSite(CorrelationId correlationId, MessageStatus status, Optional<String> optional);
	
	void removeSSHKeyAck(CorrelationId correlationId);
	void onSSHKeyRemovalFromSite(CorrelationId correlationId, MessageStatus status, Optional<String> optional);
	
	void updateSSHKeyAck(CorrelationId correlationId);
	void onSSHKeyUpdateOnSite(CorrelationId correlationId, MessageStatus status, Optional<String> optional);
	
	
}
