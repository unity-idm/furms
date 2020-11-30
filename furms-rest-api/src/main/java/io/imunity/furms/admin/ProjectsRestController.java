/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

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

import io.imunity.furms.openapi.APIDocConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

//@formatter:off
@RestController
@RequestMapping(value = "/v1/projects", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Projects Endpoint", description = "FURMS administration endpoint that provides comprehensive access to Projects "
		+ "as well as exposes basic operations that can be done in context of a Projects")
// @formatter:on
public class ProjectsRestController {
	/********************************************************************************************
	 * 
	 * Projects CRUD.
	 * 
	 ********************************************************************************************/
	// @formatter:off
	@Operation(summary = "Retrieve all projects", description = "Returns complete information about all projects including its allocations", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"), })
	// @formatter:on
	@GetMapping()
	public List<Project> getProjects() {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Retrieve project information with members", description = "Returns complete information about project including its allocations and its members.", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Project not found", content = {
					@Content }) })
	// @formatter:on
	@GetMapping("/{projectId}")
	public ProjectWithMembers getProject(@PathVariable("projectId") String projectId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Delete project", description = "Removes project from community.", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Project not found", content = {
					@Content }) })
	// @formatter:on
	@DeleteMapping("/{projectId}")
	public void deleteProject(@PathVariable("projectId") String projectId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Update project", description = "Update particular project.", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Project not found", content = {
					@Content }) })
	// @formatter:on
	@PutMapping("/{projectId}")
	public Project updateProject(@RequestBody ProjectUpdateRequest request) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Add project", description = "Creates project under particular community.", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"), })
	// @formatter:on
	@PostMapping()
	public Project addProject(@RequestBody ProjectAddRequest request) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Project's allocations.
	 * 
	 ********************************************************************************************/
	// @formatter:off
	@Operation(summary = "Retrieve all allocations", description = "Retrieve all project's allocations information", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Project not found", content = {
					@Content }) })
	// @formatter:on
	@GetMapping("/{projectId}/allocations")
	public List<ProjectAllocation> getAllProjectAllocations(
			@PathVariable("projectId") String projectId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Retrieve allocation information", description = "Retrieve project's allocation information", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Project or allocation not found", content = {
					@Content }) })
	// @formatter:on
	@GetMapping("/{projectId}/allocations/{projectAllocationId}")
	public ProjectAllocation getProjectAllocation(@PathVariable("projectId") String projectId,
			@PathVariable("projectAllocationId") String projectAllocationId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Create allocation", description = "Create particular allocation for given project.", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Project not found", content = {
					@Content }) })
	// @formatter:on
	@PostMapping("/{projectId}/allocations")
	public List<ProjectAllocation> getCreateAllocation(
			@PathVariable("projectId") String projectId,
			@RequestBody ProjectAllocationAddRequest request) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}
}
