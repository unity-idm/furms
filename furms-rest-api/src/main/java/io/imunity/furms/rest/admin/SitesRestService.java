/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import static io.imunity.furms.rest.admin.AcceptanceStatus.ACCEPTED;
import static io.imunity.furms.rest.admin.AcceptanceStatus.ACCEPTED_FORMER_REVISION;
import static io.imunity.furms.rest.admin.AcceptanceStatus.NOT_ACCEPTED;
import static io.imunity.furms.rest.admin.InstallationStatus.INSTALLED;
import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.api.site_agent_pending_message.SiteAgentConnectionService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.api.users.UserAllocationsService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.sites.SiteInstalledProjectResolved;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UnknownUserException;
import io.imunity.furms.rest.user.User;

@Service
class SitesRestService {

	private final SiteService siteService;
	private final ResourceCreditService resourceCreditService;
	private final ResourceTypeService resourceTypeService;
	private final ResourceUsageService resourceUsageService;
	private final InfraServiceService infraServiceService;
	private final ProjectAllocationService projectAllocationService;
	private final ProjectInstallationsService projectInstallationsService;
	private final UserService userService;
	private final UserAllocationsService userAllocationsService;
	private final SSHKeyService sshKeyService;
	private final ResourceChecker resourceChecker;
	private final PolicyDocumentService policyDocumentService;
	private final SiteAgentConnectionService siteAgentConnectionService;

	SitesRestService(SiteService siteService,
			ResourceCreditService resourceCreditService,
			ResourceTypeService resourceTypeService,
			ResourceUsageService resourceUsageService,
			InfraServiceService infraServiceService,
			ProjectAllocationService projectAllocationService,
			ProjectInstallationsService projectInstallationsService,
			UserService userService,
			UserAllocationsService userAllocationsService,
			SSHKeyService sshKeyService,
			PolicyDocumentService policyDocumentService,
			SiteAgentConnectionService siteAgentConnectionService) {
		this.siteService = siteService;
		this.resourceCreditService = resourceCreditService;
		this.resourceTypeService = resourceTypeService;
		this.resourceUsageService = resourceUsageService;
		this.infraServiceService = infraServiceService;
		this.projectAllocationService = projectAllocationService;
		this.projectInstallationsService = projectInstallationsService;
		this.userService = userService;
		this.userAllocationsService = userAllocationsService;
		this.sshKeyService = sshKeyService;
		this.resourceChecker = new ResourceChecker(id -> siteService.existsById(new SiteId(id)));
		this.policyDocumentService = policyDocumentService;
		this.siteAgentConnectionService = siteAgentConnectionService;
	}

	List<Site> findAll() {
		return siteService.findAllOfCurrentUserId().stream()
				.map(this::createSite)
				.collect(toList());
	}

	Site findOneById(SiteId siteId) {
		return resourceChecker.performIfExists(siteId.id, () -> siteService.findById(siteId))
				.map(this::createSite)
				.get();
	}

	List<ResourceCredit> findAllResourceCreditsBySiteId(SiteId siteId) {
		return resourceChecker.performIfExists(siteId.id, () -> resourceCreditService.findAllWithAllocations(siteId)).stream()
				.map(ResourceCredit::new)
				.collect(toList());
	}

	ResourceCredit findResourceCreditBySiteIdAndId(SiteId siteId, ResourceCreditId creditId) {
		return resourceChecker.performIfExists(siteId.id,
				() -> resourceCreditService.findWithAllocationsByIdAndSiteId(creditId, siteId))
				.map(ResourceCredit::new)
				.get();
	}

	List<ResourceType> findAllResourceTypesBySiteId(SiteId siteId) {
		return resourceChecker.performIfExists(siteId.id, () -> resourceTypeService.findAll(siteId)).stream()
				.map(ResourceType::new)
				.collect(toList());
	}

	ResourceType findResourceTypesBySiteIdAndId(SiteId siteId, ResourceTypeId resourceTypeId) {
		return resourceChecker.performIfExistsAndMatching(siteId.id,
					() -> resourceTypeService.findById(resourceTypeId, siteId),
					resourceType -> resourceType.isPresent() && resourceType.get().siteId.equals(siteId))
				.map(ResourceType::new)
				.get();
	}

	List<InfraService> findAllServicesBySiteId(SiteId siteId) {
		return resourceChecker.performIfExists(siteId.id, () -> infraServiceService.findAll(siteId)).stream()
				.map(InfraService::new)
				.collect(toList());
	}

	InfraService findServiceBySiteIdAndId(SiteId siteId, InfraServiceId serviceId) {
		return resourceChecker.performIfExistsAndMatching(siteId.id,
					() -> infraServiceService.findById(serviceId, siteId),
					service -> service.isPresent() && service.get().siteId.equals(siteId))
				.map(InfraService::new)
				.get();
	}

	List<Policy> findAllPolicies(SiteId siteId) {
		return resourceChecker.performIfExists(siteId.id, () -> policyDocumentService.findAllBySiteId(siteId)).stream()
				.map(Policy::new)
				.collect(toList());
	}

	Policy findPolicy(SiteId siteId, String policyId) {
		return resourceChecker.performIfExistsAndMatching(siteId.id,
					() -> policyDocumentService.findById(siteId, new io.imunity.furms.domain.policy_documents.PolicyId(policyId)),
					policy -> policy.isPresent() && policy.get().siteId.equals(siteId))
				.map(Policy::new)
				.get();
	}

	List<PolicyAcceptance> findAllPoliciesAcceptances(SiteId siteId) {
		Set<UserPolicyAcceptances> allUsersPolicyAcceptances = policyDocumentService.findAllUsersPolicyAcceptances(siteId);
		Map<FenixUserId, Map<PolicyId, io.imunity.furms.domain.policy_documents.PolicyAcceptance>> lastPoliciesAcceptanceByUserIdAndPolicyId = allUsersPolicyAcceptances.stream()
			.filter(userPolicyAcceptances -> userPolicyAcceptances.user.fenixUserId.isPresent())
			.collect(groupingBy(
				userPolicyAcceptances -> userPolicyAcceptances.user.fenixUserId.get(),
				flatMapping(
					userPolicyAcceptances -> userPolicyAcceptances.policyAcceptances.stream(),
					toMap(
						policyAcceptance -> policyAcceptance.policyDocumentId,
						Function.identity(),
						BinaryOperator.maxBy(Comparator.comparing(policyAcceptance -> policyAcceptance.policyDocumentRevision))
					))
			));
		return resourceChecker.performIfExists(siteId.id, () -> policyDocumentService.findAllUsersPolicies(siteId)).entrySet().stream()
			.flatMap(entry -> entry.getValue().stream()
				.map(policy -> {
					FenixUserId fenixUserId = entry.getKey();
					return getPolicyAcceptance(
						policy,
						fenixUserId,
						ofNullable(lastPoliciesAcceptanceByUserIdAndPolicyId.get(fenixUserId))
							.map(map -> map.get(policy.id))
							.orElse(null)
					);
				}))
			.collect(toList());
	}

	private PolicyAcceptance getPolicyAcceptance(PolicyDocument policy, FenixUserId fenixUserId, io.imunity.furms.domain.policy_documents.PolicyAcceptance lastPolicyAcceptance) {
		if (lastPolicyAcceptance != null) {
			return PolicyAcceptance.builder()
				.policyId(policy.id.id.toString())
				.revision(policy.revision)
				.acceptedRevision(lastPolicyAcceptance.policyDocumentRevision)
				.fenixUserId(fenixUserId)
				.acceptanceStatus(lastPolicyAcceptance.policyDocumentRevision == policy.revision ? ACCEPTED : ACCEPTED_FORMER_REVISION)
				.decisionTs(lastPolicyAcceptance.decisionTs)
				.build();
		}
		return PolicyAcceptance.builder()
			.policyId(policy.id.id.toString())
			.revision(policy.revision)
			.fenixUserId(fenixUserId)
			.acceptanceStatus(NOT_ACCEPTED)
			.build();
	}

	List<PolicyAcceptance> addPolicyAcceptance(SiteId siteId, String policyId, String fenixUserId) {
		policyDocumentService.addUserPolicyAcceptance(siteId, new FenixUserId(fenixUserId), io.imunity.furms.domain.policy_documents.PolicyAcceptance.builder()
				.policyDocumentId(new io.imunity.furms.domain.policy_documents.PolicyId(policyId))
				.policyDocumentRevision(0)
				.acceptanceStatus(PolicyAcceptanceStatus.ACCEPTED)
				.decisionTs(convertToUTCTime(ZonedDateTime.now(ZoneId.systemDefault())).toInstant(ZoneOffset.UTC))
				.build());
		return findAllPoliciesAcceptances(siteId);
	}

	List<ProtocolMessage> getProtocolMessages(SiteId siteId) {
		resourceChecker.performIfExists(siteId.id, () -> siteService.findById(siteId));
		return resourceChecker.performIfExists(siteId.id, () -> siteAgentConnectionService.findAll(siteId))
			.stream()
			.map(message -> new ProtocolMessage(
				message.correlationId.id,
				message.jsonContent,
				message.utcAckAt == null ? MessageStatus.SENT : MessageStatus.ACKNOWLEDGED,
				Optional.ofNullable(message.utcSentAt).map(x -> x.atZone(ZoneOffset.UTC)).orElse(null),
				Optional.ofNullable(message.utcAckAt).map(x -> x.atZone(ZoneOffset.UTC)).orElse(null),
				message.retryCount)
			).collect(Collectors.toList());
	}

	void dropProtocolMessage(SiteId siteId, String messageId) {
		resourceChecker.performIfExists(siteId.id, () -> siteService.findById(siteId));
		resourceChecker.performIfExists(messageId, () -> siteAgentConnectionService.delete(siteId,
			new CorrelationId(messageId)));
	}

	void retryProtocolMessage(SiteId siteId, String messageId) {
		resourceChecker.performIfExists(siteId.id, () -> siteService.findById(siteId));
		resourceChecker.performIfExists(messageId, () -> siteAgentConnectionService.retry(siteId,
			new CorrelationId(messageId)));
	}

	List<ProjectInstallation> findAllProjectInstallationsBySiteId(SiteId siteId) {
		return resourceChecker.performIfExists(siteId.id,
				() -> projectInstallationsService.findAllSiteInstalledProjectsBySiteId(siteId))
				.stream()
				.map(this::convertToProject)
				.collect(toList());
	}

	List<SiteUser> findAllSiteUsersBySiteId(SiteId siteId) {
		Set<UserAddition> installedUserAdditionsBySite = resourceChecker.performIfExists(siteId.id,
				() -> userAllocationsService.findUserAdditionsBySiteId(siteId).stream()
						.filter(userAddition -> userAddition.status.isInstalled())
						.collect(toSet()));
		return installedUserAdditionsBySite.stream()
				.collect(groupingBy(userAddition -> userAddition.userId, toSet()))
				.entrySet().stream()
				.map(entry -> createSiteUser(entry.getKey(), entry.getValue(), siteId))
				.collect(toList());
	}

	SiteUser findSiteUserByUserIdAndSiteId(FenixUserId userId, SiteId siteId) {
		Set<UserAddition> installedUserAdditionsBySite = resourceChecker.performIfExists(siteId.id,
				() -> userAllocationsService.findUserAdditionsBySiteAndFenixUserId(siteId, userId).stream()
						.filter(userAddition -> userAddition.status.isInstalled())
						.collect(toSet()));
		return createSiteUser(userId, installedUserAdditionsBySite, siteId);
	}

	List<ProjectAllocation> findAllProjectAllocationsBySiteId(SiteId siteId) {
		return resourceChecker.performIfExists(siteId.id,
				() -> projectAllocationService.findAllWithRelatedObjectsBySiteId(siteId))
				.stream()
				.map(ProjectAllocation::new)
				.collect(toList());
	}

	List<ProjectAllocation> findAllProjectAllocationsBySiteIdAndProjectId(SiteId siteId, ProjectId projectId) {
		return resourceChecker.performIfExists(siteId.id,
					() -> projectAllocationService.findAllWithRelatedObjectsBySiteIdAndProjectId(siteId, projectId))
				.stream()
				.map(ProjectAllocation::new)
				.collect(toList());
	}

	List<SiteAllocatedResources> findAllSiteAllocatedResourcesBySiteId(SiteId siteId) {
		return resourceChecker.performIfExists(siteId.id, () -> projectAllocationService.findAllChunksBySiteId(siteId)).stream()
				.map(SiteAllocatedResources::new)
				.collect(toList());
	}

	List<SiteAllocatedResources> findAllSiteAllocatedResourcesBySiteIdAndProjectId(SiteId siteId, ProjectId projectId) {
		return resourceChecker.performIfExists(siteId.id,
					() -> projectAllocationService.findAllChunksBySiteIdAndProjectId(siteId, projectId))
				.stream()
				.map(SiteAllocatedResources::new)
				.collect(toList());
	}

	List<ProjectCumulativeResourceConsumption> findAllProjectCumulativeResourceConsumptionBySiteIdAndProjectId(
		SiteId siteId, ProjectId projectId) {
		return resourceChecker.performIfExists(siteId.id,
					() -> projectAllocationService.findAllWithRelatedObjectsBySiteIdAndProjectId(siteId, projectId))
				.stream()
				.map(ProjectCumulativeResourceConsumption::new)
				.collect(toList());
	}

	List<ProjectUsageRecord> findAllProjectUsageRecordBySiteIdAndProjectIdInPeriod(SiteId siteId,
	                                                                               ProjectId projectId,
	                                                                               ZonedDateTime from,
	                                                                               ZonedDateTime until) {
		final Set<ProjectAllocationResolved> allocations = resourceChecker.performIfExists(siteId.id,
				() -> projectAllocationService.findAllWithRelatedObjectsBySiteIdAndProjectId(siteId, projectId));
		final Set<UserResourceUsage> userUsages = resourceUsageService.findAllUserUsages(
				siteId,
				allocations.stream()
					.map(allocation -> allocation.id)
					.collect(toSet()),
				convertToUTCTime(from),
				convertToUTCTime(until));

		return userUsages.stream()
				.map(userUsage -> new ProjectUsageRecord(userUsage, findAllocation(userUsage.projectAllocationId, allocations)))
				.collect(toList());
	}

	private Site createSite(io.imunity.furms.domain.sites.Site site) {
		return new Site(
				site.getId().id.toString(),
				site.getName(),
				getSelectedPolicyId(site),
				resourceCreditService.findAllWithAllocations(site.getId()).stream()
						.map(ResourceCredit::new)
						.collect(toList()),
				resourceTypeService.findAll(site.getId()).stream()
						.filter(type -> type.siteId.equals(site.getId()))
						.map(ResourceType::new)
						.collect(toList()),
				infraServiceService.findAll(site.getId()).stream()
						.filter(service -> service.siteId.equals(site.getId()))
						.map(InfraService::new)
						.collect(toList()),
				policyDocumentService.findAllBySiteId(site.getId()).stream()
						.filter(policy -> policy.siteId.equals(site.getId()))
						.map(Policy::new)
						.collect(toList()));
	}

	private String getSelectedPolicyId(io.imunity.furms.domain.sites.Site site) {
		return site.getPolicyId() == null || site.getPolicyId().id == null
				? null
				: site.getPolicyId().id.toString();
	}

	private ProjectAllocationResolved findAllocation(ProjectAllocationId projectAllocationId,
	                                                 Set<ProjectAllocationResolved> allocations) {
		return allocations.stream()
				.filter(allocation -> allocation.id.equals(projectAllocationId))
				.findFirst()
				.get();
	}

	private SiteUser createSiteUser(FenixUserId fenixUserId, Set<UserAddition> installedUserAdditions, SiteId siteId) {
		return userService.findByFenixUserId(fenixUserId)
				.map(user -> new SiteUser(
						new User(user),
						findFirstUserIdOrThrow(installedUserAdditions, fenixUserId),
						sshKeyService.findSiteSSHKeysByUserIdAndSite(user.id.get(), siteId).sshKeys,
						installedUserAdditions.stream()
							.map(userAddition -> userAddition.projectId.id.toString())
							.collect(toSet())))
				.orElseThrow(() -> new UnknownUserException(fenixUserId));
	}
	
	private String findFirstUserIdOrThrow(Set<UserAddition> installedUserAdditions, FenixUserId fenixUserId) {
		if (installedUserAdditions.isEmpty())
			throw new IllegalArgumentException(fenixUserId.id + " not provisioned on a given site");
		return installedUserAdditions.stream()
				.filter(userAddition -> StringUtils.hasText(userAddition.uid))
				.findAny()
				.map(userAddition -> userAddition.uid)
				.orElseThrow(() -> new IllegalArgumentException("UID not found"));
	}

	private ProjectInstallation convertToProject(SiteInstalledProjectResolved projectInstallation) {
		final User projectLeader = findUser(projectInstallation.project.getLeaderId().id);
		final Project project = new Project(projectInstallation.project, projectLeader, projectInstallation.toSiteInstalledProject());

		return new ProjectInstallation(project, INSTALLED, projectInstallation.gid.id);
	}

	private User findUser(String userId) {
		return userService.findById(new PersistentId(userId))
				.map(User::new)
				.orElse(null);
	}

}
