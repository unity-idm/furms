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
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import io.imunity.furms.rest.user.User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.api.users.UserAllocationsService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.sites.SiteInstalledProject;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.rest.error.exceptions.ProjectRestNotFoundException;

@Service
class SitesRestService {

	private final SiteService siteService;
	private final ProjectService projectService;
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


	SitesRestService(SiteService siteService,
	                 ProjectService projectService,
	                 ResourceCreditService resourceCreditService,
	                 ResourceTypeService resourceTypeService,
	                 ResourceUsageService resourceUsageService,
	                 InfraServiceService infraServiceService,
	                 ProjectAllocationService projectAllocationService,
	                 ProjectInstallationsService projectInstallationsService,
	                 UserService userService,
	                 UserAllocationsService userAllocationsService,
	                 SSHKeyService sshKeyService,
	                 PolicyDocumentService policyDocumentService) {
		this.siteService = siteService;
		this.projectService = projectService;
		this.resourceCreditService = resourceCreditService;
		this.resourceTypeService = resourceTypeService;
		this.resourceUsageService = resourceUsageService;
		this.infraServiceService = infraServiceService;
		this.projectAllocationService = projectAllocationService;
		this.projectInstallationsService = projectInstallationsService;
		this.userService = userService;
		this.userAllocationsService = userAllocationsService;
		this.sshKeyService = sshKeyService;
		this.resourceChecker = new ResourceChecker(siteService::existsById);
		this.policyDocumentService = policyDocumentService;
	}

	List<Site> findAll() {
		return siteService.findAllOfCurrentUserId().stream()
				.map(this::createSite)
				.collect(toList());
	}

	Site findOneById(String siteId) {
		return resourceChecker.performIfExists(siteId, () -> siteService.findById(siteId))
				.map(this::createSite)
				.get();
	}

	List<ResourceCredit> findAllResourceCreditsBySiteId(String siteId) {
		return resourceChecker.performIfExists(siteId, () -> resourceCreditService.findAllWithAllocations(siteId)).stream()
				.map(ResourceCredit::new)
				.collect(toList());
	}

	ResourceCredit findResourceCreditBySiteIdAndId(String siteId, String creditId) {
		return resourceChecker.performIfExists(siteId, () -> resourceCreditService.findWithAllocationsByIdAndSiteId(creditId, siteId))
				.map(ResourceCredit::new)
				.get();
	}

	List<ResourceType> findAllResourceTypesBySiteId(String siteId) {
		return resourceChecker.performIfExists(siteId, () -> resourceTypeService.findAll(siteId)).stream()
				.map(ResourceType::new)
				.collect(toList());
	}

	ResourceType findResourceTypesBySiteIdAndId(String siteId, String resourceTypeId) {
		return resourceChecker.performIfExistsAndMatching(siteId,
					() -> resourceTypeService.findById(resourceTypeId, siteId),
					resourceType -> resourceType.isPresent() && resourceType.get().siteId.equals(siteId))
				.map(ResourceType::new)
				.get();
	}

	List<InfraService> findAllServicesBySiteId(String siteId) {
		return resourceChecker.performIfExists(siteId, () -> infraServiceService.findAll(siteId)).stream()
				.map(InfraService::new)
				.collect(toList());
	}

	InfraService findServiceBySiteIdAndId(String siteId, String serviceId) {
		return resourceChecker.performIfExistsAndMatching(siteId,
					() -> infraServiceService.findById(serviceId, siteId),
					service -> service.isPresent() && service.get().siteId.equals(siteId))
				.map(InfraService::new)
				.get();
	}

	List<Policy> findAllPolicies(String siteId) {
		return resourceChecker.performIfExists(siteId, () -> policyDocumentService.findAllBySiteId(siteId)).stream()
				.map(Policy::new)
				.collect(toList());
	}

	Policy findPolicy(String siteId, String policyId) {
		return resourceChecker.performIfExistsAndMatching(siteId,
					() -> policyDocumentService.findById(siteId, new io.imunity.furms.domain.policy_documents.PolicyId(policyId)),
					policy -> policy.isPresent() && policy.get().siteId.equals(siteId))
				.map(Policy::new)
				.get();
	}

	List<PolicyAcceptance> findAllPoliciesAcceptances(String siteId) {
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
		return resourceChecker.performIfExists(siteId, () -> policyDocumentService.findAllUsersPolicies(siteId)).entrySet().stream()
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

	List<PolicyAcceptance> addPolicyAcceptance(String siteId, String policyId, String fenixUserId) {
		policyDocumentService.addUserPolicyAcceptance(siteId, new FenixUserId(fenixUserId), io.imunity.furms.domain.policy_documents.PolicyAcceptance.builder()
				.policyDocumentId(new io.imunity.furms.domain.policy_documents.PolicyId(policyId))
				.policyDocumentRevision(0)
				.acceptanceStatus(PolicyAcceptanceStatus.ACCEPTED)
				.decisionTs(convertToUTCTime(ZonedDateTime.now(ZoneId.systemDefault())).toInstant(ZoneOffset.UTC))
				.build());
		return findAllPoliciesAcceptances(siteId);
	}

	List<ProjectInstallation> findAllProjectInstallationsBySiteId(String siteId) {
		return resourceChecker.performIfExists(siteId, () -> projectInstallationsService.findAllSiteInstalledProjectsBySiteId(siteId))
				.stream()
				.map(this::convertToProject)
				.collect(toList());
	}

	List<SiteUser> findAllSiteUsersBySiteId(String siteId) {
		final Set<UserAddition> userAdditionsBySite = resourceChecker.performIfExists(siteId,
				() -> userAllocationsService.findAllBySiteId(siteId));
		return userAdditionsBySite.stream()
				.collect(groupingBy(userAddition -> userAddition.userId, toSet()))
				.entrySet().stream()
				.map(entry -> createSiteUser(entry.getKey(), entry.getValue()))
				.collect(toList());
	}

	List<ProjectAllocation> findAllProjectAllocationsBySiteId(String siteId) {
		return resourceChecker.performIfExists(siteId, () -> projectAllocationService.findAllWithRelatedObjectsBySiteId(siteId))
				.stream()
				.map(ProjectAllocation::new)
				.collect(toList());
	}

	List<ProjectAllocation> findAllProjectAllocationsBySiteIdAndProjectId(String siteId, String projectId) {
		return resourceChecker.performIfExists(siteId,
					() -> projectAllocationService.findAllWithRelatedObjectsBySiteIdAndProjectId(siteId, projectId))
				.stream()
				.map(ProjectAllocation::new)
				.collect(toList());
	}

	List<SiteAllocatedResources> findAllSiteAllocatedResourcesBySiteId(String siteId) {
		return resourceChecker.performIfExists(siteId, () -> projectAllocationService.findAllChunksBySiteId(siteId)).stream()
				.map(SiteAllocatedResources::new)
				.collect(toList());
	}

	List<SiteAllocatedResources> findAllSiteAllocatedResourcesBySiteIdAndProjectId(String siteId, String projectId) {
		return resourceChecker.performIfExists(siteId,
					() -> projectAllocationService.findAllChunksBySiteIdAndProjectId(siteId, projectId))
				.stream()
				.map(SiteAllocatedResources::new)
				.collect(toList());
	}

	List<ProjectCumulativeResourceConsumption> findAllProjectCumulativeResourceConsumptionBySiteIdAndProjectId(
			String siteId, String projectId) {
		return resourceChecker.performIfExists(siteId,
					() -> projectAllocationService.findAllWithRelatedObjectsBySiteIdAndProjectId(siteId, projectId))
				.stream()
				.map(ProjectCumulativeResourceConsumption::new)
				.collect(toList());
	}

	List<ProjectUsageRecord> findAllProjectUsageRecordBySiteIdAndProjectIdInPeriod(String siteId,
	                                                                               String projectId,
	                                                                               ZonedDateTime from,
	                                                                               ZonedDateTime until) {
		final Set<ProjectAllocationResolved> allocations = resourceChecker.performIfExists(siteId,
				() -> projectAllocationService.findAllWithRelatedObjectsBySiteIdAndProjectId(siteId, projectId));
		final Set<UserResourceUsage> userUsages = resourceUsageService.findAllUserUsages(
				siteId,
				allocations.stream()
					.map(allocation -> UUID.fromString(allocation.id))
					.collect(toSet()),
				convertToUTCTime(from),
				convertToUTCTime(until));

		return userUsages.stream()
				.map(userUsage -> new ProjectUsageRecord(userUsage, findAllocation(userUsage.projectAllocationId, allocations)))
				.collect(toList());
	}

	private Site createSite(io.imunity.furms.domain.sites.Site site) {
		return new Site(
				site.getId(),
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

	private ProjectAllocationResolved findAllocation(String projectAllocationId, Set<ProjectAllocationResolved> allocations) {
		return allocations.stream()
				.filter(allocation -> allocation.id.equals(projectAllocationId))
				.findFirst()
				.get();
	}

	private SiteUser createSiteUser(String fenixUserId, Set<UserAddition> userAdditions) {
		final String uid = userAdditions.stream()
				.filter(userAddition -> !StringUtils.isEmpty(userAddition.uid))
				.findAny()
				.map(userAddition -> userAddition.uid)
				.orElseThrow(() -> new IllegalArgumentException("UID not found"));
		final Optional<FURMSUser> furmsUser = userService.findByFenixUserId(new FenixUserId(fenixUserId));
		return findUserByFenixId(fenixUserId)
				.map(user -> new SiteUser(
						user,
						uid,
						furmsUser.map(persistedUser ->
								sshKeyService.findByOwnerId(persistedUser.id.get().id).stream()
									.map(sshKey -> sshKey.value)
									.collect(toList()))
								.orElse(List.of()),
						userAdditions.stream()
							.map(userAddition -> userAddition.projectId)
							.collect(toSet())))
				.orElse(null);
	}

	private ProjectInstallation convertToProject(SiteInstalledProject projectInstallation) {
		final io.imunity.furms.domain.projects.Project projectBySiteId = projectService.findById(projectInstallation.projectId)
				.orElseThrow(() -> new ProjectRestNotFoundException("Project installations not found, " +
						"related Project not found."));
		final User projectLeader = findUser(projectBySiteId.getLeaderId().id);
		final Project project = new Project(projectBySiteId, projectLeader, Set.of(projectInstallation));

		return new ProjectInstallation(project, INSTALLED, projectInstallation.gid.id);
	}

	private User findUser(String userId) {
		return userService.findById(new PersistentId(userId))
				.map(User::new)
				.orElse(null);
	}

	private Optional<User> findUserByFenixId(String fenixUserId) {
		return userService.findByFenixUserId(new FenixUserId(fenixUserId))
				.map(User::new);
	}

}
