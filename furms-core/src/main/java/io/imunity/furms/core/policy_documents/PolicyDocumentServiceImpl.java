/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.api.validation.exceptions.AssignedPolicyRemovingException;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.policy_documents.AssignedPolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentCreatedEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyDocumentRemovedEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentUpdatedEvent;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserAcceptedPolicyEvent;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
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
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_POLICY_ACCEPTANCE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_POLICY_ACCEPTANCE_WRITE;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
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
	private final PolicyNotificationService policyNotificationService;
	private final SiteAgentPolicyDocumentService siteAgentPolicyDocumentService;
	private final SiteRepository siteRepository;
	private final UsersDAO usersDAO;
	private final UserOperationRepository userRepository;
	private final ApplicationEventPublisher publisher;

	PolicyDocumentServiceImpl(AuthzService authzService, PolicyDocumentRepository policyDocumentRepository,
	                          PolicyDocumentValidator validator, PolicyDocumentDAO policyDocumentDAO,
	                          PolicyNotificationService policyNotificationService,
	                          SiteAgentPolicyDocumentService siteAgentPolicyDocumentService,
	                          SiteRepository siteRepository,
	                          UserOperationRepository userRepository,
	                          UsersDAO usersDAO, ApplicationEventPublisher publisher) {
		this.authzService = authzService;
		this.policyDocumentRepository = policyDocumentRepository;
		this.validator = validator;
		this.policyDocumentDAO = policyDocumentDAO;
		this.policyNotificationService = policyNotificationService;
		this.siteAgentPolicyDocumentService = siteAgentPolicyDocumentService;
		this.siteRepository = siteRepository;
		this.usersDAO = usersDAO;
		this.userRepository = userRepository;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Optional<PolicyDocument> findById(SiteId siteId, PolicyId id) {
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
	public Map<FenixUserId, Set<PolicyDocument>> findAllUsersPolicies(SiteId siteId) {
		LOG.debug("Getting all user's Policy Document for site id={}", siteId);
		return policyDocumentRepository.findAllUsersPolicies(siteId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<PolicyDocument> findAllBySiteId(SiteId siteId) {
		LOG.debug("Getting all Policy Document for site id={}", siteId);
		return policyDocumentRepository.findAllBySiteId(siteId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_POLICY_ACCEPTANCE_READ, resourceType = SITE, id = "siteId")
	public Set<UserPolicyAcceptances> findAllUsersPolicyAcceptances(SiteId siteId) {
		return policyDocumentDAO.getUserPolicyAcceptances(siteId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_POLICY_ACCEPTANCE_READ, resourceType = SITE, id = "siteId")
	public Set<UserPolicyAcceptances> findAllUsersPolicyAcceptances(PolicyId policyId, SiteId siteId) {
		Set<FenixUserId> allPolicyUsers = policyDocumentRepository.findAllPolicyUsers(siteId, policyId);
		return policyDocumentDAO.getUserPolicyAcceptances(siteId)
			.stream()
			.filter(x -> allPolicyUsers.contains(x.user.fenixUserId.get()))
			.collect(Collectors.toSet());
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED)
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
	public void resendPolicyInfo(SiteId siteId, PersistentId persistentId, PolicyId policyId) {
		PolicyDocument policyDocument = policyDocumentRepository.findById(policyId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Policy id %s doesn't exist", policyId)));
		policyNotificationService.notifyUserAboutNewPolicy(persistentId, policyDocument);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = AUTHENTICATED)
	public void addCurrentUserPolicyAcceptance(PolicyAcceptance policyAcceptance) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		addUserPolicyAcceptance(currentAuthNUser, policyAcceptance);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_POLICY_ACCEPTANCE_WRITE, resourceType = SITE, id = "siteId")
	public void addUserPolicyAcceptance(SiteId siteId, FenixUserId userId, PolicyAcceptance policyAcceptance) {
		FURMSUser user = usersDAO.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Fenix user id %s doesn't exist", userId)));
		Optional<PolicyDocument> policyDocument = policyDocumentRepository.findById(policyAcceptance.policyDocumentId);
		assertPolicyBelongsToSite(siteId, policyDocument);
		addUserPolicyAcceptance(user, policyAcceptance);
	}

	private void addUserPolicyAcceptance(FURMSUser user, PolicyAcceptance policyAcceptance) {
		FenixUserId userId = user.fenixUserId
			.orElseThrow(() -> new UserWithoutFenixIdValidationError("User not logged via Fenix Central IdP"));

		PolicyId policyDocumentId = policyAcceptance.policyDocumentId;
		PolicyDocument policyDocument = policyDocumentRepository.findById(policyDocumentId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Policy id %s doesn't exist", policyDocumentId)));

		if(isPolicyRevisionNotSet(policyAcceptance))
			policyAcceptance = getPolicyWithCurrentRevision(policyAcceptance, policyDocumentId, policyDocument);

		policyDocumentDAO.addUserPolicyAcceptance(userId, policyAcceptance);
		updateUsersPolicyAcceptance(user, userId, policyDocument);

		publisher.publishEvent(new UserAcceptedPolicyEvent(userId, policyAcceptance));
		LOG.info("Added Policy Document id={} for user id={}", policyDocumentId.id, userId.id);
	}

	private void updateUsersPolicyAcceptance(FURMSUser user, FenixUserId userId, PolicyDocument policyDocument) {
		if(userRepository.isUserInstalledOnSite(userId, policyDocument.siteId)) {
			Site site = siteRepository.findById(policyDocument.siteId)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Site id %s doesn't exist", policyDocument.siteId)));
			Set<PolicyAcceptance> policyAcceptances = policyDocumentDAO.getPolicyAcceptances(userId);

			Set<AssignedPolicyDocument> allAssignPoliciesBySiteId = policyDocumentRepository.findAllAssignPoliciesBySiteId(site.getId());
			Optional<PolicyDocument> sitePolicyDocument = Optional.ofNullable(site.getPolicyId())
				.filter(policyId -> policyId.id != null)
				.flatMap(policyDocumentRepository::findById);

			siteAgentPolicyDocumentService.updateUsersPolicyAcceptances(
				site.getExternalId(), new UserPolicyAcceptancesWithServicePolicies(user, policyAcceptances, sitePolicyDocument, allAssignPoliciesBySiteId)
			);
		}
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
		PolicyDocument created = policyDocumentRepository.findById(policyId).get();
		publisher.publishEvent(new PolicyDocumentCreatedEvent(created));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "policyDocument.siteId")
	public void update(PolicyDocument policyDocument) {
		LOG.debug("Updating Policy Document for site id={}", policyDocument.siteId);
		validator.validateUpdate(policyDocument);
		PolicyDocument oldPolicyDocument = policyDocumentRepository.findById(policyDocument.id).get();
		policyDocumentRepository.update(policyDocument, false);
		publisher.publishEvent(new PolicyDocumentUpdatedEvent(oldPolicyDocument, policyDocument));
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "policyDocument.siteId")
	public void updateWithRevision(PolicyDocument policyDocument) {
		LOG.debug("Updating Policy Document for site id={}", policyDocument.siteId);
		validator.validateUpdate(policyDocument);
		PolicyDocument oldPolicyDocument = policyDocumentRepository.findById(policyDocument.id).get();
		PolicyId policyId = policyDocumentRepository.update(policyDocument, true);
		PolicyDocument updatedPolicyDocument = policyDocumentRepository.findById(policyId).get();

		Site site = siteRepository.findById(policyDocument.siteId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Site id %s doesn't exist", policyDocument.siteId)));
		if(policyId.equals(site.getPolicyId()))
			siteAgentPolicyDocumentService.updatePolicyDocument(site.getExternalId(), updatedPolicyDocument);

		Map<PolicyId, Set<InfraServiceId>> policyIdToRelatedServiceIds =
			policyDocumentRepository.findAllAssignPoliciesBySiteId(policyDocument.siteId).stream()
				.filter(document -> document.serviceId.isPresent())
				.collect(groupingBy(policy -> policy.id, mapping(policy -> policy.serviceId.get(), toSet())));
		Optional.ofNullable(policyIdToRelatedServiceIds.get(policyDocument.id))
			.orElseGet(Set::of)
			.forEach(serviceId -> siteAgentPolicyDocumentService.updatePolicyDocument(site.getExternalId(),
				updatedPolicyDocument, Optional.ofNullable(serviceId)));

		policyNotificationService.notifyAboutChangedPolicy(policyDocument);
		publisher.publishEvent(new PolicyDocumentUpdatedEvent(oldPolicyDocument, updatedPolicyDocument));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "siteId")
	public void delete(SiteId siteId, PolicyId policyId) {
		LOG.debug("Deleting Policy Document {} for site id={}", policyId.id, siteId);
		boolean isAssigned = policyDocumentRepository.findAllAssignPoliciesBySiteId(siteId).stream()
			.anyMatch(policy -> policy.id.equals(policyId));
		if(isAssigned)
			throw new AssignedPolicyRemovingException(String.format("Policy %s removing error. Only not assigned policy can be removed", policyId.id));
		PolicyDocument policyDocument = policyDocumentRepository.findById(policyId).get();
		policyDocumentRepository.deleteById(policyId);
		publisher.publishEvent(new PolicyDocumentRemovedEvent(policyDocument));
	}

	private void assertPolicyBelongsToSite(SiteId siteId, Optional<PolicyDocument> policyDocument) {
		if (policyDocument.isEmpty() || !policyDocument.get().siteId.equals(siteId)) {
			throw new IllegalArgumentException("Policy doesn't belong to Site.");
		}
	}
}
