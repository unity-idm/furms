/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.policy_documents.PolicyAgreement;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentCreateEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyDocumentRemovedEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentUpdatedEvent;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.users.FURMSUser;
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
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Service
class PolicyDocumentServiceImpl implements PolicyDocumentService {
	private static final Logger LOG = LoggerFactory.getLogger(PolicyDocumentServiceImpl.class);

	private final AuthzService authzService;
	private final PolicyDocumentRepository policyDocumentRepository;
	private final PolicyDocumentValidator validator;
	private final PolicyDocumentDAO policyDocumentDAO;
	private final ApplicationEventPublisher publisher;

	PolicyDocumentServiceImpl(PolicyDocumentRepository policyDocumentRepository, PolicyDocumentValidator validator,
	                          PolicyDocumentDAO policyDocumentDAO, AuthzService authzService, ApplicationEventPublisher publisher) {
		this.policyDocumentRepository = policyDocumentRepository;
		this.validator = validator;
		this.policyDocumentDAO = policyDocumentDAO;
		this.authzService = authzService;
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
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<FURMSUser> findAllUserWithoutPolicyAgreement(String siteId, PolicyId policyId) {
		LOG.debug("Getting all Users which not accepted Policy Document {}", policyId.id);

		PolicyDocument policyDocument = policyDocumentRepository.findById(policyId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Policy Document %s doesn't exist", policyId.id)));

		return policyDocumentDAO.getUserPolicyAgreements(siteId).stream()
			.filter(userAgreement -> userAgreement.policyAgreements.stream()
				.noneMatch(policyAgreement -> policyAgreement.policyDocumentId.equals(policyDocument.id) &&
					policyAgreement.policyDocumentRevision == policyDocument.revision)
			)
			.filter(agreement -> agreement.user.fenixUserId.isPresent())
			.map(agreement -> agreement.user)
			.collect(Collectors.toSet());
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public Set<PolicyDocumentExtended> findAllByCurrentUser() {
		FenixUserId userId = authzService.getCurrentAuthNUser().fenixUserId
			.orElseThrow(() -> new IllegalArgumentException("User have to be central IDP user"));

		LOG.debug("Getting all Policy Document for user id={}", userId.id);

		Map<PolicyId, PolicyAgreement> collect = policyDocumentDAO.getPolicyAgreements(userId).stream()
			.collect(toMap(policyAgreement -> policyAgreement.policyDocumentId, identity()));

		return policyDocumentRepository.findAllByUserId(userId, (policyId, revision) ->
			Optional.ofNullable(collect.get(policyId))
				.filter(policyAgreement -> policyAgreement.policyDocumentRevision == revision)
				.map(policyAgreement -> policyAgreement.decisionTs)
				.map(policyAgreement -> LocalDateTime.ofInstant(policyAgreement, ZoneOffset.UTC.normalized()))
				.orElse(null)
		);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public void addCurrentUserPolicyAgreement(PolicyAgreement policyAgreement) {
		FenixUserId userId = authzService.getCurrentAuthNUser().fenixUserId
			.orElseThrow(() -> new IllegalArgumentException("User have to be central IDP user"));
		LOG.debug("Adding Policy Document id={} for user id={}", policyAgreement.policyDocumentId.id, userId.id);
		policyDocumentDAO.addUserPolicyAgreement(userId, policyAgreement);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public void addUserPolicyAgreement(String siteId, FenixUserId userId, PolicyAgreement policyAgreement) {
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
