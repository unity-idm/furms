/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_installation.*;
import org.springframework.stereotype.Service;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationStatusUpdater;

@Service
public class MockProjectInstallationStatusUpdater implements ProjectInstallationStatusUpdater {


	@Override
	public void update(CorrelationId correlationId, ProjectInstallationResult result) {

	}

	@Override
	public void update(CorrelationId correlationId, ProjectUpdateResult result) {

	}
}
