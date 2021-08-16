/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.cidp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping(value = "/rest-api/v1/cidp", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Central IdP Endpoint",
	description = "API intended for use by Fenix Central IdP, providing access to FURMS user attributes and "
			+ "basic user management.")
public class CentralIdPRestAPIController {
	
	private final CentralIdPRestAPIService service;
	
	CentralIdPRestAPIController(CentralIdPRestAPIService service) {
		this.service = service;
	}

	@Operation(
			summary = "Retrieve user attributes",
			description = "Returns a complete information about a given user, including project membership "
					+ "and global attributes.",
			security = { @SecurityRequirement(name = APIDocConstants.CIDP_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "404", description = "User not found", content = { @Content }) })
	@GetMapping("/user/{fenixUserId}")
	public UserRecordJson getUserRecord(@PathVariable("fenixUserId") String fenixUserId) {
		return service.findUserRecordByFenixId(fenixUserId);
	}

	@Operation(
			summary = "Retrieve user attributes on specific site",
			description = "Returns a complete information about a given user for specific site, " +
					"including project membership and global attributes.",
			security = { @SecurityRequirement(name = APIDocConstants.CIDP_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "404", description = "User or site not found", content = { @Content }) })
	@GetMapping("/user/{fenixUserId}/site/{oauthClientId}")
	public UserRecordJson getUserRecordBySiteId(
			@PathVariable("fenixUserId") String fenixUserId,
			@PathVariable("oauthClientId") String oauthClientId) {
		return service.findUserRecordByFenixIdAndSiteId(fenixUserId, oauthClientId);
	}
	
	@Operation(
			summary = "Set user status",
			security = { @SecurityRequirement(name = APIDocConstants.CIDP_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "404", description = "User not found", content = { @Content }) })
	@PostMapping(value = "/user/{fenixUserId}/status", consumes = {MediaType.APPLICATION_JSON_VALUE})
	public void setUserStatus(
			@PathVariable("fenixUserId") String fenixUserId,
			@RequestBody UserStatusHolder userStatus) {
		service.setUserStatus(fenixUserId, userStatus);
	}

	@Operation(
			summary = "Get user status",
			security = { @SecurityRequirement(name = APIDocConstants.CIDP_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "404", description = "User not found", content = { @Content }) })
	@GetMapping("/user/{fenixUserId}/status")
	public UserStatusHolder getUserStatus(
			@PathVariable("fenixUserId") String fenixUserId) {
		return service.findUserStatusByFenixId(fenixUserId);
	}
}
