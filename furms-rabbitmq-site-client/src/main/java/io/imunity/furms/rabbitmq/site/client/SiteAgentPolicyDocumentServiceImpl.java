/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.AgentPolicyUpdate;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.UserPolicyAcceptanceUpdate;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.rabbitmq.site.client.PolicyAcceptancesMapper.getPolicyAcceptances;
import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Service
class SiteAgentPolicyDocumentServiceImpl implements SiteAgentPolicyDocumentService {
	private final RabbitTemplate rabbitTemplate;

	SiteAgentPolicyDocumentServiceImpl(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void updateUsersPolicyAcceptances(SiteExternalId siteExternalId, UserPolicyAcceptancesWithServicePolicies userPolicyAcceptances) {
		UserPolicyAcceptanceUpdate request = new UserPolicyAcceptanceUpdate(userPolicyAcceptances.user.fenixUserId.get().id, getPolicyAcceptances(userPolicyAcceptances));
		String queueName = getFurmsPublishQueueName(siteExternalId);
		rabbitTemplate.convertAndSend(queueName, new Payload<>(new Header(VERSION, CorrelationId.randomID().id), request));
	}

	@Override
	public void updatePolicyDocument(SiteExternalId siteExternalId, PolicyDocument policyDocument,
	                                 Optional<InfraServiceId> serviceIdentifier) {
		AgentPolicyUpdate request = AgentPolicyUpdate.builder()
			.policyIdentifier(policyDocument.id.id.toString())
			.policyName(policyDocument.name)
			.currentVersion(policyDocument.isRevisionDefined() ? policyDocument.revision : null)
			.serviceIdentifier(serviceIdentifier.flatMap(x -> Optional.ofNullable(x.id)).map(UUID::toString).orElse(null))
			.build();
		String queueName = getFurmsPublishQueueName(siteExternalId);
		rabbitTemplate.convertAndSend(queueName, new Payload<>(new Header(VERSION, CorrelationId.randomID().id), request));
	}

	@Override
	public void updatePolicyDocument(SiteExternalId siteExternalId, PolicyDocument policyDocument) {
		updatePolicyDocument(siteExternalId, policyDocument, Optional.empty());
	}
}
