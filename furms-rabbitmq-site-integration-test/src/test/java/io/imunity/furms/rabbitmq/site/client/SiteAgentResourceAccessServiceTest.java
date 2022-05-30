/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import io.imunity.furms.site.api.status_updater.UserAllocationStatusUpdater;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class SiteAgentResourceAccessServiceTest extends IntegrationTestBase {

	@Autowired
	private SiteAgentResourceAccessService siteAgentResourceAccessService;
	@Autowired
	private UserAllocationStatusUpdater userAllocationStatusUpdater;

	@Test
	void shouldGrantAccess() {
		CorrelationId correlationId = CorrelationId.randomID();
		GrantAccess grantAccess = GrantAccess.builder()
			.allocationId(new ProjectAllocationId(UUID.randomUUID()))
			.projectId(new ProjectId(UUID.randomUUID()))
			.siteId(new SiteId(UUID.randomUUID().toString(), "mock"))
			.fenixUserId(new FenixUserId("id"))
			.build();
		FURMSUser furmsUser = FURMSUser.builder()
			.email("admin@admin.pl")
			.build();
		siteAgentResourceAccessService.grantAccess(correlationId, grantAccess, furmsUser);

		verify(userAllocationStatusUpdater, timeout(10000)).update(
			correlationId,
			AccessStatus.GRANT_ACKNOWLEDGED,
			null
		);
		verify(userAllocationStatusUpdater, timeout(10000)).update(
			correlationId,
			AccessStatus.GRANTED,
			null
		);
	}

	@Test
	void shouldRevokeAccess() {
		CorrelationId correlationId = CorrelationId.randomID();
		GrantAccess grantAccess = GrantAccess.builder()
			.allocationId(new ProjectAllocationId(UUID.randomUUID()))
			.projectId(new ProjectId(UUID.randomUUID()))
			.siteId(new SiteId(UUID.randomUUID().toString(), "mock"))
			.fenixUserId(new FenixUserId("id"))
			.build();
		siteAgentResourceAccessService.revokeAccess(correlationId, grantAccess);

		verify(userAllocationStatusUpdater, timeout(10000)).update(
			correlationId,
			AccessStatus.REVOKE_ACKNOWLEDGED,
			null
		);
		verify(userAllocationStatusUpdater, timeout(10000)).update(
			correlationId,
			AccessStatus.REVOKED,
			null
		);
	}
}
