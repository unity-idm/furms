/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rest.error.exceptions.ProjectRestNotFoundException;
import io.imunity.furms.utils.UTCTimeUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
class ProjectsRestService {

	private final ProjectService projectService;
	private final CommunityService communityService;
	private final ProjectAllocationService projectAllocationService;
	private final ProjectInstallationsService projectInstallationsService;
	private final ResourceChecker resourceChecker;
	private final ProjectsRestConverter converter;

	ProjectsRestService(ProjectService projectService,
	                    ProjectAllocationService projectAllocationService,
	                    ProjectInstallationsService projectInstallationsService,
	                    ProjectsRestConverter converter,
	                    CommunityService communityService) {
		this.projectService = projectService;
		this.projectAllocationService = projectAllocationService;
		this.resourceChecker = new ResourceChecker(id -> projectService.existsById(new ProjectId(id)));
		this.projectInstallationsService = projectInstallationsService;
		this.converter = converter;
		this.communityService = communityService;
	}

	List<Project> findAll() {
		return Stream.concat(
					projectService.findAllByCurrentUserId().stream(),
					projectInstallationsService.findAllSiteInstalledProjectsOfCurrentUser().stream()
							.map(siteInstalledProject -> siteInstalledProject.project))
				.map(converter::convert)
				.collect(toList());
	}

	ProjectWithUsers findOneById(ProjectId projectId) {
		return resourceChecker.performIfExists(projectId.id, () -> findProjectById(projectId)).get();
	}

	void delete(ProjectId projectId) {
		final io.imunity.furms.domain.projects.Project project = resourceChecker.performIfExists(
				projectId.id, () -> projectService.findById(projectId)).get();
		projectService.delete(projectId, project.getCommunityId());
	}

	Project update(ProjectId projectId, ProjectUpdateRequest request) {
		validProject(request.projectLeaderId, request.validity);
		io.imunity.furms.domain.projects.Project project = resourceChecker.performIfExists(
				projectId.id, () -> projectService.findById(projectId))
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
		validCommunityId(request.communityId);
		validProject(request.projectLeaderId, request.validity);
		ProjectId projectId = projectService.create(io.imunity.furms.domain.projects.Project.builder()
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

	void validCommunityId(String communityId)
	{
		if(communityId == null || communityId.isBlank())
			throw new IllegalArgumentException("CommunityId cannot be null or empty");
		if(!communityService.existsById(new CommunityId(communityId)))
			throw new IllegalArgumentException("CommunityId doesn't exist");
	}

	void validProject(String projectLeaderId, Validity validity)
	{
		if(projectLeaderId == null || projectLeaderId.isBlank())
			throw new IllegalArgumentException("ProjectLeaderId cannot be null or empty");
		if(validity == null)
			throw new IllegalArgumentException("Validity cannot be null");
		if(validity.to == null)
			throw new IllegalArgumentException("Validity.to cannot be null");
		if(validity.from == null)
			throw new IllegalArgumentException("Validity.from cannot be null");
	}

	List<ProjectAllocation> findAllProjectAllocationsByProjectId(ProjectId projectId) {
		return resourceChecker.performIfExistsAndMatching(projectId.id,
					() -> projectAllocationService.findAllWithRelatedObjects(projectId),
					project -> isProjectAdminOrIsProjectInstalledOnUserSites(projectId))
				.stream()
				.map(ProjectAllocation::new)
				.collect(toList());
	}

	ProjectAllocation findByIdAndProjectAllocationId(ProjectId projectId, ProjectAllocationId projectAllocationId) {
		try {
			return resourceChecker.performIfExists(projectId.id,
					() -> projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId))
					.map(ProjectAllocation::new)
					.get();
		} catch (AccessDeniedException e) {
			return projectInstallationsService.findAllSiteInstalledProjectsOfCurrentUser().stream()
					.map(siteInstallation -> projectAllocationService.findAllWithRelatedObjectsBySiteId(siteInstallation.siteId))
					.flatMap(Collection::stream)
					.filter(allocation -> allocation.id.equals(projectAllocationId))
					.findFirst()
					.map(ProjectAllocation::new)
					.orElseThrow(() -> e);
		}
	}

	List<ProjectAllocation> addAllocation(ProjectId projectId, ProjectAllocationAddRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Could not create Project Allocation due to empty request body.");
		}
		projectAllocationService.create(
				new CommunityId(request.communityId),
				io.imunity.furms.domain.project_allocation.ProjectAllocation.builder()
						.projectId(projectId)
						.communityAllocationId(request.communityAllocationId)
						.name(request.name)
						.amount(request.amount)
						.build());

		return findAllProjectAllocationsByProjectId(projectId);
	}

	private Optional<ProjectWithUsers> findProjectById(ProjectId projectId) {
		try {
			return projectService.findById(projectId)
					.map(converter::convertToProjectWithUsers);
		} catch (AccessDeniedException e) {
			final Optional<ProjectWithUsers> project = projectInstallationsService.findAllSiteInstalledProjectsOfCurrentUser()
					.stream()
					.filter(siteInstalledProject -> projectId.equals(siteInstalledProject.project.getId()))
					.findFirst()
					.map(converter::convertToProjectWithUsers);
			if (project.isEmpty()) {
				throw e;
			}
			return project;
		}
	}

	private boolean isProjectAdminOrIsProjectInstalledOnUserSites(ProjectId projectId) {
		try {
			return projectService.hasAdminRights(projectId);
		} catch (AccessDeniedException e) {
			return projectInstallationsService.findAllSiteInstalledProjectsOfCurrentUser().stream()
					.anyMatch(siteInstalledProject -> siteInstalledProject.project.getId().equals(projectId));
		}
	}

}
