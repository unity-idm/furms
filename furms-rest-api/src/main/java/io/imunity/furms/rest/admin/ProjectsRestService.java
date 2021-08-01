/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.rest.error.exceptions.ProjectRestNotFoundException;
import io.imunity.furms.utils.UTCTimeUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
class ProjectsRestService {

	private final ProjectService projectService;
	private final ProjectAllocationService projectAllocationService;
	private final ResourceChecker resourceChecker;
	private final ProjectsRestConverter converter;

	ProjectsRestService(ProjectService projectService,
	                    ProjectAllocationService projectAllocationService,
	                    ProjectsRestConverter converter) {
		this.projectService = projectService;
		this.projectAllocationService = projectAllocationService;
		this.resourceChecker = new ResourceChecker(projectService::existsById);
		this.converter = converter;
	}

	List<Project> findAll() {
		return projectService.findAll().stream()
				.map(converter::convert)
				.collect(toList());
	}

	ProjectWithUsers findOneById(String projectId) {
		return resourceChecker.performIfExists(projectId, () -> projectService.findById(projectId))
				.map(converter::convertToProjectWithUsers)
				.get();
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
				.leaderId(new PersistentId(request.projectLeaderId))
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
				.leaderId(new PersistentId(request.projectLeaderId))
				.build());

		return projectService.findById(projectId)
				.map(converter::convert)
				.orElseThrow(() -> new ProjectRestNotFoundException("Could not find project " +
						"with specific id"));
	}

	List<ProjectAllocation> findAllProjectAllocationsByProjectId(String projectId) {
		return resourceChecker.performIfExists(projectId,
					() -> projectAllocationService.findAllWithRelatedObjects(projectId))
				.stream()
				.map(ProjectAllocation::new)
				.collect(toList());
	}

	ProjectAllocation findByIdAndProjectAllocationId(String projectId, String projectAllocationId) {
		return resourceChecker.performIfExists(projectId,
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

}
