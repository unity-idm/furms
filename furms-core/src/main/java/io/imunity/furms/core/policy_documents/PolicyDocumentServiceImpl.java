/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.policy_documents.AssignedPolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentCreateEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyDocumentRemovedEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentUpdatedEvent;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPendingPoliciesChangedEvent;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.spi.notifications.NotificationDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_POLICY_ACCEPTANCE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_POLICY_ACCEPTANCE_WRITE;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static io.imunity.furms.utils.StreamUtils.distinctBy;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
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
	private final UserOperationService userOperationService;
	private final SiteAgentPolicyDocumentService siteAgentPolicyDocumentService;
	private final ResourceAccessRepository resourceAccessRepository;
	private final SiteRepository siteRepository;
	private final UserService userService;
	private final ApplicationEventPublisher publisher;

	PolicyDocumentServiceImpl(AuthzService authzService, PolicyDocumentRepository policyDocumentRepository,
	                          PolicyDocumentValidator validator, PolicyDocumentDAO policyDocumentDAO,
	                          NotificationDAO notificationDAO, UserOperationService userOperationService,
	                          SiteAgentPolicyDocumentService siteAgentPolicyDocumentService,
	                          ResourceAccessRepository resourceAccessRepository, SiteRepository siteRepository,
	                          UserService userService, ApplicationEventPublisher publisher) {
		this.authzService = authzService;
		this.policyDocumentRepository = policyDocumentRepository;
		this.validator = validator;
		this.policyDocumentDAO = policyDocumentDAO;
		this.notificationDAO = notificationDAO;
		this.userOperationService = userOperationService;
		this.siteAgentPolicyDocumentService = siteAgentPolicyDocumentService;
		this.resourceAccessRepository = resourceAccessRepository;
		this.siteRepository = siteRepository;
		this.userService = userService;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Optional<PolicyDocument> findById(String siteId, PolicyId id) {
		LOG.debug("Getting Policy Document with id={}", id);
		return policyDocumentRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public Set<PolicyDocument> findAll() {
		LOG.debug("Getting all Policy Documents");
		return policyDocumentRepository.findAll();
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Map<FenixUserId, Set<PolicyDocument>> findAllUsersPolicies(String siteId) {
		LOG.debug("Getting all user's Policy Document for site id={}", siteId);
		return policyDocumentRepository.findAllUsersPolicies(siteId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<PolicyDocument> findAllBySiteId(String siteId) {
		LOG.debug("Getting all Policy Document for site id={}", siteId);
		return policyDocumentRepository.findAllBySiteId(siteId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_POLICY_ACCEPTANCE_READ, resourceType = SITE, id = "siteId")
	public Set<UserPolicyAcceptances> findAllUsersPolicyAcceptances(String siteId) {
		return policyDocumentDAO.getUserPolicyAcceptances(siteId);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public Set<PolicyDocumentExtended> findAllByCurrentUser() {
		Optional<FenixUserId> userId = authzService.getCurrentAuthNUser().fenixUserId;
		if(userId.isEmpty())
			return Set.of();

		LOG.debug("Getting all Policy Document for user id={}", userId.get().id);

		Map<PolicyId, PolicyAcceptance> collect = policyDocumentDAO.getPolicyAcceptances(userId.get()).stream()
			.collect(toMap(policyAgreement -> policyAgreement.policyDocumentId, identity()));

		return policyDocumentRepository.findAllByUserId(userId.get(), (policyId, revision) ->
			Optional.ofNullable(collect.get(policyId))
				.filter(policyAgreement -> policyAgreement.policyDocumentRevision == revision)
				.map(policyAgreement -> policyAgreement.decisionTs)
				.map(policyAgreement -> LocalDateTime.ofInstant(policyAgreement, ZoneOffset.UTC.normalized()))
				.orElse(null)
		);
	}

	@Override
	@FurmsAuthorize(capability = SITE_POLICY_ACCEPTANCE_READ, resourceType = SITE, id = "siteId")
	public void resendPolicyInfo(String siteId, PersistentId persistentId, PolicyId policyId) {
		PolicyDocument policyDocument = policyDocumentRepository.findById(policyId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Policy id %s doesn't exist", policyId)));
		notificationDAO.notifyUser(persistentId, policyDocument);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public void addCurrentUserPolicyAcceptance(PolicyAcceptance policyAcceptance) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		addUserPolicyAcceptance(currentAuthNUser, policyAcceptance);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_POLICY_ACCEPTANCE_WRITE, resourceType = SITE, id = "siteId")
	public void addUserPolicyAcceptance(String siteId, FenixUserId userId, PolicyAcceptance policyAcceptance) {
		FURMSUser user = userService.findByFenixUserId(userId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Fenix user id %s doesn't exist", userId)));
		Optional<PolicyDocument> policyDocument = policyDocumentRepository.findById(policyAcceptance.policyDocumentId);
		assertPolicyBelongsToSite(siteId, policyDocument);
		addUserPolicyAcceptance(user, policyAcceptance);
	}

	private void addUserPolicyAcceptance(FURMSUser user, PolicyAcceptance policyAcceptance) {
		FenixUserId userId = user.fenixUserId
			.orElseThrow(() -> new UserWithoutFenixIdValidationError("User not logged via Fenix Central IdP"));
		PolicyId policyDocumentId = policyAcceptance.policyDocumentId;
		LOG.debug("Adding Policy Document id={} for user id={}", policyDocumentId.id, userId.id);

		PolicyDocument policyDocument = policyDocumentRepository.findById(policyDocumentId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Policy id %s doesn't exist", policyDocumentId)));

		if(isPolicyRevisionNotSet(policyAcceptance))
			policyAcceptance = getPolicyWithCurrentRevision(policyAcceptance, policyDocumentId, policyDocument);

		Site site = siteRepository.findById(policyDocument.siteId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Site id %s doesn't exist", policyDocument.siteId)));

		Set<PolicyAcceptance> policyAcceptances = policyDocumentDAO.getPolicyAcceptances(userId);

		Set<AssignedPolicyDocument> allAssignPoliciesBySiteId = policyDocumentRepository.findAllAssignPoliciesBySiteId(site.getId());
		Optional<PolicyDocument> sitePolicyDocument = policyDocumentRepository.findById(site.getPolicyId());

		policyDocumentDAO.addUserPolicyAcceptance(userId, policyAcceptance);
		if(policyDocument.id.equals(site.getPolicyId()) && policyAcceptances.stream()
			.noneMatch(acceptance ->
				acceptance.policyDocumentId.equals(policyDocument.id) && acceptance.policyDocumentRevision == policyDocument.revision)
		) {
			policyAcceptances.add(policyAcceptance);
			resourceAccessRepository.findWaitingGrantAccesses(userId, policyDocument.siteId)
				.stream()
				.filter(distinctBy(grantAccess -> grantAccess.projectId))
				.forEach(grantAccess ->
					userOperationService.createUserAdditions(
						grantAccess.siteId,
						grantAccess.projectId,
						new UserPolicyAcceptancesWithServicePolicies(user, policyAcceptances, sitePolicyDocument, allAssignPoliciesBySiteId)
					)
				);
		}
		else {
			policyAcceptances.add(policyAcceptance);
			siteAgentPolicyDocumentService.updateUsersPolicyAcceptances(
				site.getExternalId(), new UserPolicyAcceptancesWithServicePolicies(user, policyAcceptances, sitePolicyDocument, allAssignPoliciesBySiteId)
			);
		}
		publisher.publishEvent(new UserPendingPoliciesChangedEvent(userId));
	}

	private PolicyAcceptance getPolicyWithCurrentRevision(PolicyAcceptance policyAcceptance, PolicyId policyDocumentId, PolicyDocument policyDocument) {
		return PolicyAcceptance.builder()
			.policyDocumentId(policyDocumentId)
			.policyDocumentRevision(policyDocument.revision)
			.acceptanceStatus(policyAcceptance.acceptanceStatus)
			.decisionTs(policyAcceptance.decisionTs)
			.build();
	}

	private boolean isPolicyRevisionNotSet(PolicyAcceptance policyAcceptance) {
		return policyAcceptance.policyDocumentRevision < 1;
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
	@Transactional
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "policyDocument.siteId")
	public void updateWithRevision(PolicyDocument policyDocument) {
		LOG.debug("Updating Policy Document for site id={}", policyDocument.siteId);
		validator.validateUpdate(policyDocument);
		PolicyId policyId = policyDocumentRepository.update(policyDocument, true);

		Site site = siteRepository.findById(policyDocument.siteId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Site id %s doesn't exist", policyDocument.siteId)));
		if(policyId.equals(site.getPolicyId()))
			siteAgentPolicyDocumentService.updatePolicyDocument(site.getExternalId(), policyDocument);

		Map<PolicyId, Set<String>> policyIdToRelatedServiceIds = policyDocumentRepository.findAllAssignPoliciesBySiteId(policyDocument.siteId).stream()
			.collect(groupingBy(policy -> policy.id, mapping(policy -> policy.serviceId, toSet())));
		Optional.ofNullable(policyIdToRelatedServiceIds.get(policyDocument.id))
			.orElseGet(Set::of)
			.forEach(serviceId -> siteAgentPolicyDocumentService.updatePolicyDocument(site.getExternalId(), policyDocument, serviceId));

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

	private void assertPolicyBelongsToSite(String siteId, Optional<PolicyDocument> policyDocument) {
		if (policyDocument.isEmpty() || !policyDocument.get().siteId.equals(siteId)) {
			throw new IllegalArgumentException("Policy doesn't belongs to Site.");
		}
	}
}
