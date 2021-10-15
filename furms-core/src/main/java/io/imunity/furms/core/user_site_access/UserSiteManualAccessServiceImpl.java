/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_site_access;

import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserAcceptedPolicyEvent;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrantAddedEvent;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.sites.SiteUser;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.user_site_access.UserSiteAccessStatus;
import io.imunity.furms.domain.user_site_access.UsersSitesAccesses;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.user_site_access.UserSiteAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
class UserSiteManualAccessServiceImpl {
	private static final Logger LOG = LoggerFactory.getLogger(UserSiteManualAccessServiceImpl.class);

	private final UserSiteAccessRepository userSiteAccessRepository;

	private final UserPoliciesDocumentsServiceHelper policyDocumentService;
	private final ResourceAccessRepository resourceAccessRepository;
	private final SiteRepository siteRepository;

	private final PolicyDocumentRepository policyDocumentRepository;
	private final PolicyDocumentDAO policyDocumentDAO;

	private final UserOperationService userOperationService;
	private final UserOperationRepository userRepository;


	@Transactional
	public void addAccess(String siteId, String projectId, FenixUserId userId){
		if(!userSiteAccessRepository.exists(siteId, projectId, userId)) {
			userSiteAccessRepository.add(siteId, projectId, userId);
			if (hasUserSitePolicyAcceptanceOrSiteHasntPolicy(siteId, userId))
				userOperationService.createUserAdditions(new SiteId(siteId), projectId, policyDocumentService.getUserPolicyAcceptancesWithServicePolicies(siteId, userId));
		}
	}

	@Transactional
	public void removeAccess(String siteId, String projectId, FenixUserId userId){
		if(userSiteAccessRepository.exists(siteId, projectId, userId)) {
			userSiteAccessRepository.remove(siteId, projectId, userId);
			userOperationService.createUserRemovals(projectId, userId);
		}
	}

	public UsersSitesAccesses isAccessGrant(String projectId){
		Map<String, Set<FenixUserId>> map;
//		userRepository.isUserInstalledOnSite(userId, siteId) ? UserSiteAccessStatus.;
//
//		Set<SiteUser> userSitesInstallations = userOperationService.findUserSitesInstallations();
//		userSitesInstallations.
//
//		userSiteAccessRepository.findAllUserProjectIds()
		return null;
	}

	@EventListener
	void onUserPolicyAcceptance(UserAcceptedPolicyEvent event) {
		FenixUserId userId = event.userId;
		PolicyAcceptance policyAcceptance = event.policyAcceptance;
		PolicyId policyDocumentId = policyAcceptance.policyDocumentId;
		LOG.debug("Adding Policy Document id={} for user id={}", policyDocumentId.id, userId.id);

		PolicyDocument policyDocument = policyDocumentRepository.findById(policyDocumentId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Policy id %s doesn't exist", policyDocumentId)));

		Site site = siteRepository.findById(policyDocument.siteId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Site id %s doesn't exist", policyDocument.siteId)));

		if(site.getPolicyId().equals(policyDocumentId)) {
			userSiteAccessRepository.findAllUserProjectIds(site.getId(), userId).stream()
				.filter(projectId -> userRepository.findAdditionStatus(site.getId(), projectId, userId).isEmpty())
				.forEach(projectId ->
					userOperationService.createUserAdditions(
						new SiteId(site.getId(), site.getExternalId()),
						projectId,
						policyDocumentService.getUserPolicyAcceptancesWithServicePolicies(site.getId(), userId)
					)
				);
		}
	}

	@EventListener
	void onUserGrantAccess(UserGrantAddedEvent event) {
		GrantAccess grantAccess = event.grantAccess;
		Optional<UserStatus> userAdditionStatus = userRepository.findAdditionStatus(grantAccess.siteId.id, grantAccess.projectId, grantAccess.fenixUserId);
		if (
			userSiteAccessRepository.exists(grantAccess.siteId.id, grantAccess.projectId, grantAccess.fenixUserId) &&
			userAdditionStatus.isEmpty() &&
			hasUserSitePolicyAcceptanceOrSiteHasntPolicy(grantAccess.siteId.id, grantAccess.fenixUserId)
		) {
			userOperationService.createUserAdditions(
				grantAccess.siteId,
				grantAccess.projectId,
				policyDocumentService.getUserPolicyAcceptancesWithServicePolicies(grantAccess.siteId.id, grantAccess.fenixUserId)
			);
		}
	}

	private boolean hasUserSitePolicyAcceptanceOrSiteHasntPolicy(String siteId, FenixUserId userId) {
		return policyDocumentService.hasUserSitePolicyAcceptance(userId, siteId)
			|| !policyDocumentService.hasSitePolicy(siteId);
	}
}
