/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

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
import io.imunity.furms.domain.project_installation.ProjectInstallationJobStatus;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.sites.Gid;
import io.imunity.furms.domain.sites.SiteInstalledProject;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.rest.error.exceptions.*;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static java.lang.String.format;
import static java.util.stream.Collectors.*;

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
		return siteService.findById(siteId)
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
				.orElseThrow(() -> new SiteRestNotFoundException("Site for specific ID doesn't exist."));
	}

	List<ResourceCredit> findAllResourceCreditsBySiteId(String siteId) {
		return resourceCreditService.findAllWithAllocations(siteId).stream()
				.map(ResourceCredit::new)
				.collect(toList());
	}

	ResourceCredit findResourceCreditBySiteIdAndId(String siteId, String creditId) {
		return resourceCreditService.findWithAllocationsByIdAndSiteId(creditId, siteId)
				.map(ResourceCredit::new)
				.orElseThrow(() -> new ResourceCreditRestNotFoundException(format("Could not find Resource Credit for " +
						"id=%s and siteId=%s", creditId, siteId)));
	}

	List<ResourceType> findAllResourceTypesBySiteId(String siteId) {
		return resourceTypeService.findAll(siteId).stream()
				.map(ResourceType::new)
				.collect(toList());
	}

	ResourceType findResourceTypesBySiteIdAndId(String siteId, String resourceTypeId) {
		return resourceTypeService.findById(resourceTypeId, siteId)
				.map(ResourceType::new)
				.orElseThrow(() -> new ResourceTypeRestNotFoundException(format("Could not find Resource Type for " +
						"id=%s and siteId=%s", resourceTypeId, siteId)));
	}

	List<InfraService> findAllServicesBySiteId(String siteId) {
		return infraServiceService.findAll(siteId).stream()
				.map(InfraService::new)
				.collect(toList());
	}

	InfraService findServiceBySiteIdAndId(String siteId, String serviceId) {
		return infraServiceService.findById(serviceId, siteId)
				.map(InfraService::new)
				.orElseThrow(() -> new InfraServiceRestNotFoundException(format("Could not find Service for " +
						"id=%s and siteId=%s", serviceId, siteId)));
	}

	List<ProjectInstallation> findAllProjectInstallationsBySiteId(String siteId) {
		return projectInstallationsService.findAllSiteInstalledProjectsBySiteId(siteId).stream()
				.map(this::convertToProject)
				.collect(toList());
	}

	List<SiteUser> findAllSiteUsersBySiteId(String siteId) {
		final Set<UserAddition> userAdditionsBySite = userAllocationsService.findAllBySiteId(siteId);
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
		return projectAllocationService.findAllWithRelatedObjectsBySiteId(siteId).stream()
				.map(ProjectAllocation::new)
				.collect(toList());
	}

	List<ProjectAllocation> findAllProjectAllocationsBySiteIdAndProjectId(String siteId, String projectId) {
		return projectAllocationService.findAllWithRelatedObjectsBySiteIdAndProjectId(siteId, projectId).stream()
				.map(ProjectAllocation::new)
				.collect(toList());
	}

	List<SiteAllocatedResources> findAllSiteAllocatedResourcesBySiteId(String siteId) {
		return projectAllocationService.findAllChunksBySiteId(siteId).stream()
				.map(SiteAllocatedResources::new)
				.collect(toList());
	}

	List<SiteAllocatedResources> findAllSiteAllocatedResourcesBySiteIdAndProjectId(String siteId, String projectId) {
		return projectAllocationService.findAllChunksBySiteIdAndProjectId(siteId, projectId).stream()
				.map(SiteAllocatedResources::new)
				.collect(toList());
	}

	List<ProjectCumulativeResourceConsumption> findAllProjectCumulativeResourceConsumptionBySiteIdAndProjectId(
			String siteId, String projectId) {
		return projectAllocationService.findAllWithRelatedObjectsBySiteIdAndProjectId(siteId, projectId).stream()
				.map(ProjectCumulativeResourceConsumption::new)
				.collect(toList());
	}

	List<ProjectUsageRecord> findAllProjectUsageRecordBySiteIdAndProjectIdInPeriod(String siteId,
	                                                                               String projectId,
	                                                                               ZonedDateTime from,
	                                                                               ZonedDateTime until) {
		final Set<ProjectAllocationResolved> allocations = projectAllocationService
				.findAllWithRelatedObjectsBySiteIdAndProjectId(siteId, projectId);
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
		final Project project = new Project(projectBySiteId, projectInstallation, projectLeader);

		return new ProjectInstallation(project, InstallationStatus.INSTALLED, null, projectInstallation.gid.id);
	}

	private User findUser(String userId) {
		return userService.findById(new PersistentId(userId))
				.map(User::new)
				.orElse(null);
	}

}
