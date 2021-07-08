/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.imunity.furms.rest.openapi.APIDocConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/rest-api/v1/projects", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Projects Endpoint", description = "FURMS administration endpoint that provides comprehensive access to Projects "
		+ "as well as exposes basic operations that can be done in context of a Projects")
public class ProjectsRestController {

	private final ProjectsRestService service;

	public ProjectsRestController(ProjectsRestService service) {
		this.service = service;
	}

	/********************************************************************************************
	 * 
	 * Projects CRUD.
	 * 
	 ********************************************************************************************/
	@Operation(
			summary = "Retrieve all projects",
			description = "Returns complete information about all projects including its allocations", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"), })
	@GetMapping()
	public List<Project> getAll() {
		return service.findAll();
	}

	@Operation(
			summary = "Retrieve project information with members",
			description = "Returns complete information about project including its allocations and its members.", 
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Project not found", content = { @Content }) })
	@GetMapping("/{projectId}")
	public ProjectWithUsers get(
			@PathVariable("projectId") String projectId) {
		return service.findOneById(projectId);
	}

	@Operation(
			summary = "Delete project",
			description = "Removes project from community.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Project not found", content = { @Content }) })
	@DeleteMapping("/{projectId}")
	public void delete(
			@PathVariable("projectId") String projectId) {
		service.delete(projectId);
	}

	@Operation(
			summary = "Update project",
			description = "Update particular project.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Project not found", content = { @Content }) })
	@PutMapping("/{projectId}")
	public Project update(
			@PathVariable("projectId") String projectId,
			@RequestBody ProjectMutableDefinition request) {
		return service.update(projectId, request);
	}

	@Operation(
			summary = "Add project",
			description = "Creates project under particular community.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied") })
	@PostMapping()
	public Project add(
			@RequestBody ProjectDefinition request) {
		return service.create(request);
	}

	/********************************************************************************************
	 * 
	 * Project's allocations.
	 * 
	 ********************************************************************************************/
	@Operation(
			summary = "Retrieve all allocations",
			description = "Retrieve all project's allocations information", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Project not found", content = { @Content }) })
	@GetMapping("/{projectId}/allocations")
	public List<ProjectAllocation> getAllocations(
			@PathVariable("projectId") String projectId) {
		return service.findAllProjectAllocationsByProjectId(projectId);
	}

	@Operation(
			summary = "Retrieve allocation information",
			description = "Retrieve project's allocation information",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Project or allocation not found", content = { @Content }) })
	@GetMapping("/{projectId}/allocations/{projectAllocationId}")
	public ProjectAllocation getAllocation(
			@PathVariable("projectId") String projectId,
			@PathVariable("projectAllocationId") String projectAllocationId) {
		return service.findByIdAndProjectAllocationId(projectId, projectAllocationId);
	}

	@Operation(summary = "Create allocation",
			description = "Allocate resources to a project.",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Project not found", content = {
					@Content }) })
	@PostMapping("/{projectId}/allocations")
	public List<ProjectAllocation> addAllocation(
			@PathVariable("projectId") String projectId,
			@RequestBody ProjectAllocationDefinition request) {
		return service.addAllocation(projectId, request);
	}
}
