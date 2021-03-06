/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import io.imunity.furms.site.api.status_updater.UserOperationStatusUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
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
	private UserOperationStatusUpdater userOperationStatusUpdater;

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

		verify(userOperationStatusUpdater, timeout(10000)).updateStatus(correlationId, UserStatus.ADDING_ACKNOWLEDGED, Optional.empty());
		verify(userOperationStatusUpdater, timeout(10000)).update(any());
	}

	@Test
	void shouldRemoveUser() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = CorrelationId.randomID();
		UserAddition userAdditionJob = UserAddition.builder()
			.id("id")
			.correlationId(correlationId)
			.siteId(new SiteId("id", new SiteExternalId("mock")))
			.projectId("projectId")
			.build();

		siteAgentUserService.removeUser(userAdditionJob);

		verify(userOperationStatusUpdater, timeout(10000)).updateStatus(correlationId, UserStatus.REMOVAL_ACKNOWLEDGED, Optional.empty());
		verify(userOperationStatusUpdater, timeout(10000)).updateStatus(correlationId, UserStatus.REMOVED, Optional.empty());
	}
}
