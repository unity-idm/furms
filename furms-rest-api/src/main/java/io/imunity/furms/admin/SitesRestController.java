/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.imunity.furms.openapi.APIDocConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/v1/sites", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Site Endpoint",
	description = "FURMS administration endpoint that provides comprehensive access to Sites "
			+ "as well as exposes basic operations that can be done in a site context.")
public class SitesRestController {

	@Operation(summary = "Retrieve all sites",
		description = "Returns complete information about all sites including with its all allocations"
				+ "resource types, services and policies.",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"), })
	@GetMapping()
	public List<Site> getSites() {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(summary = "Retrieve particular site information",
		description = "Returns complete information about site including its all allocations, "
				+ "resource types, services and policyes",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}")
	public Site getSite(@PathVariable("siteId") String siteId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Site's credits.
	 * 
	 ********************************************************************************************/

	@Operation(summary = "Retrieve all allocations information",
		description = "Retrieve all allocations information.",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/credits")
	public List<ResourceCredit> getResourceCredits(@PathVariable("siteId") String siteId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(summary = "Retrieve particular site's allocation information",
		description = "Retrieve particular site's allocation information",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404",
				description = "Site or allocation not found",
				content = { @Content }) })
	@GetMapping("/{siteId}/credits/{siteAllocationId}")
	public ResourceCredit getResourceCredit(@PathVariable("siteId") String siteId,
			@PathVariable("siteAllocationId") String siteAllocationId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Site's resource types.
	 * 
	 ********************************************************************************************/

	@Operation(summary = "Retrieve all site's resource types information",
		description = "Retrieve all site's resource types information",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/resourceTypes")
	public List<ResourceType> getResourceTypes(@PathVariable("siteId") String siteId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(summary = "Retrieve particular site's resource type information",
		description = "Retrieve particular site's resource type information",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404",
				description = "Site or resource type not found",
				content = { @Content }) })
	@GetMapping("/{siteId}/resourceTypes/{resourceTypeIds}")
	public ResourceType getResourceType(@PathVariable("siteId") String siteId,
			@PathVariable("resourceTypeIds") String resourceTypeIds) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Site's services.
	 * 
	 ********************************************************************************************/

	@Operation(summary = "Retrieve all site's services information",
		description = "Retrieve all site's services information",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/services")
	public List<Service> getServices(@PathVariable("siteId") String siteId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(summary = "Retrieve particular site's service information",
		description = "Retrieve particular site's service information",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404",
				description = "Site or services not found",
				content = { @Content }) })
	@GetMapping("/{siteId}/services/{servicesId}")
	public Service getService(@PathVariable("siteId") String siteId,
			@PathVariable("servicesId") String servicesId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Site's policies.
	 * 
	 ********************************************************************************************/

	@Operation(summary = "Retrieve all site's policies information",
		description = "Retrieve all site's policies information",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/policies")
	public List<Policy> getPolicies(@PathVariable("siteId") String siteId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(summary = "Retrieve particular site's policiy information",
		description = "Retrieve particular site's policiy information",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404",
				description = "Site or policy not found",
				content = { @Content }) })
	@GetMapping("/{siteId}/policies/{policyId}")
	public Policy getPolicy(@PathVariable("siteId") String siteId, @PathVariable("policyId") String servicesId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Site's policy acceptance.
	 * 
	 ********************************************************************************************/

	@Operation(summary = "Retrieve site policies acceptance",
		description = "Returns list of all site's users with policies and status whether given policy "
				+ "is accepcted or not",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/policyAcceptances")
	public List<PolicyAcceptance> getPoliciesAcceptance(@PathVariable("siteId") String siteId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(summary = "Accepts sites policy on behalf of user",
		description = "Performs opertion of accepting the policy on behalf of given user",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404",
				description = "Site, policy or user not found",
				content = { @Content }) })
	@PostMapping("/{siteId}/policies/{policyId}/acceptance/{fenixUserId}")
	public List<PolicyAcceptance> addPolicyAcceptance(@PathVariable("siteId") String siteId,
			@PathVariable("policyId") String policyId, @PathVariable("fenixUserId") String fenixUserId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Site's protocol messages.
	 * 
	 ********************************************************************************************/

	@Operation(summary = "Retrieve site's protocol messages",
		description = "Returns all pending site's messages exchanged with site agents.",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/protocolMessages")
	public List<ProtocolMessage> getProtocolMessages(@PathVariable("siteId") String siteId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(summary = "Drop pending protocol message",
		description = "Removes the protocol message, it won't be possible to retry it, "
				+ "FURMS will assume it has timed out.",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404",
				description = "Site or message not found",
				content = { @Content }) })
	@DeleteMapping("/{siteId}/protocolMessages/{messageId}")
	public void dropProtocolMessage(@PathVariable("siteId") String siteId,
			@PathVariable("messageId") String messageId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(summary = "Retry the protocol message",
		description = "Retry sending protocol message to a site agent.",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404",
				description = "Site or message not found",
				content = { @Content }) })
	@PostMapping("/{siteId}/protocolMessages/{messageId}")
	public void retryProtocolMessage(@PathVariable("siteId") String siteId,
			@PathVariable("messageId") String messageId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}
	
	
	/********************************************************************************************
	 * 
	 * Site installed projects
	 * 
	 ********************************************************************************************/
	@Operation(summary = "Retrieve project installations",
			description = "Returns list of projects that should be installed on the site with "
					+ "details of installation status",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/projectInstallations")
	public List<ProjectInstallation> getInstalledProjects(@PathVariable("siteId") String siteId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}
	
	
	@Operation(summary = "Retrieve all users that should have access to a site",
			description = "Returns list of users that have access to the site through at least "
					+ "one of their projects. SSH key of each user is provided as well.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/users")
	public List<SiteUser> getSiteUsers(@PathVariable("siteId") String siteId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}
	
	@Operation(summary = "Retrieve all FURMS allocations at the site", 
			description = "Retrieve all project allocations at the site as assigned in FURMS", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = {
					@Content }) })
	@GetMapping("/{siteId}/furmsAllocations")
	public List<ProjectAllocation> getAllocations(
			@PathVariable("siteId") String siteId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(summary = "Retrieve FURMS allocations at the site of a project", 
			description = "Retrieve given project allocations at the site as assigned in FURMS", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or project not found", content = {
					@Content }) })
	@GetMapping("/{siteId}/furmsAllocations/{projectId}")
	public List<ProjectAllocation> getProjectAllocations(
			@PathVariable("siteId") String siteId, @PathVariable("projectId") String projectId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	
	
	@Operation(summary = "Retrieve site allocated resources to projects", 
			description = "Retrieve all project allocations at the site as assigned by the site", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = {
					@Content }) })
	@GetMapping("/{siteId}/siteAllocations")
	public List<SiteAllocatedResources> getSiteAllocatedResources(
			@PathVariable("siteId") String siteId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(summary = "Retrieve site allocated resources to a project", 
			description = "Retrieve given project allocations at the site as assigned by the site", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or project not found", content = {
					@Content }) })
	@GetMapping("/{siteId}/siteAllocations/{projectId}")
	public List<SiteAllocatedResources> getSiteAllocatedProjectResources(
			@PathVariable("siteId") String siteId, @PathVariable("projectId") String projectId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	
	@Operation(summary = "Retrieve cumulative project resource consumption", 
			description = "Retrieve cumultaive resoruce consumption recorded by FURMS for "
					+ "a given project on a site.", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or project not found", content = {
					@Content }) })
	@GetMapping("/{siteId}/cumulativeResourcesConsumption/{projectId}")
	public List<ProjectCumulativeResourceConsumption> getProjectCumulativeResourceConsumption(
			@PathVariable("siteId") String siteId, @PathVariable("projectId") String projectId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Operation(summary = "Retrieve project resource usage records", 
			description = "Retrieve individual resoruce usage records, stored by FURMS for "
					+ "a given project on a site. Records have per-user granularity. "
					+ "Caller can limit time range of records.", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or project not found", content = {
					@Content }) })
	@GetMapping("/{siteId}/usageRecords/{projectId}")
	public List<ProjectUsageRecord> getProjectResourceUsageRecords(
			@PathVariable("siteId") String siteId, 
			@PathVariable("projectId") String projectId,
			@RequestParam(name = "from", required = false) ZonedDateTime from, 
			@RequestParam(name = "until", required = false) ZonedDateTime until) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

}

