/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.policy_documents.PolicyNotificationService;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.services.InfraServiceCreatedEvent;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.services.InfraServiceRemovedEvent;
import io.imunity.furms.domain.services.InfraServiceUpdatedEvent;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;

@Service
class InfraServiceServiceImpl implements InfraServiceService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final InfraServiceRepository infraServiceRepository;
	private final InfraServiceServiceValidator validator;
	private final SiteAgentPolicyDocumentService siteAgentPolicyDocumentService;
	private final SiteRepository siteRepository;
	private final PolicyDocumentRepository policyDocumentRepository;
	private final ApplicationEventPublisher publisher;
	private final PolicyNotificationService policyNotificationService;

	InfraServiceServiceImpl(InfraServiceRepository infraServiceRepository,
	                        InfraServiceServiceValidator validator,
	                        SiteAgentPolicyDocumentService siteAgentPolicyDocumentService,
	                        SiteRepository siteRepository,
	                        PolicyDocumentRepository policyDocumentRepository,
	                        ApplicationEventPublisher publisher,
	                        PolicyNotificationService notificationDAO) {
		this.infraServiceRepository = infraServiceRepository;
		this.validator = validator;
		this.siteAgentPolicyDocumentService = siteAgentPolicyDocumentService;
		this.siteRepository = siteRepository;
		this.policyDocumentRepository = policyDocumentRepository;
		this.publisher = publisher;
		this.policyNotificationService = notificationDAO;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Optional<InfraService> findById(InfraServiceId id, SiteId siteId) {
		return infraServiceRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<InfraService> findAll(SiteId siteId) {
		return infraServiceRepository.findAll(siteId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public Set<InfraService> findAll() {
		return infraServiceRepository.findAll();
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "infraService.siteId")
	public void create(InfraService infraService) {
		validator.validateCreate(infraService);
		InfraServiceId id = infraServiceRepository.create(infraService);
		InfraService created = infraServiceRepository.findById(id).get();
		if(infraService.policyId != null && infraService.policyId.id != null)
			sendUpdateToSite(id, infraService);
		publisher.publishEvent(new InfraServiceCreatedEvent(created));
		LOG.info("InfraService with given ID: {} was created: {}", id, infraService);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "infraService.siteId")
	public void update(InfraService infraService) {
		validator.validateUpdate(infraService);
		InfraService oldInfraService = infraServiceRepository.findById(infraService.id)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Infra service id %s doesn't exist", infraService.id)));
		infraServiceRepository.update(infraService);
		handlePolicyChange(infraService, oldInfraService);
		publisher.publishEvent(new InfraServiceUpdatedEvent(oldInfraService, infraService));
		LOG.info("InfraService was updated {}", infraService);
	}

	private void handlePolicyChange(InfraService infraService, InfraService oldInfraService) {
		if(isPolicyChange(infraService, oldInfraService)) {
			sendUpdateToSite(infraService, oldInfraService);
			if (infraService.policyId != null && infraService.policyId.id != null) {
				policyNotificationService.notifyAllUsersAboutPolicyAssignmentChange(infraService);
			}
		}
	}

	private void sendUpdateToSite(InfraService infraService, InfraService oldInfraService) {
		int revision;
		PolicyDocument policyDocument;
		if(isPolicyDisengage(infraService, oldInfraService)){
			revision = -1;
			policyDocument = getPolicyDocument(oldInfraService.policyId);
		}
		else {
			policyDocument = getPolicyDocument(infraService.policyId);
			revision = policyDocument.revision;
		}

		sendUpdateToSite(infraService.id, infraService, revision, policyDocument);
	}

	private PolicyDocument getPolicyDocument(PolicyId policyId) {
		return policyDocumentRepository.findById(policyId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Policy id %s doesn't exist", policyId)));
	}

	private void sendUpdateToSite(InfraServiceId infraId, InfraService infraService) {
		PolicyDocument policyDocument = getPolicyDocument(infraService.policyId);

		sendUpdateToSite(infraId, infraService, policyDocument.revision, policyDocument);
	}

	private void sendUpdateToSite(InfraServiceId infraServiceId, InfraService infraService, int revision, PolicyDocument policyDocument) {
		siteAgentPolicyDocumentService.updatePolicyDocument(
			siteRepository.findByIdExternalId(infraService.siteId),
			PolicyDocument.builder()
				.id(policyDocument.id)
				.name(policyDocument.name)
				.revision(revision)
				.build(),
			Optional.of(infraServiceId));
	}

	private boolean isPolicyChange(InfraService infraService, InfraService oldInfraService) {
		return !Objects.equals(oldInfraService.policyId, infraService.policyId);
	}

	private boolean isPolicyDisengage(InfraService infraService, InfraService oldInfraService) {
		return (oldInfraService.policyId != null && oldInfraService.policyId.id != null) &&
			(infraService.policyId == null || infraService.policyId.id == null);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "siteId")
	public void delete(InfraServiceId infraServiceId, SiteId siteId) {
		validator.validateDelete(infraServiceId);
		InfraService infraService = infraServiceRepository.findById(infraServiceId).get();
		infraServiceRepository.delete(infraServiceId);
		publisher.publishEvent(new InfraServiceRemovedEvent(infraService));
		LOG.info("InfraService with given ID: {} was deleted", infraServiceId);
	}
}
