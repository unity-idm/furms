/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.message_resolver;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationResult;

public interface SSHKeyOperationMessageResolver {

	void updateStatus(CorrelationId correlationId, SSHKeyOperationResult result);	
}
