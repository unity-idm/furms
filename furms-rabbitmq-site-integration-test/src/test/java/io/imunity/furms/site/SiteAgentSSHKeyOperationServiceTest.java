/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site;

import static io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationStatus.ACK;
import static io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationStatus.DONE;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyAddition;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyRemoval;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyUpdating;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rabbitmq.site.client.SiteAgentListenerConnector;
import io.imunity.furms.site.api.SiteExternalIdsResolver;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.site.api.message_resolver.SSHKeyOperationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentSSHKeyOperationService;

@SpringBootTest
class SiteAgentSSHKeyOperationServiceTest {
	@Autowired
	private SiteAgentSSHKeyOperationService siteAgent;
	@Autowired
	private SiteAgentListenerConnector siteAgentListenerConnector;
	@MockBean
	private ProjectInstallationMessageResolver projectInstallationService;
	@MockBean
	private SSHKeyOperationMessageResolver sshKeyOperationService;
	@MockBean
	private SiteExternalIdsResolver siteExternalIdsResolver;

	@BeforeEach
	void init() {
		siteAgentListenerConnector.connectListenerToQueue("mock-site-pub");
	}

	@Test
	void shouldAddKey() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = CorrelationId.randomID();

		siteAgent.addSSHKey(correlationId, SSHKeyAddition.builder().siteExternalId(new SiteExternalId("mock"))
				.publicKey("key").userUid("uid").user(new FenixUserId("xx")).build());

		verify(sshKeyOperationService, timeout(10000)).updateStatus(correlationId, ACK, Optional.empty());
		verify(sshKeyOperationService, timeout(10000)).updateStatus(correlationId, DONE, Optional.empty());
	}

	@Test
	void shouldRemoveKey() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = CorrelationId.randomID();

		siteAgent.removeSSHKey(correlationId, SSHKeyRemoval.builder().siteExternalId(new SiteExternalId("mock"))
				.publicKey("key").userUid("uid").user(new FenixUserId("xx")).build());

		verify(sshKeyOperationService, timeout(10000)).updateStatus(correlationId, ACK, Optional.empty());
		verify(sshKeyOperationService, timeout(10000)).updateStatus(correlationId, DONE, Optional.empty());
	}

	@Test
	void shouldUpdateKey() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = CorrelationId.randomID();

		siteAgent.updateSSHKey(correlationId,
				SSHKeyUpdating.builder().siteExternalId(new SiteExternalId("mock")).oldPublicKey("key")
						.newPublicKey("key2").userUid("uid").user(new FenixUserId("xx"))
						.build());

		verify(sshKeyOperationService, timeout(10000)).updateStatus(correlationId, ACK, Optional.empty());
		verify(sshKeyOperationService, timeout(10000)).updateStatus(correlationId, DONE, Optional.empty());
	}
}
