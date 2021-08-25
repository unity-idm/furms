/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.AssignedPolicyDocument;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rabbitmq.site.models.Acceptance;
import io.imunity.furms.rabbitmq.site.models.AgentPolicyUpdate;
import io.imunity.furms.rabbitmq.site.models.UserPolicyAcceptanceUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
class SiteAgentPolicyDocumentServiceTest {
	@Autowired
	private SiteAgentListenerConnector siteAgentListenerConnector;
	@Autowired
	private SiteAgentPolicyDocumentServiceImpl siteAgentPolicyDocumentService;
	@Autowired
	private SiteAgentPolicyDocumentReceiverMock receiverMock;

	@BeforeEach
	void init(){
		siteAgentListenerConnector.connectListenerToQueue( "mock-site-pub");
	}

	@Test
	void shouldUpdatePolicyDocument() {
		SiteExternalId siteExternalId = new SiteExternalId("mock");
		UUID uuid = UUID.randomUUID();
		PolicyId id = new PolicyId(uuid);
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(id)
			.revision(1)
			.name("policyName")
			.build();
		siteAgentPolicyDocumentService.updatePolicyDocument(siteExternalId, policyDocument);

		verify(receiverMock, timeout(10000)).process(
			AgentPolicyUpdate.builder()
				.policyIdentifier(uuid.toString())
				.policyName("policyName")
				.currentVersion(1)
				.serviceIdentifier(null)
				.build()
		);
	}

	@Test
	void shouldUpdateServicePolicyDocument() {
		SiteExternalId siteExternalId = new SiteExternalId("mock");
		UUID uuid = UUID.randomUUID();
		PolicyId id = new PolicyId(uuid);
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(id)
			.revision(1)
			.name("policyName")
			.build();
		siteAgentPolicyDocumentService.updatePolicyDocument(siteExternalId, policyDocument, "serviceId");

		verify(receiverMock, timeout(10000)).process(
			AgentPolicyUpdate.builder()
				.policyIdentifier(uuid.toString())
				.policyName("policyName")
				.currentVersion(1)
				.serviceIdentifier("serviceId")
				.build()
		);
	}

	@Test
	void shouldUpdateUsersPolicyAcceptances() {
		SiteExternalId siteExternalId = new SiteExternalId("mock");
		UUID uuid = UUID.randomUUID();
		PolicyId policyId = new PolicyId(uuid);
		FenixUserId fenixUserId = new FenixUserId("id");
		FURMSUser furmsUser = FURMSUser.builder()
			.email("email")
			.fenixUserId(fenixUserId)
			.build();
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.policyDocumentRevision(1)
			.acceptanceStatus(PolicyAcceptanceStatus.ACCEPTED)
			.build();
		AssignedPolicyDocument servicePolicyDocument = AssignedPolicyDocument.builder()
			.id(policyId)
			.revision(2)
			.serviceId("serviceId")
			.build();

		UserPolicyAcceptancesWithServicePolicies userPolicyAcceptancesWithServicePolicies =
			new UserPolicyAcceptancesWithServicePolicies(furmsUser, Set.of(policyAcceptance), Optional.empty(), Set.of(servicePolicyDocument));
		siteAgentPolicyDocumentService.updateUsersPolicyAcceptances(siteExternalId, userPolicyAcceptancesWithServicePolicies);

		verify(receiverMock, timeout(10000)).process(
			new UserPolicyAcceptanceUpdate(fenixUserId.id, List.of(
				io.imunity.furms.rabbitmq.site.models.PolicyAcceptance.builder()
					.policyIdentifier(uuid.toString())
					.currentVersion(2)
					.processedVersion(1)
					.serviceIdentifier("serviceId")
					.acceptanceStatus(Acceptance.ACCEPTED)
					.build()
			))
		);
	}
}
