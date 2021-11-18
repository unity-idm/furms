/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.notification;

import io.imunity.furms.domain.applications.ProjectApplicationEvent;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.InvitationEvent;
import io.imunity.furms.domain.notification.UserApplicationNotificationRequestEvent;
import io.imunity.furms.domain.notification.UserInvitationNotificationRequestEvent;
import io.imunity.furms.domain.notification.UserPolicyNotificationRequestEvent;
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
import io.imunity.furms.spi.notifications.EmailNotificationDAO;
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
public class NotificationService {

	private final EmailNotificationDAO emailNotificationDAO;
	private final ApplicationEventPublisher publisher;
	private final PolicyDocumentDAO policyDocumentDAO;
	private final PolicyDocumentRepository policyDocumentRepository;
	private final UserOperationRepository userOperationRepository;
	private final SiteGroupDAO siteGroupDAO;

	NotificationService(EmailNotificationDAO emailNotificationDAO, ApplicationEventPublisher publisher, PolicyDocumentDAO policyDocumentDAO, PolicyDocumentRepository policyDocumentRepository, UserOperationRepository userOperationRepository, SiteGroupDAO siteGroupDAO) {
		this.emailNotificationDAO = emailNotificationDAO;
		this.publisher = publisher;
		this.policyDocumentDAO = policyDocumentDAO;
		this.policyDocumentRepository = policyDocumentRepository;
		this.userOperationRepository = userOperationRepository;
		this.siteGroupDAO = siteGroupDAO;
	}

	@EventListener
	void onUserSiteAccessGrantedEvent(UserSiteAccessGrantedEvent event){
		publisher.publishEvent(new UserPolicyNotificationRequestEvent(event.fenixUserId));
	}

	@EventListener
	void onUserAcceptedPolicyEvent(UserAcceptedPolicyEvent event){
		publisher.publishEvent(new UserPolicyNotificationRequestEvent(event.userId));
	}

	@EventListener
	void onUserSiteAccessRevokedEvent(UserSiteAccessRevokedEvent event){
		publisher.publishEvent(new UserPolicyNotificationRequestEvent(event.fenixUserId));
	}

	@EventListener
	void onUserGrantRemovedEvent(UserGrantRemovedEvent event){
		publisher.publishEvent(new UserPolicyNotificationRequestEvent(event.grantAccess.fenixUserId));
	}

	@EventListener
	void onProjectApplicationEvent(ProjectApplicationEvent event){
		UserApplicationNotificationRequestEvent notificationRequestEvent = event::isTargetedAt;
		publisher.publishEvent(notificationRequestEvent);
	}

	@EventListener
	void onInvitationEvent(InvitationEvent event){
		publisher.publishEvent(new UserInvitationNotificationRequestEvent(event.getEmail()));
	}

	public void notifyUser(PersistentId id, PolicyDocument policyDocument) {
		emailNotificationDAO.notifyUser(id, policyDocument);
	}

	public void notifyUserAboutNewRole(PersistentId id, Role role) {
		emailNotificationDAO.notifyUserAboutNewRole(id, role);
	}

	public void notifyAdminAboutRoleAcceptance(PersistentId id, Role role, String acceptanceUserEmail) {
		emailNotificationDAO.notifyAdminAboutRoleAcceptance(id, role, acceptanceUserEmail);
	}

	public void notifyAdminAboutApplicationRequest(PersistentId id, String projectId, String projectName, String applicationUserEmail) {
		emailNotificationDAO.notifyAdminAboutApplicationRequest(id, projectId, projectName, applicationUserEmail);
	}

	public void notifyUserAboutApplicationAcceptance(PersistentId id, String projectName) {
		emailNotificationDAO.notifyUserAboutApplicationAcceptance(id, projectName);
	}

	public void notifyUserAboutApplicationRejection(PersistentId id, String projectName) {
		emailNotificationDAO.notifyUserAboutApplicationRejection(id, projectName);
	}

	public void notifyAdminAboutRoleRejection(PersistentId id, Role role, String rejectionUserEmail) {
		emailNotificationDAO.notifyAdminAboutRoleRejection(id, role, rejectionUserEmail);
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
				emailNotificationDAO.notifyAboutChangedPolicy(user.id.get(), policyDocument.name);
				publisher.publishEvent(new UserPolicyNotificationRequestEvent(user.fenixUserId.get()));
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
				emailNotificationDAO.notifyAboutNotAcceptedPolicy(userId, policyDocumentExtended.name)
			);
		publisher.publishEvent(new UserPolicyNotificationRequestEvent(userId));
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
				emailNotificationDAO.notifySiteUserAboutPolicyAssignmentChange(fenixUserId, policy.name);
				publisher.publishEvent(new UserPolicyNotificationRequestEvent(fenixUserId));
			});
	}

	private boolean isCurrentRevisionAccepted(PolicyDocument sitePolicy, PolicyAcceptance acceptance) {
		return acceptance.policyDocumentId.equals(sitePolicy.id)
			&& acceptance.policyDocumentRevision == sitePolicy.revision;
	}
}
