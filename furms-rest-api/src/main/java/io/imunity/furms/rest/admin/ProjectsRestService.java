/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.rest.error.exceptions.ProjectRestNotFoundException;
import io.imunity.furms.utils.UTCTimeUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

@Service
class ProjectsRestService {

	private final ProjectService projectService;
	private final ProjectAllocationService projectAllocationService;
	private final ResourceAccessService resourceAccessService;
	private final UserService userService;

	ProjectsRestService(ProjectService projectService,
	                           ProjectAllocationService projectAllocationService,
	                           ResourceAccessService resourceAccessService,
	                           UserService userService) {
		this.projectService = projectService;
		this.projectAllocationService = projectAllocationService;
		this.resourceAccessService = resourceAccessService;
		this.userService = userService;
	}

	List<Project> findAll() {
		return projectService.findAll().stream()
				.map(this::convertToProject)
				.collect(toList());
	}

	ProjectWithUsers findOneById(String projectId) {
		return performIfExists(projectId, () -> projectService.findById(projectId))
				.map(this::convertToProjectWithUsers)
				.get();
	}

	void delete(String projectId) {
		final io.imunity.furms.domain.projects.Project project = performIfExists(
				projectId, () -> projectService.findById(projectId)).get();
		projectService.delete(projectId, project.getCommunityId());
	}

	Project update(String projectId, ProjectUpdateRequest request) {
		final io.imunity.furms.domain.projects.Project project = performIfExists(
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
				.leaderId(new PersistentId(request.projectLeaderId))
				.build());

		return projectService.findById(projectId)
				.map(this::convertToProject)
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
				.leaderId(new PersistentId(request.projectLeaderId))
				.build());

		return projectService.findById(projectId)
				.map(this::convertToProject)
				.orElseThrow(() -> new ProjectRestNotFoundException("Could not find project " +
						"with specific id"));
	}

	List<ProjectAllocation> findAllProjectAllocationsByProjectId(String projectId) {
		return performIfExists(projectId, () -> projectAllocationService.findAllWithRelatedObjects(projectId))
				.stream()
				.map(ProjectAllocation::new)
				.collect(toList());
	}

	ProjectAllocation findByIdAndProjectAllocationId(String projectId, String projectAllocationId) {
		return performIfExists(projectId,
					() -> projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId))
				.map(ProjectAllocation::new)
				.get();
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

	private <T> T performIfExists(String projectId, Supplier<T> action) {
		return ResourceExistsWrapper.performIfExists(() -> projectService.existsById(projectId), action);
	}

	private ProjectWithUsers convertToProjectWithUsers(io.imunity.furms.domain.projects.Project project) {
		return new ProjectWithUsers(
				convertToProject(project),
				new ArrayList<>(resourceAccessService.findAddedUser(project.getId())));
	}

	private Project convertToProject(io.imunity.furms.domain.projects.Project project) {
		return new Project(project, findUser(project.getLeaderId()));
	}

	private User findUser(PersistentId userId) {
		return userService.findById(userId)
				.map(User::new)
				.orElse(null);
	}

}
