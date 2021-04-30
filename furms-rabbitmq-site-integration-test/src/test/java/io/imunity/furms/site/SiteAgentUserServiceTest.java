/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionStatus;
import io.imunity.furms.domain.user_operation.UserRemoval;
import io.imunity.furms.domain.user_operation.UserRemovalStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rabbitmq.site.client.SiteAgentListenerConnector;
import io.imunity.furms.site.api.message_resolver.UserOperationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
class SiteAgentUserServiceTest {
	@Autowired
	private SiteAgentUserService siteAgentUserService;
	@Autowired
	private SiteAgentListenerConnector siteAgentListenerConnector;
	@Autowired
	private UserOperationMessageResolver userOperationMessageResolver;

	@BeforeEach
	void init(){
		siteAgentListenerConnector.connectListenerToQueue( "mock-site-pub");
	}

	@Test
	void shouldAddUser() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = CorrelationId.randomID();
		UserAddition userAddition = UserAddition.builder()
			.id("id")
			.siteId(new SiteId("id", new SiteExternalId("mock")))
			.projectId("projectId")
			.correlationId(correlationId)
			.build();
		FURMSUser user = FURMSUser.builder()
			.fenixUserId(new FenixUserId("id"))
			.email("email")
			.build();
		siteAgentUserService.addUser(userAddition, user);

		verify(userOperationMessageResolver, timeout(10000)).updateStatus(correlationId, UserAdditionStatus.ACKNOWLEDGED);
		verify(userOperationMessageResolver, timeout(10000)).update(any());
	}

	@Test
	void shouldRemoveUser() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = CorrelationId.randomID();
		UserRemoval userRemoval = UserRemoval.builder()
			.id("id")
			.siteId(new SiteId("id", new SiteExternalId("mock")))
			.projectId("projectId")
			.correlationId(correlationId)
			.build();

		siteAgentUserService.removeUser(userRemoval);

		verify(userOperationMessageResolver, timeout(10000)).updateStatus(correlationId, UserRemovalStatus.ACKNOWLEDGED);
		verify(userOperationMessageResolver, timeout(10000)).updateStatus(correlationId, UserRemovalStatus.REMOVED);
	}
}
