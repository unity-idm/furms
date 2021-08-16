/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceAtSite;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentCreateEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyDocumentRemovedEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentUpdatedEvent;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.notifications.NotificationDAO;
import io.imunity.furms.spi.notifications.NotificationDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.POLICY_ACCEPTANCE_MAINTENANCE;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus.ACCEPTED;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
class PolicyDocumentServiceImpl implements PolicyDocumentService {
	private static final Logger LOG = LoggerFactory.getLogger(PolicyDocumentServiceImpl.class);

	private final AuthzService authzService;
	private final PolicyDocumentRepository policyDocumentRepository;
	private final PolicyDocumentValidator validator;
	private final PolicyDocumentDAO policyDocumentDAO;
	private final NotificationDAO notificationDAO;
	private final ApplicationEventPublisher publisher;

	PolicyDocumentServiceImpl(PolicyDocumentRepository policyDocumentRepository,
	                          PolicyDocumentValidator validator,
	                          PolicyDocumentDAO policyDocumentDAO,
	                          AuthzService authzService,
	                          NotificationDAO notificationDAO,
	                          ApplicationEventPublisher publisher) {
		this.policyDocumentRepository = policyDocumentRepository;
		this.validator = validator;
		this.policyDocumentDAO = policyDocumentDAO;
		this.authzService = authzService;
		this.notificationDAO = notificationDAO;
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
	public Set<FURMSUser> findAllUsersWithoutCurrentRevisionPolicyAcceptance(String siteId, PolicyId policyId) {
		LOG.debug("Getting all users who not accepted Policy Document {}", policyId.id);

		PolicyDocument policyDocument = policyDocumentRepository.findById(policyId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Policy Document %s doesn't exist", policyId.id)));

		return policyDocumentDAO.getUserPolicyAcceptances(siteId).stream()
			.filter(userAcceptance -> userAcceptance.policyAcceptances.stream()
				.noneMatch(policyAcceptance -> policyAcceptance.policyDocumentId.equals(policyDocument.id)
						&& policyAcceptance.policyDocumentRevision == policyDocument.revision)
			)
			.filter(userAcceptance -> userAcceptance.user.fenixUserId.isPresent())
			.map(userAcceptance -> userAcceptance.user)
			.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public Set<PolicyDocumentExtended> findAllByCurrentUser() {
		FenixUserId userId = authzService.getCurrentAuthNUser().fenixUserId
			.orElseThrow(() -> new IllegalArgumentException("User have to be central IDP user"));

		LOG.debug("Getting all Policy Document for user id={}", userId.id);

		Map<PolicyId, PolicyAcceptance> collect = policyDocumentDAO.getyPolicyAcceptances(userId).stream()
			.collect(toMap(policyAcceptance -> policyAcceptance.policyDocumentId, identity()));

		return policyDocumentRepository.findAllByUserId(userId, (policyId, revision) ->
			Optional.ofNullable(collect.get(policyId))
				.filter(policyAcceptance -> policyAcceptance.policyDocumentRevision == revision)
				.map(policyAcceptance -> policyAcceptance.decisionTs)
				.map(decisionTs -> LocalDateTime.ofInstant(decisionTs, ZoneOffset.UTC.normalized()))
				.orElse(null)
		);
	}

	@Override
	@FurmsAuthorize(capability = POLICY_ACCEPTANCE_MAINTENANCE, resourceType = APP_LEVEL)
	public Set<PolicyAcceptanceAtSite> findSitePolicyAcceptancesByUserId(PersistentId userId) {
		final Set<PolicyDocument> userPolicies = policyDocumentRepository.findAllSitePoliciesByUserId(userId);
		return findPolicyAcceptancesByUserIdFilterByPolicies(userId, userPolicies);
	}

	@Override
	@FurmsAuthorize(capability = POLICY_ACCEPTANCE_MAINTENANCE, resourceType = APP_LEVEL)
	public Set<PolicyAcceptanceAtSite> findServicesPolicyAcceptancesByUserId(PersistentId userId) {
		final Set<PolicyDocument> userPolicies = policyDocumentRepository.findAllServicePoliciesByUserId(userId);
		return findPolicyAcceptancesByUserIdFilterByPolicies(userId, userPolicies);
	}

	private Set<PolicyAcceptanceAtSite> findPolicyAcceptancesByUserIdFilterByPolicies(PersistentId userId,
	                                                                                  Set<PolicyDocument> userPolicies) {
		return findPolicyAcceptancesByUserId(userId).stream()
				.filter(policyAcceptance -> policyAcceptance.acceptanceStatus == ACCEPTED)
				.map(policyAcceptance -> userPolicies.stream()
								.filter(userPolicy -> isPolicyRelatedToAcceptance(userPolicy, policyAcceptance))
								.findFirst()
								.map(policyDocument -> new PolicyAcceptanceAtSite(policyAcceptance, policyDocument))
								.orElse(null))
				.filter(Objects::nonNull)
				.collect(toSet());
	}

	private boolean isPolicyRelatedToAcceptance(PolicyDocument userPolicy, PolicyAcceptance policyAcceptance) {
		return userPolicy.id.equals(policyAcceptance.policyDocumentId)
				&& userPolicy.revision == policyAcceptance.policyDocumentRevision;
	}

	private Set<PolicyAcceptance> findPolicyAcceptancesByUserId(PersistentId userId) {
		final FenixUserId fenixUserId = authzService.getCurrentAuthNUser().fenixUserId
				.orElseThrow(() -> new IllegalArgumentException("User have to be central IDP user"));

		LOG.debug("Getting all Policy Document for user id={}", userId.id);
		return policyDocumentDAO.getyPolicyAcceptances(fenixUserId);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public void addCurrentUserPolicyAcceptance(PolicyAcceptance policyAcceptance) {
		FenixUserId userId = authzService.getCurrentAuthNUser().fenixUserId
			.orElseThrow(() -> new IllegalArgumentException("User have to be central IDP user"));
		LOG.debug("Adding Policy Document id={} for user id={}", policyAcceptance.policyDocumentId.id, userId.id);
		policyDocumentDAO.addUserPolicyAcceptance(userId, policyAcceptance);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public void addUserPolicyAcceptance(String siteId, FenixUserId userId, PolicyAcceptance policyAcceptance) {
		LOG.debug("Adding Policy Document id={} for user id={}", policyAcceptance.policyDocumentId.id, userId.id);
		policyDocumentDAO.addUserPolicyAcceptance(userId, policyAcceptance);
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
		notificationDAO.notifyAboutChangedPolicy(policyDocument);
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
