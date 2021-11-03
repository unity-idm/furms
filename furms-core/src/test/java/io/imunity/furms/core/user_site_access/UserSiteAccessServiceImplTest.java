/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_site_access;

import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserAcceptedPolicyEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationEvent;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrantAddedEvent;
import io.imunity.furms.domain.resource_access.UserGrantRemovedEvent;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.user_site_access.UserSiteAccessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSiteAccessServiceImplTest {

	@Mock
	private UserSiteAccessRepository userSiteAccessRepository;
	@Mock
	private UserPoliciesDocumentsServiceHelper policyDocumentService;
	@Mock
	private UserOperationService userOperationService;
	@Mock
	private UserOperationRepository userRepository;
	@Mock
	private ProjectGroupsDAO projectGroupsDAO;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private ProjectAllocationRepository projectAllocationRepository;
	@Mock
	private ResourceAccessRepository resourceAccessRepository;

	@InjectMocks
	private UserSiteAccessServiceImpl userSiteAccessService;

	@Test
	void shouldAddAccessAndCreateUserInstallation() {
		FenixUserId userId = new FenixUserId("userId");
		Site site = Site.builder()
			.externalId(new SiteExternalId("siteExternalId"))
			.build();
		when(userSiteAccessRepository.exists("siteId", "projectId", userId)).thenReturn(false);
		when(siteRepository.findById("siteId")).thenReturn(Optional.of(site));
		when(policyDocumentService.hasUserSitePolicyAcceptance(userId,"siteId")).thenReturn(true);

		userSiteAccessService.addAccess("siteId", "projectId", userId);

		verify(userSiteAccessRepository).add("siteId", "projectId", userId);
		verify(userOperationService).createUserAdditions(new SiteId("siteId", site.getExternalId()), "projectId", null);
	}

	@Test
	void shouldAddAccessAndNotCreateUserInstallationWhenPolicyIsNotAccepted() {
		FenixUserId userId = new FenixUserId("userId");
		Site site = Site.builder()
			.externalId(new SiteExternalId("siteExternalId"))
			.build();
		when(userSiteAccessRepository.exists("siteId", "projectId", userId)).thenReturn(false);
		when(siteRepository.findById("siteId")).thenReturn(Optional.of(site));
		when(policyDocumentService.hasUserSitePolicyAcceptance(userId,"siteId")).thenReturn(false);
		when(policyDocumentService.hasSitePolicy("siteId")).thenReturn(true);

		userSiteAccessService.addAccess("siteId", "projectId", userId);

		verify(userSiteAccessRepository).add("siteId", "projectId", userId);
		verify(userOperationService, times(0)).createUserAdditions(new SiteId("siteId", site.getExternalId()), "projectId", null);
	}

	@Test
	void shouldRemoveAccess() {
		FenixUserId userId = new FenixUserId("userId");
		when(userSiteAccessRepository.exists("siteId", "projectId", userId)).thenReturn(true);

		userSiteAccessService.removeAccess("siteId", "projectId", userId);

		verify(userSiteAccessRepository).remove("siteId", "projectId", userId);
		verify(userOperationService).createUserRemovals("siteId", "projectId", userId);
	}

	@Test
	void shouldNotRemoveAccessWhenAccessNotExisting() {
		FenixUserId userId = new FenixUserId("userId");
		when(userSiteAccessRepository.exists("siteId", "projectId", userId)).thenReturn(false);

		userSiteAccessService.removeAccess("siteId", "projectId", userId);

		verify(userSiteAccessRepository, times(0)).remove("siteId", "projectId", userId);
		verify(userOperationService, times(0)).createUserRemovals("siteId", "projectId", userId);
	}

	@Test
	void shouldGetUsersSitesAccesses() {
		String communityId = "communityId";
		String projectId = "projectId";
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(
			Project.builder()
				.id(projectId)
				.communityId(communityId)
				.build()
		));

		userSiteAccessService.getUsersSitesAccesses(projectId);

		verify(projectGroupsDAO).getAllUsers(communityId, projectId);
		verify(userSiteAccessRepository).findAllUserGroupedBySiteId(projectId);
		verify(userRepository).findAllUserAdditions(projectId);
	}

	@Test
	void shouldCreateUserAdditionAfterPolicyAcceptance() {
		FenixUserId userId = new FenixUserId("id");
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(new PolicyId(UUID.randomUUID()))
			.policyDocumentRevision(0)
			.acceptanceStatus(PolicyAcceptanceStatus.ACCEPTED)
			.decisionTs(Instant.now())
			.build();

		PolicyDocument policyDocument = PolicyDocument.builder()
			.build();
		when(policyDocumentService.findById(policyAcceptance.policyDocumentId)).thenReturn(policyDocument);
		Site site = Site.builder()
			.id("id")
			.externalId(new SiteExternalId("id"))
			.policyId(policyAcceptance.policyDocumentId)
			.build();
		when(policyDocumentService.getPolicySite(policyDocument)).thenReturn(site);
		when(userSiteAccessRepository.findAllUserProjectIds(site.getId(), userId)).thenReturn(Set.of("projectId"));
		when(userRepository.findAdditionStatus(site.getId(), "projectId", userId)).thenReturn(Optional.empty());

		userSiteAccessService.onUserPolicyAcceptance(new UserAcceptedPolicyEvent(userId, policyAcceptance));

		verify(userOperationService).createUserAdditions(new SiteId("id", "id"), "projectId", null);
	}

	@Test
	void shouldNotCreateUserAdditionAfterPolicyAcceptanceWhenUserHasNoRelatedProjects() {
		FenixUserId userId = new FenixUserId("id");
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(new PolicyId(UUID.randomUUID()))
			.policyDocumentRevision(0)
			.acceptanceStatus(PolicyAcceptanceStatus.ACCEPTED)
			.decisionTs(Instant.now())
			.build();

		PolicyDocument policyDocument = PolicyDocument.builder()
			.build();
		when(policyDocumentService.findById(policyAcceptance.policyDocumentId)).thenReturn(policyDocument);
		Site site = Site.builder()
			.id("id")
			.externalId(new SiteExternalId("id"))
			.policyId(policyAcceptance.policyDocumentId)
			.build();
		when(policyDocumentService.getPolicySite(policyDocument)).thenReturn(site);
		when(userSiteAccessRepository.findAllUserProjectIds(site.getId(), userId)).thenReturn(Set.of());

		userSiteAccessService.onUserPolicyAcceptance(new UserAcceptedPolicyEvent(userId, policyAcceptance));

		verify(userOperationService, times(0)).createUserAdditions(new SiteId("id", "id"), "projectId", null);
	}

	@Test
	void shouldNotCreateUserAdditionAfterPolicyAcceptanceWhenUserAdditionAlreadyExist() {
		FenixUserId userId = new FenixUserId("id");
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(new PolicyId(UUID.randomUUID()))
			.policyDocumentRevision(0)
			.acceptanceStatus(PolicyAcceptanceStatus.ACCEPTED)
			.decisionTs(Instant.now())
			.build();

		PolicyDocument policyDocument = PolicyDocument.builder()
			.build();
		when(policyDocumentService.findById(policyAcceptance.policyDocumentId)).thenReturn(policyDocument);
		Site site = Site.builder()
			.id("id")
			.externalId(new SiteExternalId("id"))
			.policyId(policyAcceptance.policyDocumentId)
			.build();
		when(policyDocumentService.getPolicySite(policyDocument)).thenReturn(site);
		when(userSiteAccessRepository.findAllUserProjectIds(site.getId(), userId)).thenReturn(Set.of("projectId"));
		when(userRepository.findAdditionStatus(site.getId(), "projectId", userId)).thenReturn(Optional.of(UserStatus.ADDING_PENDING));

		userSiteAccessService.onUserPolicyAcceptance(new UserAcceptedPolicyEvent(userId, policyAcceptance));

		verify(userOperationService, times(0)).createUserAdditions(new SiteId("id", "id"), "projectId", null);
	}

	@Test
	void shouldCreateUserAdditionAfterUserGrant() {
		FenixUserId userId = new FenixUserId("userId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId("siteId", "externalId"))
			.projectId("projectId")
			.fenixUserId(userId)
			.build();

		when(userSiteAccessRepository.exists("siteId", "projectId", userId)).thenReturn(false);
		when(userRepository.findAdditionStatus("siteId", "projectId", userId)).thenReturn(Optional.empty());
		when(policyDocumentService.hasUserSitePolicyAcceptance(userId,"siteId")).thenReturn(true);

		userSiteAccessService.onUserGrantAccess(new UserGrantAddedEvent(grantAccess));

		verify(userSiteAccessRepository).add("siteId", "projectId", userId);
		verify(userOperationService).createUserAdditions(new SiteId("siteId", "externalId"), "projectId", null);
	}

	@Test
	void shouldNotCreateUserAdditionAfterUserGrantWhenUserHasNotAcceptedPolicy() {
		FenixUserId userId = new FenixUserId("userId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId("siteId", "externalId"))
			.projectId("projectId")
			.fenixUserId(userId)
			.build();

		when(userSiteAccessRepository.exists("siteId", "projectId", userId)).thenReturn(false);
		when(userRepository.findAdditionStatus("siteId", "projectId", userId)).thenReturn(Optional.empty());
		when(policyDocumentService.hasUserSitePolicyAcceptance(userId,"siteId")).thenReturn(false);
		when(policyDocumentService.hasSitePolicy("siteId")).thenReturn(true);

		userSiteAccessService.onUserGrantAccess(new UserGrantAddedEvent(grantAccess));

		verify(userSiteAccessRepository).add("siteId", "projectId", userId);
		verify(userOperationService, times(0)).createUserAdditions(new SiteId("siteId", "externalId"), "projectId", null);
	}

	@Test
	void shouldNotCreateUserAdditionAfterUserGrantWhenUserAdditionAlreadyExist() {
		FenixUserId userId = new FenixUserId("userId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId("siteId", "externalId"))
			.projectId("projectId")
			.fenixUserId(userId)
			.build();

		when(userSiteAccessRepository.exists("siteId", "projectId", userId)).thenReturn(false);
		when(userRepository.findAdditionStatus("siteId", "projectId", userId)).thenReturn(Optional.of(UserStatus.ADDED));

		userSiteAccessService.onUserGrantAccess(new UserGrantAddedEvent(grantAccess));

		verify(userSiteAccessRepository).add("siteId", "projectId", userId);
		verify(userOperationService, times(0)).createUserAdditions(new SiteId("siteId", "externalId"), "projectId", null);
	}

	@Test
	void shouldRemoveAccessWhenUserGrantRevoke() {
		FenixUserId userId = new FenixUserId("userId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId("siteId", "externalId"))
			.projectId("projectId")
			.fenixUserId(userId)
			.build();

		when(resourceAccessRepository.existsBySiteIdAndProjectIdAndFenixUserId("siteId", "projectId", userId)).thenReturn(false);
		when(projectAllocationRepository.findAllWithRelatedObjects(grantAccess.siteId.id, grantAccess.projectId)).thenReturn(Set.of());
		when(userSiteAccessRepository.exists("siteId", "projectId", userId)).thenReturn(true);

		userSiteAccessService.onUserGrantRevoke(new UserGrantRemovedEvent(grantAccess));

		verify(userSiteAccessRepository).remove("siteId", "projectId", userId);
		verify(userOperationService).createUserRemovals("siteId", "projectId", userId);
	}

	@Test
	void shouldNotRemoveAccessAfterUserGrantRevokeWhenThereIsProjectAllocationAccessibleForAll() {
		FenixUserId userId = new FenixUserId("userId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId("siteId", "externalId"))
			.projectId("projectId")
			.fenixUserId(userId)
			.build();

		when(resourceAccessRepository.existsBySiteIdAndProjectIdAndFenixUserId("siteId", "projectId", userId)).thenReturn(false);
		when(projectAllocationRepository.findAllWithRelatedObjects(grantAccess.siteId.id, grantAccess.projectId)).thenReturn(Set.of(
			ProjectAllocationResolved.builder()
				.resourceType(ResourceType.builder()
					.accessibleForAllProjectMembers(true)
					.build())
				.build()
		));

		userSiteAccessService.onUserGrantRevoke(new UserGrantRemovedEvent(grantAccess));

		verify(userSiteAccessRepository, times(0)).remove("siteId", "projectId", userId);
		verify(userOperationService, times(0)).createUserRemovals("siteId", "projectId", userId);
	}

	@Test
	void shouldRemoveAccessWhenProjectAllocationRemoved() {
		FenixUserId userId = new FenixUserId("userId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId("siteId", "externalId"))
			.projectId("projectId")
			.fenixUserId(userId)
			.build();

		when(resourceAccessRepository.existsBySiteIdAndProjectIdAndFenixUserId("siteId", "projectId", userId)).thenReturn(false);
		when(projectAllocationRepository.findAllWithRelatedObjects(grantAccess.siteId.id, grantAccess.projectId)).thenReturn(Set.of());
		when(userSiteAccessRepository.exists("siteId", "projectId", userId)).thenReturn(true);

		userSiteAccessService.onProjectAllocationRemove(new ProjectDeallocationEvent(Set.of(grantAccess)));

		verify(userSiteAccessRepository).remove("siteId", "projectId", userId);
		verify(userOperationService).createUserRemovals("siteId", "projectId", userId);
	}
}