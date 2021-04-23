/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.message_resolver;

import java.util.Optional;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationStatus;

public interface SSHKeyOperationMessageResolver {
	
	void onSSHKeyRemovalFromSite(CorrelationId correlationId);
	void updateStatus(CorrelationId correlationId, SSHKeyOperationStatus status,  Optional<String> error);
	
	
}
