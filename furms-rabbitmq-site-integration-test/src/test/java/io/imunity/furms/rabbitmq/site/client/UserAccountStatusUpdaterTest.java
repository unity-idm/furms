/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.SiteAgentSetUserAccountStatusRequest;
import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.site.api.status_updater.UserAccountStatusUpdater;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.imunity.furms.domain.users.UserStatus.ENABLED;
import static io.imunity.furms.domain.users.UserAccountStatusUpdateReason.SECURITY_INCIDENT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class UserAccountStatusUpdaterTest extends IntegrationTestBase {

	@Autowired
	private UserAccountStatusUpdater userAccountStatusUpdater;

	@Test
	void shouldReceiveAckOnUserUpdateStatus() {
		final CorrelationId correlationId = CorrelationId.randomID();
		final FenixUserId fenixUserId = new FenixUserId("fenixId");
		final SiteExternalId siteExternalId = new SiteExternalId("mock");

		userAccountStatusUpdater.setStatus(new SiteAgentSetUserAccountStatusRequest(
				siteExternalId, correlationId, fenixUserId, ENABLED, SECURITY_INCIDENT));

		verify(flowUpdater, timeout(DEFAULT_RECEIVE_TIMEOUT_MILIS)).updateSetUserStatusRequestAck(eq(correlationId));
	}

	@Test
	void shouldReceiveResultOnUserUpdateStatus() {
		final CorrelationId correlationId = CorrelationId.randomID();
		final FenixUserId fenixUserId = new FenixUserId("fenixId");
		final SiteExternalId siteExternalId = new SiteExternalId("mock");

		userAccountStatusUpdater.setStatus(new SiteAgentSetUserAccountStatusRequest(
				siteExternalId, correlationId, fenixUserId, ENABLED, SECURITY_INCIDENT));

		verify(flowUpdater, timeout(DEFAULT_RECEIVE_TIMEOUT_MILIS)).updateSetUserStatusResult(eq(correlationId));
	}
}
