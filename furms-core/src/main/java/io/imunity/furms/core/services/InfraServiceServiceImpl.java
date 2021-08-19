/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.services.CreateServiceEvent;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.RemoveServiceEvent;
import io.imunity.furms.domain.services.UpdateServiceEvent;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
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
	private final ApplicationEventPublisher publisher;

	InfraServiceServiceImpl(InfraServiceRepository infraServiceRepository,
	                        InfraServiceServiceValidator validator,
	                        SiteAgentPolicyDocumentService siteAgentPolicyDocumentService,
	                        SiteRepository siteRepository, ApplicationEventPublisher publisher) {
		this.infraServiceRepository = infraServiceRepository;
		this.validator = validator;
		this.siteAgentPolicyDocumentService = siteAgentPolicyDocumentService;
		this.siteRepository = siteRepository;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Optional<InfraService> findById(String id, String siteId) {
		return infraServiceRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<InfraService> findAll(String siteId) {
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
		String id = infraServiceRepository.create(infraService);
		publisher.publishEvent(new CreateServiceEvent(infraService.id));
		LOG.info("InfraService with given ID: {} was created: {}", id, infraService);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "infraService.siteId")
	public void update(InfraService infraService) {
		validator.validateUpdate(infraService);
		InfraService oldInfraService = infraServiceRepository.findById(infraService.id)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Infra service id %s doesn't exist", infraService.id)));
		infraServiceRepository.update(infraService);
		if(isPolicyDisengage(infraService, oldInfraService))
			sendUpdateToSite(oldInfraService);
		publisher.publishEvent(new UpdateServiceEvent(infraService.id));
		LOG.info("InfraService was updated {}", infraService);
	}

	private void sendUpdateToSite(InfraService oldInfraService) {
		siteAgentPolicyDocumentService.updatePolicyDocument(
			siteRepository.findByIdExternalId(oldInfraService.id),
			PolicyDocument.builder()
				.id(oldInfraService.policyId)
				.revision(0)
				.build(),
			oldInfraService.id);
	}

	private boolean isPolicyDisengage(InfraService infraService, InfraService oldInfraService) {
		return oldInfraService.policyId != null && infraService.policyId == null;
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "siteId")
	public void delete(String infraServiceId, String siteId) {
		validator.validateDelete(infraServiceId);
		infraServiceRepository.delete(infraServiceId);
		publisher.publishEvent(new RemoveServiceEvent(infraServiceId));
		LOG.info("InfraService with given ID: {} was deleted", infraServiceId);
	}
}
