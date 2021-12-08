/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.notification.UserPoliciesListChangedEvent;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserAcceptedPolicyEvent;
import io.imunity.furms.domain.resource_access.UserGrantRemovedEvent;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_site_access.UserSiteAccessGrantedEvent;
import io.imunity.furms.domain.user_site_access.UserSiteAccessRevokedEvent;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.notifications.EmailNotificationSender;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteGroupDAO;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PolicyNotificationService {

	private final EmailNotificationSender emailNotificationSender;
	private final ApplicationEventPublisher publisher;
	private final PolicyDocumentDAO policyDocumentDAO;
	private final PolicyDocumentRepository policyDocumentRepository;
	private final UserOperationRepository userOperationRepository;
	private final SiteGroupDAO siteGroupDAO;

	PolicyNotificationService(EmailNotificationSender emailNotificationSender, ApplicationEventPublisher publisher,
	                          PolicyDocumentDAO policyDocumentDAO, PolicyDocumentRepository policyDocumentRepository,
	                          UserOperationRepository userOperationRepository, SiteGroupDAO siteGroupDAO) {
		this.emailNotificationSender = emailNotificationSender;
		this.publisher = publisher;
		this.policyDocumentDAO = policyDocumentDAO;
		this.policyDocumentRepository = policyDocumentRepository;
		this.userOperationRepository = userOperationRepository;
		this.siteGroupDAO = siteGroupDAO;
	}

	@EventListener
	void onUserSiteAccessGrantedEvent(UserSiteAccessGrantedEvent event){
		publisher.publishEvent(new UserPoliciesListChangedEvent(event.fenixUserId));
	}

	@EventListener
	void onUserAcceptedPolicyEvent(UserAcceptedPolicyEvent event){
		publisher.publishEvent(new UserPoliciesListChangedEvent(event.userId));
	}

	@EventListener
	void onUserSiteAccessRevokedEvent(UserSiteAccessRevokedEvent event){
		publisher.publishEvent(new UserPoliciesListChangedEvent(event.fenixUserId));
	}

	@EventListener
	void onUserGrantRemovedEvent(UserGrantRemovedEvent event){
		publisher.publishEvent(new UserPoliciesListChangedEvent(event.grantAccess.fenixUserId));
	}

	public void notifyUserAboutNewPolicy(PersistentId id, PolicyDocument policyDocument) {
		emailNotificationSender.notifyUserAboutNewPolicy(id, policyDocument);
	}

	public void notifyAboutChangedPolicy(PolicyDocument policyDocument) {
		policyDocumentDAO.getUserPolicyAcceptances(policyDocument.siteId).stream()
			.filter(userPolicyAcceptances -> userPolicyAcceptances.policyAcceptances.stream()
				.anyMatch(policyAgreement -> policyAgreement.policyDocumentId.equals(policyDocument.id))
			)
			.map(userPolicyAcceptances -> userPolicyAcceptances.user)
			.filter(userPolicyAcceptances -> userPolicyAcceptances.fenixUserId.isPresent())
			.filter(user -> user.id.isPresent())
			.forEach(user -> {
				emailNotificationSender.notifyAboutChangedPolicy(user.id.get(), policyDocument.name);
				publisher.publishEvent(new UserPoliciesListChangedEvent(user.fenixUserId.get()));
			});
	}

	public void notifyAboutAllNotAcceptedPolicies(String siteId, FenixUserId userId, String grantId) {
		Map<PolicyId, PolicyAcceptance> policyAcceptanceMap = policyDocumentDAO.getPolicyAcceptances(userId).stream()
			.collect(Collectors.toMap(x -> x.policyDocumentId, Function.identity()));

		Optional<PolicyDocument> servicePolicy = policyDocumentRepository.findByUserGrantId(grantId);
		Optional<PolicyDocument> sitePolicy = policyDocumentRepository.findSitePolicy(siteId);

		policyDocumentRepository.findAllByUserId(userId, (id, revision) -> LocalDateTime.MAX).stream()
			.filter(policyDocument ->
				Optional.ofNullable(policyAcceptanceMap.get(policyDocument.id))
					.filter(policyAcceptance -> policyDocument.revision == policyAcceptance.policyDocumentRevision).isEmpty()
			)
			.filter(policyDocument ->
				servicePolicy.filter(policy -> policy.id.equals(policyDocument.id)).isPresent() ||
					sitePolicy.filter(policy -> policy.id.equals(policyDocument.id)).isPresent())
			.forEach(policyDocumentExtended ->
				emailNotificationSender.notifyAboutNotAcceptedPolicy(userId, policyDocumentExtended.name)
			);
		publisher.publishEvent(new UserPoliciesListChangedEvent(userId));
	}

	public void notifyAllUsersAboutPolicyAssignmentChange(SiteId siteId) {
		final PolicyDocument sitePolicy = policyDocumentRepository.findSitePolicy(siteId.id)
			.orElseThrow(() -> new IllegalArgumentException("Site hasn't policy document attached."));

		notifyAllSiteUsersAboutPolicyAssignmentChange(siteId, sitePolicy);
	}

	public void notifyAllUsersAboutPolicyAssignmentChange(InfraService infraService) {
		final PolicyDocument servicePolicy = policyDocumentRepository.findById(infraService.policyId)
			.orElseThrow(() -> new IllegalArgumentException("Service hasn't policy document attached."));

		notifyAllSiteUsersAboutPolicyAssignmentChange(new SiteId(infraService.siteId), servicePolicy);
	}

	private void notifyAllSiteUsersAboutPolicyAssignmentChange(SiteId siteId, PolicyDocument policy) {
		Stream.concat(
			userOperationRepository.findAllUserAdditionsBySiteId(siteId.id).stream()
				.map(addition -> addition.userId)
				.map(FenixUserId::new),
			siteGroupDAO.getAllSiteUsers(siteId.id, Set.of(Role.SITE_ADMIN, Role.SITE_SUPPORT)).stream()
				.filter(user -> user.fenixUserId.isPresent())
				.map(user -> user.fenixUserId.get())
		)
			.distinct()
			.filter(fenixUserId -> policyDocumentDAO.getPolicyAcceptances(fenixUserId).stream()
				.noneMatch(acceptance -> isCurrentRevisionAccepted(policy, acceptance)))
			.forEach(fenixUserId -> {
				emailNotificationSender.notifySiteUserAboutPolicyAssignmentChange(fenixUserId, policy.name);
				publisher.publishEvent(new UserPoliciesListChangedEvent(fenixUserId));
			});
	}

	private boolean isCurrentRevisionAccepted(PolicyDocument sitePolicy, PolicyAcceptance acceptance) {
		return acceptance.policyDocumentId.equals(sitePolicy.id)
			&& acceptance.policyDocumentRevision == sitePolicy.revision;
	}
}
