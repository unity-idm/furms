/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.SiteAgentSetUserAccountStatus;
import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.SetUserStatusRequestAck;
import io.imunity.furms.rabbitmq.site.models.SetUserStatusResult;
import io.imunity.furms.rabbitmq.site.models.Status;
import io.imunity.furms.site.api.status_updater.UserAccountStatusUpdater;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.imunity.furms.domain.users.UserStatus.ENABLED;
import static io.imunity.furms.domain.users.UserAccountStatusUpdateReason.SECURITY_INCIDENT;
import static io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentMock.MOCK_SITE_PUB;
import static org.assertj.core.api.Assertions.assertThat;

class UserAccountStatusUpdaterTest extends IntegrationTestBase {

	@Autowired
	private UserAccountStatusUpdater userAccountStatusUpdater;

	@Test
	void shouldReceiveAckOnUserUpdateStatus() {
		final CorrelationId correlationId = CorrelationId.randomID();
		final FenixUserId fenixUserId = new FenixUserId("fenixId");
		final SiteExternalId siteExternalId = new SiteExternalId("mock");

		userAccountStatusUpdater.setStatus(new SiteAgentSetUserAccountStatus(
				siteExternalId, correlationId, fenixUserId, ENABLED, SECURITY_INCIDENT));

		final Payload<SetUserStatusRequestAck> ack = (Payload<SetUserStatusRequestAck>)rabbitTemplate.receiveAndConvert(
				MOCK_SITE_PUB, DEFAULT_RECEIVE_TIMEOUT_MILIS);
		assertThat(ack).isNotNull();
		assertThat(ack.header.messageCorrelationId).isEqualTo(correlationId.id);
		assertThat(ack.header.status).isEqualTo(Status.OK);
	}

	@Test
	void shouldReceiveResultOnUserUpdateStatus() {
		final CorrelationId correlationId = CorrelationId.randomID();
		final FenixUserId fenixUserId = new FenixUserId("fenixId");
		final SiteExternalId siteExternalId = new SiteExternalId("mock");

		userAccountStatusUpdater.setStatus(new SiteAgentSetUserAccountStatus(
				siteExternalId, correlationId, fenixUserId, ENABLED, SECURITY_INCIDENT));

		final Payload<SetUserStatusResult> result = (Payload<SetUserStatusResult>)rabbitTemplate.receiveAndConvert(
				MOCK_SITE_PUB, DEFAULT_RECEIVE_TIMEOUT_MILIS);
		assertThat(result).isNotNull();
		assertThat(result.header.messageCorrelationId).isEqualTo(correlationId.id);
		assertThat(result.header.status).isEqualTo(Status.OK);
	}
}
