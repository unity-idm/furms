/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.MessageStatus;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyAddition;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.site.api.message_resolver.SSHKeyOperationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentSSHKeyOperationService;

@SpringBootTest
class SiteAgentKeyOperationServiceTest {
	@Autowired
	private SiteAgentSSHKeyOperationService siteAgent;
	@MockBean
	private ProjectInstallationMessageResolver projectInstallationService;
	@MockBean
	private SSHKeyOperationMessageResolver sshKeyOperationService;

	@Test
	void shouldAddKey() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = new CorrelationId("id");

		siteAgent.addSSHKey(correlationId, SSHKeyAddition.builder().siteExternalId(new SiteExternalId("mock"))
				.publicKey("key").userUid("uid").user(new FenixUserId("xx")).build());

		verify(sshKeyOperationService, timeout(10000)).addSSHKeyAck(correlationId);
		verify(sshKeyOperationService, timeout(10000)).onSSHKeyAddToSite(correlationId, MessageStatus.OK,
				Optional.empty());
	}
}
