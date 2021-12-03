/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.site_agent_pending_message;

import io.imunity.furms.api.site_agent_pending_message.SiteAgentConnectionService;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SiteAgentConnectionServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private SiteAgentConnectionService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllPublicMethodsBeAnnotatedWithSecurityAnnotation(service.getClass());
	}

	@Test
	void userWith_SITE_READ_canFindAllBySiteId() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAll(new SiteId(site)));
	}

	@Test
	void userWith_SITE_WRITE_canRetry() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.retry(new SiteId(site), CorrelationId.randomID()));
	}

	@Test
	void userWith_SITE_WRITE_canDelete() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.retry(new SiteId(site), CorrelationId.randomID()));
	}

	@Test
	void userWith_SITE_READ_canGetSiteAgentStatus() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.getSiteAgentStatus(new SiteId(site)));
	}


}
