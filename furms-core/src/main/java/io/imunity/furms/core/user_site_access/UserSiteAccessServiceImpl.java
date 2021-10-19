/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_site_access;

import io.imunity.furms.api.user_site_access.UserSiteAccessService;
import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserAcceptedPolicyEvent;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrantAddedEvent;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.user_site_access.UsersSitesAccesses;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.user_site_access.UserSiteAccessRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
class UserSiteAccessServiceImpl implements UserSiteAccessService {
	private static final Logger LOG = LoggerFactory.getLogger(UserSiteAccessServiceImpl.class);

	private final UserSiteAccessRepository userSiteAccessRepository;
	private final UserPoliciesDocumentsServiceHelper policyDocumentService;
	private final ResourceAccessRepository resourceAccessRepository;
	private final UserOperationService userOperationService;
	private final UserOperationRepository userRepository;
	private final UsersDAO usersDAO;

	UserSiteAccessServiceImpl(UserSiteAccessRepository userSiteAccessRepository,
	                          UserPoliciesDocumentsServiceHelper policyDocumentService,
	                          ResourceAccessRepository resourceAccessRepository,
	                          UserOperationService userOperationService, UserOperationRepository userRepository,
	                          UsersDAO usersDAO) {
		this.userSiteAccessRepository = userSiteAccessRepository;
		this.policyDocumentService = policyDocumentService;
		this.resourceAccessRepository = resourceAccessRepository;
		this.userOperationService = userOperationService;
		this.userRepository = userRepository;
		this.usersDAO = usersDAO;
	}

	@Override
	@Transactional
	public void addAccess(String siteId, String projectId, FenixUserId userId) {
		if(!userSiteAccessRepository.exists(siteId, projectId, userId)) {
			userSiteAccessRepository.add(siteId, projectId, userId);
			if (hasUserSitePolicyAcceptanceOrSiteHasntPolicy(siteId, userId))
				userOperationService.createUserAdditions(new SiteId(siteId), projectId, policyDocumentService.getUserPolicyAcceptancesWithServicePolicies(siteId, userId));
		}
	}

	@Override
	@Transactional
	public void removeAccess(String siteId, String projectId, FenixUserId userId) {
		if(userSiteAccessRepository.exists(siteId, projectId, userId)) {
			userSiteAccessRepository.remove(siteId, projectId, userId);
			userOperationService.createUserRemovals(projectId, userId);
		}
	}

	@Override
	public UsersSitesAccesses getUsersSitesAccesses(String projectId) {
		Map<String, Set<FenixUserId>> allUserGroupedBySiteId = userSiteAccessRepository.findAllUserGroupedBySiteId(projectId);
		return new UsersSitesAccesses(allUserGroupedBySiteId, usersDAO.getAllUsers(), userRepository.findAllUserAdditions(projectId), resourceAccessRepository.findGrantAccessesBy(projectId));
	}

	@EventListener
	void onUserPolicyAcceptance(UserAcceptedPolicyEvent event) {
		FenixUserId userId = event.userId;
		PolicyAcceptance policyAcceptance = event.policyAcceptance;
		PolicyId policyDocumentId = policyAcceptance.policyDocumentId;
		LOG.debug("Adding Policy Document id={} for user id={}", policyDocumentId.id, userId.id);

		PolicyDocument policyDocument = policyDocumentService.findById(policyDocumentId);
		Site site = policyDocumentService.getPolicySite(policyDocument);

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
