/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

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
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.sites.SiteInstalledProject;
import io.imunity.furms.domain.user_operation.UserAddition;
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

	SitesRestService(SiteService siteService,
	                 ProjectService projectService,
	                 ResourceCreditService resourceCreditService,
	                 ResourceTypeService resourceTypeService,
	                 ResourceUsageService resourceUsageService,
	                 InfraServiceService infraServiceService,
	                 ProjectAllocationService projectAllocationService,
	                 ProjectInstallationsService projectInstallationsService,
	                 UserService userService, UserAllocationsService userAllocationsService,
	                 SSHKeyService sshKeyService) {
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
	}

	List<Site> findAll() {
		final Set<io.imunity.furms.domain.resource_credits.ResourceCredit> resourceCredits = resourceCreditService.findAll();
		final Set<io.imunity.furms.domain.resource_types.ResourceType> resourceTypes = resourceTypeService.findAll();
		final Set<io.imunity.furms.domain.services.InfraService> services = infraServiceService.findAll();

		return siteService.findAll().stream()
				.map(site -> new Site(
						site.getId(),
						site.getName(),
						resourceCredits.stream()
								.filter(credit -> credit.siteId.equals(site.getId()))
								.map(credit -> new ResourceCredit(credit, findResource(resourceTypes, credit.resourceTypeId)))
								.collect(toList()),
						resourceTypes.stream()
								.filter(type -> type.siteId.equals(site.getId()))
								.map(ResourceType::new)
								.collect(toList()),
						services.stream()
								.filter(service -> service.siteId.equals(site.getId()))
								.map(InfraService::new)
								.collect(toList()),
						//TODO fill policy
						List.of()))
				.collect(toList());
	}

	Site findOneById(String siteId) {
		return resourceChecker.performIfExists(siteId, () -> siteService.findById(siteId))
				.map(site -> new Site(
						site.getId(),
						site.getName(),
						resourceCreditService.findAllWithAllocations(siteId).stream()
								.map(ResourceCredit::new)
								.collect(toList()),
						resourceTypeService.findAll(siteId).stream()
								.filter(type -> type.siteId.equals(site.getId()))
								.map(ResourceType::new)
								.collect(toList()),
						infraServiceService.findAll(siteId).stream()
								.filter(service -> service.siteId.equals(site.getId()))
								.map(InfraService::new)
								.collect(toList()),
						//TODO fill policy
						List.of()))
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
		return resourceChecker.performIfExists(siteId, () -> resourceTypeService.findById(resourceTypeId, siteId))
				.map(ResourceType::new)
				.get();
	}

	List<InfraService> findAllServicesBySiteId(String siteId) {
		return resourceChecker.performIfExists(siteId, () -> infraServiceService.findAll(siteId)).stream()
				.map(InfraService::new)
				.collect(toList());
	}

	InfraService findServiceBySiteIdAndId(String siteId, String serviceId) {
		return resourceChecker.performIfExists(siteId, () -> infraServiceService.findById(serviceId, siteId))
				.map(InfraService::new)
				.get();
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
		final Map<String, Set<String>> projectsGroupingByUserId = userAdditionsBySite.stream().collect(groupingBy(
				userAddition -> userAddition.userId,
				mapping(userAddition -> userAddition.projectId, toSet())));
		return userAdditionsBySite.stream()
				.map(userAddition -> new SiteUser(
						findUser(userAddition.userId),
						userAddition.uid,
						sshKeyService.findByOwnerId(userAddition.userId).stream()
								.map(sshKey -> sshKey.id)
								.collect(toList()),
						projectsGroupingByUserId.get(userAddition.userId)))
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

	private ProjectAllocationResolved findAllocation(String projectAllocationId, Set<ProjectAllocationResolved> allocations) {
		return allocations.stream()
				.filter(allocation -> allocation.id.equals(projectAllocationId))
				.findFirst()
				.get();
	}

	private Optional<io.imunity.furms.domain.resource_types.ResourceType> findResource(
			Set<io.imunity.furms.domain.resource_types.ResourceType> resourceTypes, String resourceTypeId) {
		return resourceTypes.stream()
				.filter(resourceType -> resourceTypeId.equals(resourceType.id))
				.findFirst();
	}

	private ProjectInstallation convertToProject(SiteInstalledProject projectInstallation) {
		final io.imunity.furms.domain.projects.Project projectBySiteId = projectService.findById(projectInstallation.projectId)
				.orElseThrow(() -> new ProjectRestNotFoundException("Project installations not found, " +
						"related Project not found."));
		final User projectLeader = findUser(projectBySiteId.getLeaderId().id);
		final Project project = new Project(projectBySiteId, projectLeader, projectInstallation.gid);

		return new ProjectInstallation(project, InstallationStatus.INSTALLED, null, projectInstallation.gid.id);
	}

	private User findUser(String userId) {
		return userService.findById(new PersistentId(userId))
				.map(User::new)
				.orElse(null);
	}

}
