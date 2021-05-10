/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site;

import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.ACK;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.DONE;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationError;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationResult;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rabbitmq.site.client.SiteAgentListenerConnector;
import io.imunity.furms.site.api.message_resolver.SSHKeyOperationMessageResolver;
import io.imunity.furms.site.api.ssh_keys.SSHKeyAddition;
import io.imunity.furms.site.api.ssh_keys.SSHKeyRemoval;
import io.imunity.furms.site.api.ssh_keys.SSHKeyUpdating;
import io.imunity.furms.site.api.ssh_keys.SiteAgentSSHKeyOperationService;

@SpringBootTest
class SiteAgentSSHKeyOperationServiceTest {
	@Autowired
	private SiteAgentSSHKeyOperationService siteAgent;
	@Autowired
	private SiteAgentListenerConnector siteAgentListenerConnector;
	@Autowired
	private SSHKeyOperationMessageResolver sshKeyOperationService;

	@BeforeEach
	void init() {
		siteAgentListenerConnector.connectListenerToQueue("mock-site-pub");
	}

	@Test
	void shouldAddKey() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = CorrelationId.randomID();

		siteAgent.addSSHKey(correlationId, SSHKeyAddition.builder().siteExternalId(new SiteExternalId("mock"))
				.publicKey("key").user(new FenixUserId("xx")).build());

		verify(sshKeyOperationService, timeout(10000)).updateStatus(correlationId,
				new SSHKeyOperationResult(ACK, new SSHKeyOperationError(null, null)));
		verify(sshKeyOperationService, timeout(10000)).updateStatus(correlationId,
				new SSHKeyOperationResult(DONE, new SSHKeyOperationError(null, null)));
	}

	@Test
	void shouldRemoveKey() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = CorrelationId.randomID();

		siteAgent.removeSSHKey(correlationId, SSHKeyRemoval.builder().siteExternalId(new SiteExternalId("mock"))
				.publicKey("key").user(new FenixUserId("xx")).build());

		verify(sshKeyOperationService, timeout(10000)).updateStatus(correlationId,
				new SSHKeyOperationResult(ACK, new SSHKeyOperationError(null, null)));
		verify(sshKeyOperationService, timeout(10000)).updateStatus(correlationId,
				new SSHKeyOperationResult(DONE, new SSHKeyOperationError(null, null)));
	}

	@Test
	void shouldUpdateKey() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = CorrelationId.randomID();

		siteAgent.updateSSHKey(correlationId,
				SSHKeyUpdating.builder().siteExternalId(new SiteExternalId("mock")).oldPublicKey("key")
						.newPublicKey("key2").user(new FenixUserId("xx")).build());

		verify(sshKeyOperationService, timeout(10000)).updateStatus(correlationId,
				new SSHKeyOperationResult(ACK, new SSHKeyOperationError(null, null)));
		verify(sshKeyOperationService, timeout(10000)).updateStatus(correlationId,
				new SSHKeyOperationResult(DONE, new SSHKeyOperationError(null, null)));
	}
}
