/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
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

//@formatter:off
@RestController
@RequestMapping(value = "/rest-api/v1/communities", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Communities Endpoint",
	description = "FURMS administration endpoint that provides comprehensive access to communities "
			+ "as well as exposes basic operations that can be done in the context of communities.")
public class CommunityRestController {

	private final CommunityRestService restService;

	public CommunityRestController(CommunityRestService restService) {
		this.restService = restService;
	}

	@Operation(
			summary = "Retrieve all communities",
			description = "Returns complete information about all communities including its allocations",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"), })
	@GetMapping
	public List<Community> getCommunities() {
		return restService.findAll();
	}

	@Operation(
			summary = "Retrieve particular community",
			description = "Returns complete information about community including its allocations",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community not found", content = { @Content }) })
	@GetMapping("/{communityId}")
	public Community getCommunity(@PathVariable("communityId") String communityId) {
		return restService.findOneById(communityId);
	}

	@Operation(
			summary = "Get all community projects",
			description = "Returns complete information about projects related to specific community.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community not found", content = { @Content }) })
	@GetMapping("/{communityId}/projects")
	public List<Project> getCommunityProjects(@PathVariable("communityId") String communityId) {
		return restService.findAllProjectsByCommunityId(communityId);
	}

	/********************************************************************************************
	 * 
	 * Community's allocations.
	 * 
	 ********************************************************************************************/

	@Operation(
			summary = "Retrieve all allocations",
			description = "Retrieve all allocations of a community.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community not found", content = { @Content }) })
	@GetMapping("/{communityId}/allocations")
	public List<CommunityAllocation> getAllocations(@PathVariable("communityId") String communityId) {
		return restService.findAllocationByCommunityId(communityId);
	}

	@Operation(
			summary = "Retrieve allocation",
			description = "Retrieve a particular allocation of a community.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community or allocation not found",
					content = { @Content }) })
	@GetMapping("/{communityId}/allocations/{communityAllocationId}")
	public CommunityAllocation getAllocation(@PathVariable("communityId") String communityId,
	                                         @PathVariable("communityAllocationId") String communityAllocationId) {
		return restService.findAllocationByIdAndCommunityId(communityAllocationId, communityId);
	}

	@Operation(
			summary = "Create allocation",
			description = "Create a new allocation for a community.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community not found", content = { @Content }) })
	@PostMapping("/{communityId}/allocations")
	public List<CommunityAllocation> addAllocation(@PathVariable("communityId") String communityId,
	                                               @RequestBody CommunityAllocationAddRequest request) {
		return restService.addAllocation(communityId, request);
	}

	/********************************************************************************************
	 * 
	 * Groups CRUD.
	 * 
	 ********************************************************************************************/

	@Operation(
			summary = "Retrieve all groups",
			description = "Returns information about all groups in a community.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community not found", content = { @Content }) })
	@GetMapping("/{communityId}/groups")
	public List<Group> getGroups(@PathVariable("communityId") String communityId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(
			summary = "Get group with members",
			description = "Returns complete information about a group, including membership information.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404",
				description = "Community or group not found",
				content = { @Content }) })
	@GetMapping("/{communityId}/groups/{groupId}")
	public GroupWithMembers getGroup(@PathVariable("communityId") String communityId,
	                                 @PathVariable("groupId") String groupId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(
			summary = "Delete group",
			description = "Removes a group from a community.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community or group not found", content = { @Content }) })
	@DeleteMapping("/{communityId}/groups/{groupId}")
	public void deleteGroup(@PathVariable("communityId") String communityId,
	                        @PathVariable("groupId") String groupId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(
			summary = "Update group",
			description = "Update mutable elements of a group.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community or group not found", content = { @Content }) })
	@PutMapping("/{communityId}/groups/{groupId}")
	public Group updateGroup(@PathVariable("communityId") String communityId,
	                         @PathVariable("groupId") String groupId,
	                         @RequestBody GroupDefinitionRequest request) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(
			summary = "Add group",
			description = "Creates a new group in a community.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community not found", content = { @Content }) })
	@PostMapping("/{communityId}/groups")
	public Group addGroup(@RequestBody GroupDefinitionRequest request) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

}
