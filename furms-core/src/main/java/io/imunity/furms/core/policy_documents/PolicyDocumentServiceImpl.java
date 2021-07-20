/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.policy_documents.PolicyAgreement;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentCreateEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyDocumentRemovedEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentUpdatedEvent;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static java.util.stream.Collectors.toMap;

@Service
class PolicyDocumentServiceImpl implements PolicyDocumentService {
	private static final Logger LOG = LoggerFactory.getLogger(PolicyDocumentServiceImpl.class);

	private final PolicyDocumentRepository policyDocumentRepository;
	private final PolicyDocumentValidator validator;
	private final PolicyDocumentDAO policyDocumentDAO;
	private final ApplicationEventPublisher publisher;

	PolicyDocumentServiceImpl(PolicyDocumentRepository policyDocumentRepository, PolicyDocumentValidator validator, PolicyDocumentDAO policyDocumentDAO, ApplicationEventPublisher publisher) {
		this.policyDocumentRepository = policyDocumentRepository;
		this.validator = validator;
		this.policyDocumentDAO = policyDocumentDAO;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Optional<PolicyDocument> findById(String siteId, PolicyId id) {
		LOG.debug("Getting Policy Document with id={}", id);
		return policyDocumentRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<PolicyDocument> findAllBySiteId(String siteId) {
		LOG.debug("Getting all Policy Document for site id={}", siteId);
		return policyDocumentRepository.findAllBySiteId(siteId);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public Set<PolicyDocumentExtended> findAllByUserId(FenixUserId userId) {
		LOG.debug("Getting all Policy Document for user id={}", userId.id);
		Map<PolicyId, PolicyAgreement> collect = policyDocumentDAO.getPolicyAgreements(userId).stream()
			.collect(toMap(x -> x.policyDocumentId, x -> x));
		return policyDocumentRepository.findAllByUserId(userId, x ->
			Optional.ofNullable(collect.get(x))
				.map(y -> y.decisionTs)
				.map(y -> LocalDateTime.ofInstant(y, ZoneOffset.UTC.normalized()))
				.orElse(null)
		);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public void addUserPolicyAgreement(FenixUserId userId, PolicyAgreement policyAgreement) {
		LOG.debug("Adding Policy Document id={} for user id={}", policyAgreement.policyDocumentId.id, userId.id);
		policyDocumentDAO.addUserPolicyAgreement(userId, policyAgreement);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "policyDocument.siteId")
	public void create(PolicyDocument policyDocument) {
		LOG.debug("Creating Policy Document for site id={}", policyDocument.siteId);
		validator.validateCreate(policyDocument);
		PolicyId policyId = policyDocumentRepository.create(policyDocument);
		publisher.publishEvent(new PolicyDocumentCreateEvent(policyId));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "policyDocument.siteId")
	public void update(PolicyDocument policyDocument) {
		LOG.debug("Updating Policy Document for site id={}", policyDocument.siteId);
		validator.validateUpdate(policyDocument);
		PolicyId policyId = policyDocumentRepository.update(policyDocument, false);
		publisher.publishEvent(new PolicyDocumentUpdatedEvent(policyId));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "policyDocument.siteId")
	public void updateWithRevision(PolicyDocument policyDocument) {
		LOG.debug("Updating Policy Document for site id={}", policyDocument.siteId);
		validator.validateUpdate(policyDocument);
		PolicyId policyId = policyDocumentRepository.update(policyDocument, true);
		publisher.publishEvent(new PolicyDocumentUpdatedEvent(policyId));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "siteId")
	public void delete(String siteId, PolicyId policyId) {
		LOG.debug("Deleting Policy Document {} for site id={}", policyId.id, siteId);
		policyDocumentRepository.deleteById(policyId);
		publisher.publishEvent(new PolicyDocumentRemovedEvent(policyId));
	}
}
