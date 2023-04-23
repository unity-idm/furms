/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rest.openapi.APIDocConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/rest-api/v1/sites", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Site Endpoint",
	description = "FURMS administration endpoint that provides comprehensive access to information about sites "
			+ "as well as exposes basic operations that can be done in a site context.")
public class SitesRestController {

	private final SitesRestService sitesRestService;

	SitesRestController(SitesRestService sitesRestService) {
		this.sitesRestService = sitesRestService;
	}

	@Operation(
			summary = "Retrieve all sites",
			description = "Returns complete information about all sites including their allocations, "
				+ "resource types, services and policies.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"), })
	@GetMapping()
	public List<Site> getSites() {
		return sitesRestService.findAll();
	}

	@Operation(
			summary = "Retrieve particular site information",
			description = "Returns complete information about a site, including its all allocations, "
				+ "resource types, services and policies",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}")
	public Site getSite(
			@PathVariable("siteId") String siteId) {
		return sitesRestService.findOneById(new SiteId(siteId));
	}

	@Operation(
		summary = "Retrieve particular site availability",
		description = "Returns availability status (AVAILABLE or UNAVAILABLE)",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successful operation"),
		@ApiResponse(responseCode = "403", description = "Permission denied"),
		@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/availability")
	public SiteAvailability getSiteAvailability(@PathVariable("siteId") String siteId) {
		return sitesRestService.getStatusById(new SiteId(siteId));
	}

	/********************************************************************************************
	 * 
	 * Site's credits.
	 * 
	 ********************************************************************************************/

	@Operation(
			summary = "Retrieve credits",
			description = "Retrieve all resource credits of a site.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/credits")
	public List<ResourceCredit> getResourceCredits(
			@PathVariable("siteId") String siteId) {
		return sitesRestService.findAllResourceCreditsBySiteId(new SiteId(siteId));
	}

	@Operation(
			summary = "Retrieve resource credit",
			description = "Retrieve details of a given resource credit.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or allocation not found", content = { @Content }) })
	@GetMapping("/{siteId}/credits/{creditId}")
	public ResourceCredit getResourceCredit(
			@PathVariable("siteId") String siteId,
			@PathVariable("creditId") String creditId) {
		return sitesRestService.findResourceCreditBySiteIdAndId(new SiteId(siteId), new ResourceCreditId(creditId));
	}

	/********************************************************************************************
	 * 
	 * Site's resource types.
	 * 
	 ********************************************************************************************/

	@Operation(
			summary = "Retrieve all resource types",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/resourceTypes")
	public List<ResourceType> getResourceTypes(
			@PathVariable("siteId") String siteId) {
		return sitesRestService.findAllResourceTypesBySiteId(new SiteId(siteId));
	}

	@Operation(
			summary = "Retrieve a given resource type",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or resource type not found", content = { @Content }) })
	@GetMapping("/{siteId}/resourceTypes/{resourceTypeId}")
	public ResourceType getResourceType(
			@PathVariable("siteId") String siteId,
			@PathVariable("resourceTypeId") String resourceTypeId) {
		return sitesRestService.findResourceTypesBySiteIdAndId(new SiteId(siteId), new ResourceTypeId(resourceTypeId));
	}

	/********************************************************************************************
	 * 
	 * Site's services.
	 * 
	 ********************************************************************************************/

	@Operation(
			summary = "Retrieve all services",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/services")
	public List<InfraService> getServices(
			@PathVariable("siteId") String siteId) {
		return sitesRestService.findAllServicesBySiteId(new SiteId(siteId));
	}

	@Operation(
			summary = "Retrieve a given site service",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or service not found", content = { @Content }) })
	@GetMapping("/{siteId}/services/{serviceId}")
	public InfraService getService(
			@PathVariable("siteId") String siteId,
			@PathVariable("serviceId") String serviceId) {
		return sitesRestService.findServiceBySiteIdAndId(new SiteId(siteId), new InfraServiceId(serviceId));
	}

	/********************************************************************************************
	 * 
	 * Site's policies.
	 * 
	 ********************************************************************************************/

	@Operation(
			summary = "Retrieve site policies",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/policies")
	public List<Policy> getPolicies(
			@PathVariable("siteId") String siteId) {
		return sitesRestService.findAllPolicies(new SiteId(siteId));
	}

	@Operation(
			summary = "Retrieve a given site policy",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or policy not found", content = { @Content }) })
	@GetMapping("/{siteId}/policies/{policyId}")
	public Policy getPolicy(
			@PathVariable("siteId") String siteId,
			@PathVariable("policyId") String policyId) {
		return sitesRestService.findPolicy(new SiteId(siteId), policyId);
	}

	/********************************************************************************************
	 * 
	 * Site's policy acceptance.
	 * 
	 ********************************************************************************************/

	@Operation(
			summary = "Retrieve site policies acceptance",
			description = "Returns list of all site's users with policies and status whether given policy "
				+ "is accepcted or not.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/policyAcceptances")
	public List<PolicyAcceptance> getPoliciesAcceptance(
			@PathVariable("siteId") String siteId) {
		return sitesRestService.findAllPoliciesAcceptances(new SiteId(siteId));
	}

	@Operation(
			summary = "Accept sites policy on behalf of user",
			description = "Performs operation of accepting the policy on behalf of given user.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site, policy or user not found", content = { @Content }) })
	@PostMapping("/{siteId}/policies/{policyId}/acceptance/{fenixUserId}")
	public List<PolicyAcceptance> addPolicyAcceptance(
			@PathVariable("siteId") String siteId,
			@PathVariable("policyId") String policyId, 
			@PathVariable("fenixUserId") String fenixUserId) {
		return sitesRestService.addPolicyAcceptance(new SiteId(siteId), policyId, fenixUserId);
	}

	/********************************************************************************************
	 * 
	 * Site's protocol messages.
	 * 
	 ********************************************************************************************/

	@Operation(
			summary = "Retrieve pending requests",
			description = "Returns all pending site's requests, which were sent to the site agent.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/protocolMessages")
	public List<ProtocolMessage> getProtocolMessages(
			@PathVariable("siteId") String siteId) {
		return sitesRestService.getProtocolMessages(new SiteId(siteId));
	}

	@Operation(
			summary = "Drop pending request",
			description = "Removes a pending protocol request. It won't be possible to retry the removed request, "
				+ "FURMS will assume it has timed out.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or request not found", content = { @Content }) })
	@DeleteMapping("/{siteId}/protocolMessages/{messageId}")
	public void dropProtocolMessage(
			@PathVariable("siteId") String siteId,
			@PathVariable("messageId") String messageId) {
		sitesRestService.dropProtocolMessage(new SiteId(siteId), messageId);
	}

	@Operation(
			summary = "Retry site request",
			description = "Retry sending a request to a site agent.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or message not found", content = { @Content }) })
	@PostMapping("/{siteId}/protocolMessages/{messageId}")
	public void retryProtocolMessage(
			@PathVariable("siteId") String siteId,
			@PathVariable("messageId") String messageId) {
		sitesRestService.retryProtocolMessage(new SiteId(siteId), messageId);
	}
	
	
	/********************************************************************************************
	 * 
	 * Site installed projects
	 * 
	 ********************************************************************************************/
	@Operation(
			summary = "Retrieve project installations",
			description = "Returns list of projects that should be installed on the site with "
					+ "details of installation status",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/projectInstallations")
	public List<ProjectInstallation> getInstalledProjects(
			@PathVariable("siteId") String siteId) {
		return sitesRestService.findAllProjectInstallationsBySiteId(new SiteId(siteId));
	}
	
	
	@Operation(
			summary = "Retrieve all users that should have access to a site",
			description = "Returns list of users that have access to the site through at least "
					+ "one of their projects. SSH key of each user is provided as well.",
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/users")
	public List<SiteUser> getSiteUsers(
			@PathVariable("siteId") String siteId) {
		return sitesRestService.findAllSiteUsersBySiteId(new SiteId(siteId));
	}


	@Operation(
		summary = "Retrieve site user",
		description = "Returns a specified user by Fenix id, assuming that this user has access to the site.",
		security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successful operation"),
		@ApiResponse(responseCode = "403", description = "Permission denied"),
		@ApiResponse(responseCode = "404", description = "Site not found or user not found", content = { @Content }) })
	@GetMapping("/{siteId}/users/{userId}")
	public SiteUser getSiteUsers(
		@PathVariable("siteId") String siteId, @PathVariable("userId") String userId) {
		return sitesRestService.findSiteUserByUserIdAndSiteId(new FenixUserId(userId), new SiteId(siteId));
	}

	/********************************************************************************************
	 * 
	 * Site allocations of projects and resource consumption
	 * 
	 ********************************************************************************************/
	
	@Operation(
			summary = "Retrieve all FURMS allocations at the site",
			description = "Retrieve all project allocations at the site as assigned in FURMS", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/furmsAllocations")
	public List<ProjectAllocation> getAllocations(
			@PathVariable("siteId") String siteId) {
		return sitesRestService.findAllProjectAllocationsBySiteId(new SiteId(siteId));
	}

	@Operation(
			summary = "Retrieve FURMS allocations at the site of a project",
			description = "Retrieve given project allocations at the site as assigned in FURMS", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or project not found", content = { @Content }) })
	@GetMapping("/{siteId}/furmsAllocations/{projectId}")
	public List<ProjectAllocation> getProjectAllocations(
			@PathVariable("siteId") String siteId,
			@PathVariable("projectId") String projectId) {
		return sitesRestService.findAllProjectAllocationsBySiteIdAndProjectId(new SiteId(siteId),
			new ProjectId(projectId));
	}
	
	@Operation(
			summary = "Retrieve site allocated resources to projects",
			description = "Retrieve all project allocations at the site as assigned by the site", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = { @Content }) })
	@GetMapping("/{siteId}/siteAllocations")
	public List<SiteAllocatedResources> getSiteAllocatedResources(
			@PathVariable("siteId") String siteId) {
		return sitesRestService.findAllSiteAllocatedResourcesBySiteId(new SiteId(siteId));
	}

	@Operation(
			summary = "Retrieve site allocated resources to a project",
			description = "Retrieve given project allocations at the site as assigned by the site", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or project not found", content = { @Content }) })
	@GetMapping("/{siteId}/siteAllocations/{projectId}")
	public List<SiteAllocatedResources> getSiteAllocatedProjectResources(
			@PathVariable("siteId") String siteId,
			@PathVariable("projectId") String projectId) {
		return sitesRestService.findAllSiteAllocatedResourcesBySiteIdAndProjectId(new SiteId(siteId),
			new ProjectId(projectId));
	}
	
	@Operation(
			summary = "Retrieve cumulative project resource consumption",
			description = "Retrieve cumultaive resoruce consumption recorded by FURMS for "
					+ "a given project on a site.", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or project not found", content = { @Content }) })
	@GetMapping("/{siteId}/cumulativeResourcesConsumption/{projectId}")
	public List<ProjectCumulativeResourceConsumption> getProjectCumulativeResourceConsumption(
			@PathVariable("siteId") String siteId,
			@PathVariable("projectId") String projectId) {
		return sitesRestService.findAllProjectCumulativeResourceConsumptionBySiteIdAndProjectId(new SiteId(siteId),
			new ProjectId(projectId));
	}

	@Operation(
			summary = "Retrieve project resource usage records",
			description = "Retrieve individual resoruce usage records, stored by FURMS for "
					+ "a given project on a site. Records have per-user granularity. "
					+ "Caller can limit time range of records.", 
			security = { @SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME) })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or project not found", content = { @Content }) })
	@GetMapping("/{siteId}/usageRecords/{projectId}")
	public List<ProjectUsageRecord> getProjectResourceUsageRecords(
			@PathVariable("siteId") String siteId, 
			@PathVariable("projectId") String projectId,
			@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime from,
			@RequestParam(name = "until", required = false) @DateTimeFormat(iso = DATE_TIME) ZonedDateTime until) {
		return sitesRestService.findAllProjectUsageRecordBySiteIdAndProjectIdInPeriod(new SiteId(siteId),
			new ProjectId(projectId), from, until);
	}

}

