/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.site;

import org.springframework.stereotype.Service;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationResult;
import io.imunity.furms.site.api.message_resolver.SSHKeyOperationMessageResolver;

@Service
public class MockSSHKeyOperationMessageResolver implements SSHKeyOperationMessageResolver {

	@Override
	public void updateStatus(CorrelationId correlationId, SSHKeyOperationResult result) {
		
	}
	

}
