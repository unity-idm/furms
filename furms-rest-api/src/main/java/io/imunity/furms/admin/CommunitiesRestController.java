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
@RequestMapping(value = "/v1/communities", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Communities Endpoint", description = "FURMS administration endpoint that provides comprehensive access to Communities "
		+ "as well as exposes basic operations that can be done in context of a Community")
// @formatter:on
public class CommunitiesRestController {
	// @formatter:off
	@Operation(summary = "Retrieve all communities", description = "Returns complete information about all communities including its allocations", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"), })
	// @formatter:on
	@GetMapping()
	public List<Community> getCommunities() {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Retrieve particular community information", description = "Returns complete information about community including its allocations", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community not found", content = {
					@Content }) })
	// @formatter:on
	@GetMapping("/{communityId}")
	public Community getCommunity(@PathVariable("communityId") String communityId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Community's allocations.
	 * 
	 ********************************************************************************************/
	// @formatter:off
	@Operation(summary = "Retrieve all allocations", description = "Retrieve all community's allocations information", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community not found", content = {
					@Content }) })
	// @formatter:on
	@GetMapping("/{communityId}/allocations")
	public List<CommunityAllocation> getAllCommunityAllocations(
			@PathVariable("communityId") String communityId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Retrieve particular allocation information", description = "Retrieve particular community's allocation information", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community or allocation not found", content = {
					@Content }) })
	// @formatter:on
	@GetMapping("/{communityId}/allocations/{communityAllocationId}")
	public CommunityAllocation getCommunityAllocation(
			@PathVariable("communityId") String communityId,
			@PathVariable("communityAllocationId") String communityAllocationId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Create allocation", description = "Create particular allocation for given community.", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community not found", content = {
					@Content }) })
	// @formatter:on
	@PostMapping("/{communityId}/allocations")
	public List<CommunityAllocation> getCreateAllocation(
			@PathVariable("communityId") String communityId,
			@RequestBody CommunityAllocationAddRequest request) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Groups CRUD.
	 * 
	 ********************************************************************************************/
	// @formatter:off
	@Operation(summary = "Retrieve all groups", description = "Returns information about all groups in community", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community not found", content = {
					@Content }) })
	// @formatter:on
	@GetMapping("/{communityId}/groups")
	public List<Group> getGroups(@PathVariable("communityId") String communityId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Get group with members", description = "Returns complete information about gropu including membership information", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community or Group not found", content = {
					@Content }) })
	// @formatter:on
	@GetMapping("/{communityId}/groups/{groupId}")
	public GroupWithMembers getGroup(@PathVariable("communityId") String communityId,
			@PathVariable("groupId") String groupId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Delete group", description = "Removes group from community.", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community or Group not found", content = {
					@Content }) })
	// @formatter:on
	@DeleteMapping("/{communityId}/groups/{groupId}")
	public void deleteGroup(@PathVariable("communityId") String communityId,
			@PathVariable("groupId") String groupId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Update group", description = "Update particular group.", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community or Group not found", content = {
					@Content }) })
	// @formatter:on
	@PutMapping("/{communityId}/groups/{groupId}")
	public Group updateGroup(@RequestBody GroupUpdateRequest request) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	// @formatter:off
	@Operation(summary = "Add group", description = "Creates group in community.", security = {
			@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Community not found", content = {
					@Content }) })
	// @formatter:on
	@PostMapping("/{communityId}/groups")
	public Group addGroup(@RequestBody GroupAddRequest request) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

}
