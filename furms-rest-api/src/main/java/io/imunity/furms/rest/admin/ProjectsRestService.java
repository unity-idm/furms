/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.project_installation.ProjectInstallationStatusService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.rest.error.exceptions.ProjectRestNotFoundException;
import io.imunity.furms.utils.UTCTimeUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
class ProjectsRestService {

	private final ProjectService projectService;
	private final ProjectAllocationService projectAllocationService;
	private final ProjectInstallationStatusService projectInstallationStatusService;
	private final ResourceAccessService resourceAccessService;
	private final UserService userService;

	public ProjectsRestService(ProjectService projectService,
	                           ProjectAllocationService projectAllocationService,
	                           ProjectInstallationStatusService projectInstallationStatusService,
	                           ResourceAccessService resourceAccessService,
	                           UserService userService) {
		this.projectService = projectService;
		this.projectAllocationService = projectAllocationService;
		this.projectInstallationStatusService = projectInstallationStatusService;
		this.resourceAccessService = resourceAccessService;
		this.userService = userService;
	}

	public List<Project> findAll() {
		return projectService.findAll().stream()
				.map(this::convertToProject)
				.collect(toList());
	}

	public ProjectWithUsers findOneById(String projectId) {
		return projectService.findById(projectId)
				.map(this::convertToProjectWithUsers)
				.orElseThrow(() -> new ProjectRestNotFoundException("Could not find project " +
						"with specific id"));
	}

	public void delete(String projectId) {
		final Optional<io.imunity.furms.domain.projects.Project> project = projectService.findById(projectId);
		if (project.isEmpty()) {
			throw new ProjectRestNotFoundException("Could Not find project to delete");
		}
		projectService.delete(projectId, project.get().getCommunityId());
	}

	public Project update(String projectId, ProjectMutableDefinition request) {
		final io.imunity.furms.domain.projects.Project project = projectService.findById(projectId)
				.orElseThrow(() -> new ProjectRestNotFoundException("Could not find project " +
						"with specific id"));

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
				.leaderId(new PersistentId(request.projectLeader.fenixIdentifier))
				.build());

		return projectService.findById(projectId)
				.map(this::convertToProject)
				.orElseThrow(() -> new ProjectRestNotFoundException("Could not find project " +
						"with specific id"));
	}

	public Project create(ProjectDefinition request) {
		final String projectId = projectService.create(io.imunity.furms.domain.projects.Project.builder()
				.communityId(request.communityId)
				.name(request.name)
				.description(request.description)
				.acronym(request.acronym)
				.researchField(request.researchField)
				.utcStartTime(UTCTimeUtils.convertToUTCTime(request.validity.from))
				.utcEndTime(UTCTimeUtils.convertToUTCTime(request.validity.to))
				.leaderId(new PersistentId(request.projectLeader.fenixIdentifier))
				.build());

		return projectService.findById(projectId)
				.map(this::convertToProject)
				.orElseThrow(() -> new ProjectRestNotFoundException("Could not find project " +
						"with specific id"));
	}

	public List<ProjectAllocation> findAllProjectAllocationsByProjectId(String projectId) {
		return projectAllocationService.findAllWithRelatedObjects(projectId).stream()
				.map(ProjectAllocation::new)
				.collect(toList());
	}

	public ProjectAllocation findByIdAndProjectAllocationId(String projectId, String projectAllocationId) {
		return projectAllocationService.findWithRelatedObjectsByProjectIdAndId(projectId, projectAllocationId)
				.map(ProjectAllocation::new)
				.orElseThrow(() -> new ProjectRestNotFoundException("Could not find project " +
				"with specific id"));
	}

	public List<ProjectAllocation> addAllocation(String projectId, ProjectAllocationDefinition request) {
		if (request == null) {
			throw new IllegalArgumentException("Could not create Project Allocation due to empty request body.");
		}
		projectAllocationService.create(
				request.communityAllocationId.communityId,
				io.imunity.furms.domain.project_allocation.ProjectAllocation.builder()
						.projectId(projectId)
						.communityAllocationId(request.communityAllocationId.allocationId)
						.name(request.name)
						.amount(request.amount.amount)
						.build());

		return findAllProjectAllocationsByProjectId(projectId);
	}

	private Project convertToProject(io.imunity.furms.domain.projects.Project project) {
		return new Project(
				project,
				projectInstallationStatusService.findAllByProjectId(project.getId()).stream().findFirst(),
				findUser(project.getLeaderId()));
	}

	private ProjectWithUsers convertToProjectWithUsers(io.imunity.furms.domain.projects.Project project) {
		return new ProjectWithUsers(
				convertToProject(project),
				new ArrayList<>(resourceAccessService.findAddedUser(project.getId())));
	}

	private User findUser(PersistentId userId) {
		return userService.findById(userId)
				.map(User::new)
				.orElse(null);
	}

}
