/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.sites.SiteInstalledProjectResolved;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rest.error.exceptions.ProjectRestNotFoundException;
import io.imunity.furms.utils.UTCTimeUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
class ProjectsRestService {

	private final ProjectService projectService;
	private final ProjectAllocationService projectAllocationService;
	private final ProjectInstallationsService projectInstallationsService;
	private final ResourceChecker resourceChecker;
	private final ProjectsRestConverter converter;
	private final SiteService siteService;

	ProjectsRestService(ProjectService projectService,
	                    ProjectAllocationService projectAllocationService,
	                    ProjectInstallationsService projectInstallationsService,
	                    ProjectsRestConverter converter,
	                    SiteService siteService) {
		this.projectService = projectService;
		this.projectAllocationService = projectAllocationService;
		this.resourceChecker = new ResourceChecker(projectService::existsById);
		this.projectInstallationsService = projectInstallationsService;
		this.converter = converter;
		this.siteService = siteService;
	}

	List<Project> findAll() {
		return Stream.concat(
					projectService.findAllByCurrentUserId().stream(),
					findAllUserInstalledProjects().stream().map(siteInstalledProject -> siteInstalledProject.project))
				.map(converter::convert)
				.collect(toList());
	}

	ProjectWithUsers findOneById(String projectId) {
		return resourceChecker.performIfExists(projectId, () -> findProjectById(projectId)).get();
	}

	void delete(String projectId) {
		final io.imunity.furms.domain.projects.Project project = resourceChecker.performIfExists(
				projectId, () -> projectService.findById(projectId)).get();
		projectService.delete(projectId, project.getCommunityId());
	}

	Project update(String projectId, ProjectUpdateRequest request) {
		final io.imunity.furms.domain.projects.Project project = resourceChecker.performIfExists(
				projectId, () -> projectService.findById(projectId))
				.get();

		projectService.update(io.imunity.furms.domain.projects.Project.builder()
				.id(projectId)
				.communityId(project.getCommunityId())
				.name(request.name)
				.description(request.description)
				.logo(project.getLogo())
				.acronym(project.getAcronym())
				.researchField(request.researchField)
				.utcStartTime(UTCTimeUtils.convertToUTCTime(request.validity.from))
				.utcEndTime(UTCTimeUtils.convertToUTCTime(request.validity.to))
				.leaderId(converter.convertToPersistentId(new FenixUserId(request.projectLeaderId)))
				.build());

		return projectService.findById(projectId)
				.map(converter::convert)
				.orElseThrow(() -> new ProjectRestNotFoundException("Could not find project " +
						"with specific id"));
	}

	Project create(ProjectCreateRequest request) {
		final String projectId = projectService.create(io.imunity.furms.domain.projects.Project.builder()
				.communityId(request.communityId)
				.name(request.name)
				.description(request.description)
				.acronym(request.acronym)
				.researchField(request.researchField)
				.utcStartTime(UTCTimeUtils.convertToUTCTime(request.validity.from))
				.utcEndTime(UTCTimeUtils.convertToUTCTime(request.validity.to))
				.leaderId(converter.convertToPersistentId(new FenixUserId(request.projectLeaderId)))
				.build());

		return projectService.findById(projectId)
				.map(converter::convert)
				.orElseThrow(() -> new ProjectRestNotFoundException("Could not find project " +
						"with specific id"));
	}

	List<ProjectAllocation> findAllProjectAllocationsByProjectId(String projectId) {
		return resourceChecker.performIfExistsAndMatching(projectId,
					() -> projectAllocationService.findAllWithRelatedObjects(projectId),
					project -> isProjectAdminOrIsProjectInstalledOnUserSites(projectId))
				.stream()
				.map(ProjectAllocation::new)
				.collect(toList());
	}

	ProjectAllocation findByIdAndProjectAllocationId(String projectId, String projectAllocationId) {
		try {
			return resourceChecker.performIfExists(projectId,
					() -> projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId))
					.map(ProjectAllocation::new)
					.get();
		} catch (AccessDeniedException e) {
			return findAllUserInstalledProjects().stream()
					.map(siteInstallation -> projectAllocationService.findAllWithRelatedObjectsBySiteId(siteInstallation.siteId))
					.flatMap(Collection::stream)
					.filter(allocation -> allocation.id.equals(projectAllocationId))
					.findFirst()
					.map(ProjectAllocation::new)
					.orElseThrow(() -> e);
		}
	}

	List<ProjectAllocation> addAllocation(String projectId, ProjectAllocationAddRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Could not create Project Allocation due to empty request body.");
		}
		projectAllocationService.create(
				request.communityId,
				io.imunity.furms.domain.project_allocation.ProjectAllocation.builder()
						.projectId(projectId)
						.communityAllocationId(request.communityAllocationId)
						.name(request.name)
						.amount(request.amount)
						.build());

		return findAllProjectAllocationsByProjectId(projectId);
	}

	private Optional<ProjectWithUsers> findProjectById(String projectId) {
		try {
			return projectService.findById(projectId)
					.map(converter::convertToProjectWithUsers);
		} catch (AccessDeniedException e) {
			final Optional<ProjectWithUsers> project = findAllUserInstalledProjects().stream()
					.filter(siteInstalledProject -> projectId.equals(siteInstalledProject.project.getId()))
					.findFirst()
					.map(converter::convertToProjectWithUsers);
			if (project.isEmpty()) {
				throw e;
			}
			return project;
		}
	}

	private Set<SiteInstalledProjectResolved> findAllUserInstalledProjects() {
		return siteService.findAllOfCurrentUserId().stream()
				.map(site -> projectInstallationsService.findAllSiteInstalledProjectsBySiteId(site.getId()))
				.flatMap(Collection::stream)
				.collect(toSet());
	}

	private boolean isProjectAdminOrIsProjectInstalledOnUserSites(String projectId) {
		try {
			return projectService.hasAdminRights(projectId);
		} catch (AccessDeniedException e) {
			return findAllUserInstalledProjects().stream()
					.anyMatch(siteInstalledProject -> siteInstalledProject.project.getId().equals(projectId));
		}
	}

}
