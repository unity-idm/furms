/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_site_access;

import io.imunity.furms.api.user_site_access.UserSiteAccessService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.core.utils.InvokeAfterCommitEvent;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserAcceptedPolicyEvent;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationEvent;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.user_site_access.UserSiteAccessGrantedEvent;
import io.imunity.furms.domain.user_site_access.UserSiteAccessRevokedEvent;
import io.imunity.furms.domain.user_site_access.UsersSitesAccesses;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.user_site_access.UserSiteAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_WRITE;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;

@Service
class UserSiteAccessServiceImpl implements UserSiteAccessService, UserSiteAccessInnerService {
	private static final Logger LOG = LoggerFactory.getLogger(UserSiteAccessServiceImpl.class);

	private final UserSiteAccessRepository userSiteAccessRepository;
	private final UserPoliciesDocumentsServiceHelper policyDocumentService;
	private final UserOperationService userOperationService;
	private final UserOperationRepository userRepository;
	private final ProjectGroupsDAO projectGroupsDAO;
	private final ProjectRepository projectRepository;
	private final SiteRepository siteRepository;
	private final ApplicationEventPublisher publisher;
	private final ProjectAllocationRepository projectAllocationRepository;
	private final ResourceAccessRepository resourceAccessRepository;

	UserSiteAccessServiceImpl(UserSiteAccessRepository userSiteAccessRepository,
	                          UserPoliciesDocumentsServiceHelper policyDocumentService,
	                          UserOperationService userOperationService, UserOperationRepository userRepository,
	                          ProjectGroupsDAO projectGroupsDAO, ProjectRepository projectRepository,
	                          SiteRepository siteRepository,
	                          ProjectAllocationRepository projectAllocationRepository,
		                      ResourceAccessRepository resourceAccessRepository,
	                          ApplicationEventPublisher publisher) {
		this.userSiteAccessRepository = userSiteAccessRepository;
		this.policyDocumentService = policyDocumentService;
		this.userOperationService = userOperationService;
		this.userRepository = userRepository;
		this.projectGroupsDAO = projectGroupsDAO;
		this.projectRepository = projectRepository;
		this.siteRepository = siteRepository;
		this.projectAllocationRepository = projectAllocationRepository;
		this.resourceAccessRepository = resourceAccessRepository;
		this.publisher = publisher;
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "projectId")
	public void addAccess(String siteId, String projectId, FenixUserId userId) {
		if(!userSiteAccessRepository.exists(siteId, projectId, userId)) {
			Site site = siteRepository.findById(siteId)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Site id %s doesn't exist", siteId)));

			userSiteAccessRepository.add(siteId, projectId, userId);

			if(hasUserSitePolicyAcceptanceOrSiteHasntPolicy(siteId, userId))
				userOperationService.createUserAdditions(
					new SiteId(siteId, site.getExternalId()),
					projectId, policyDocumentService.getUserPolicyAcceptancesWithServicePolicies(siteId, userId)
				);
			else
				publisher.publishEvent(new UserSiteAccessGrantedEvent(userId));

			LOG.info("User {} has got manual access to project {} on site {}", userId, projectId, siteId);
		}
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "projectId")
	public void removeAccess(String siteId, String projectId, FenixUserId userId) {
		LOG.info("Manual removing user {} access to project {} on site {}", userId, projectId, siteId);
		if(userSiteAccessRepository.exists(siteId, projectId, userId)) {
			userSiteAccessRepository.remove(siteId, projectId, userId);
			userOperationService.createUserRemovals(siteId, projectId, userId);
		}
		publisher.publishEvent(new InvokeAfterCommitEvent(() -> publisher.publishEvent(new UserSiteAccessRevokedEvent(userId))));
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public UsersSitesAccesses getUsersSitesAccesses(String projectId) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Project id %s doesn't exist", projectId)));
		return new UsersSitesAccesses(
			projectGroupsDAO.getAllUsers(project.getCommunityId(), projectId),
			userSiteAccessRepository.findAllUserGroupedBySiteId(projectId),
			userRepository.findAllUserAdditions(projectId)
		);
	}

	@EventListener
	void onUserPolicyAcceptance(UserAcceptedPolicyEvent event) {
		FenixUserId userId = event.userId;
		PolicyAcceptance policyAcceptance = event.policyAcceptance;
		PolicyId policyDocumentId = policyAcceptance.policyDocumentId;

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

	@Override
	public void addAccessToSite(GrantAccess grantAccess) {
		if(!userSiteAccessRepository.exists(grantAccess.siteId.id, grantAccess.projectId.id.toString(), grantAccess.fenixUserId))
			userSiteAccessRepository.add(grantAccess.siteId.id, grantAccess.projectId.id.toString(), grantAccess.fenixUserId);

		Optional<UserStatus> userAdditionStatus = userRepository.findAdditionStatus(grantAccess.siteId.id,
			grantAccess.projectId.id.toString(), grantAccess.fenixUserId);
		if (userAdditionStatus.isEmpty() &&
			hasUserSitePolicyAcceptanceOrSiteHasntPolicy(grantAccess.siteId.id, grantAccess.fenixUserId)
		) {
			userOperationService.createUserAdditions(
				grantAccess.siteId,
				grantAccess.projectId.id.toString(),
				policyDocumentService.getUserPolicyAcceptancesWithServicePolicies(grantAccess.siteId.id, grantAccess.fenixUserId)
			);
		}
	}

	@Override
	public void revokeAccessToSite(GrantAccess grantAccess) {
		removeAccess(grantAccess);
	}

	@EventListener
	void onProjectAllocationRemove(ProjectDeallocationEvent event) {
		event.relatedGrantAccesses.forEach(this::removeAccess);
	}

	private void removeAccess(GrantAccess grantAccess) {
		if(!resourceAccessRepository.existsBySiteIdAndProjectIdAndFenixUserId(grantAccess.siteId.id, grantAccess.projectId, grantAccess.fenixUserId) &&
			projectAllocationRepository.findAllWithRelatedObjects(grantAccess.siteId.id, grantAccess.projectId).stream()
				.noneMatch(projectAllocation -> projectAllocation.resourceType.accessibleForAllProjectMembers)
		) {
			removeAccess(grantAccess.siteId.id, grantAccess.projectId, grantAccess.fenixUserId);
		}
	}

	private boolean hasUserSitePolicyAcceptanceOrSiteHasntPolicy(String siteId, FenixUserId userId) {
		return policyDocumentService.hasUserSitePolicyAcceptance(userId, siteId)
			|| !policyDocumentService.hasSitePolicy(siteId);
	}
}
