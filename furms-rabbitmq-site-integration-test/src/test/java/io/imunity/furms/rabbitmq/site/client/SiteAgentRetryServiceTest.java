/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.rabbitmq.site.models.AgentUser;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.UserProjectAddRequest;
import io.imunity.furms.rabbitmq.site.models.converter.FurmsPayloadConverter;
import io.imunity.furms.site.api.site_agent.SiteAgentRetryService;
import io.imunity.furms.site.api.status_updater.ProjectAllocationInstallationStatusUpdater;
import io.imunity.furms.site.api.status_updater.UserOperationStatusUpdater;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.INSTALLED;
import static io.imunity.furms.rabbitmq.site.client.PolicyAcceptancesMapper.getPolicyAcceptances;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SiteAgentRetryServiceTest extends IntegrationTestBase {
	@Autowired
	private SiteAgentRetryService siteAgentRetryService;
	@Autowired
	private UserOperationStatusUpdater userOperationStatusUpdater;

	@Autowired
	private ProjectAllocationInstallationStatusUpdater projectAllocationInstallationStatusUpdater;

	@Test
	void shouldRetryUserAddingMessage() throws JsonProcessingException {
		CorrelationId correlationId = CorrelationId.randomID();
		FURMSUser user = FURMSUser.builder()
			.fenixUserId(new FenixUserId("id"))
			.email("email")
			.build();

		AgentUser agentUser = UserMapper.map(user);
		UserProjectAddRequest body = new UserProjectAddRequest(
			agentUser,
			getPolicyAcceptances(new UserPolicyAcceptancesWithServicePolicies(user, Set.of(), Optional.empty(), Set.of())), "projectId"
		);
		Payload<Body> payload = new Payload<>(new Header(VERSION, correlationId.id), body);
		String json = new FurmsPayloadConverter().mapper.writeValueAsString(payload);

		siteAgentRetryService.retry(new SiteExternalId("mock"), json);

		verify(userOperationStatusUpdater, timeout(10000)).updateStatus(correlationId, UserStatus.ADDING_ACKNOWLEDGED, Optional.empty());
		verify(userOperationStatusUpdater, timeout(10000)).update(any());
	}

	@Test
	void shouldRetryMessageFromStringTemplate() {
		String jsonTemplate = "{\"header\":{\"version\":\"1\",\"messageCorrelationId\":\"%s\"," +
			"\"status\":\"OK\"},\"body\":{\"ProjectResourceAllocationRequest\":{\"projectIdentifier\":\"%s\"," +
			"\"allocationIdentifier\":\"%s\",\"resourceCreditIdentifier\":\"%s\"," +
			"\"resourceType\":\"%s\",\"amount\":%s,\"validFrom\":\"%s\"," +
			"\"validTo\":\"%s\"}}}";

		CorrelationId correlationId = CorrelationId.randomID();
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId allocationId = new ProjectAllocationId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		String offsetDateTime = OffsetDateTime.of(2022, 3, 4, 5, 6, 4,5, ZoneOffset.UTC).toString();

		String json = String.format(jsonTemplate, correlationId.id, projectId.id, allocationId.id, resourceCreditId.id,
			"type", BigDecimal.ONE, offsetDateTime, offsetDateTime);

		when(projectAllocationInstallationStatusUpdater.isWaitingForInstallationConfirmation(allocationId)).thenReturn(true);

		siteAgentRetryService.retry(new SiteExternalId("mock"), json);

		verify(projectAllocationInstallationStatusUpdater, timeout(10000)).updateStatusToAck(correlationId);
		verify(projectAllocationInstallationStatusUpdater, timeout(10000)).updateStatus(allocationId, INSTALLED,
			Optional.empty());
	}
}
