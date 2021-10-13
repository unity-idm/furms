/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.mocks;

import org.springframework.stereotype.Service;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationResult;
import io.imunity.furms.site.api.status_updater.SSHKeyOperationStatusUpdater;

@Service
public class SSHKeyOperationStatusUpdaterMock implements SSHKeyOperationStatusUpdater {

	@Override
	public void updateStatus(CorrelationId correlationId, SSHKeyOperationResult result) {
		
	}
	

}
