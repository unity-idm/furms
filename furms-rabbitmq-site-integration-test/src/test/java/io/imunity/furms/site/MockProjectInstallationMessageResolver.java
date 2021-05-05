/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.site;

import io.imunity.furms.domain.project_installation.*;
import org.springframework.stereotype.Service;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;

@Service
public class MockProjectInstallationMessageResolver implements ProjectInstallationMessageResolver {


	@Override
	public void update(CorrelationId correlationId, ProjectInstallationResult result) {

	}

	@Override
	public void update(CorrelationId correlationId, ProjectUpdateResult result) {

	}
}
