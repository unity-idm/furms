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
@RequestMapping(value = "/v1/sites", produces = APPLICATION_JSON_VALUE)
@Tag(
		name        = "Site Endpoint", 
		description = "FURMS administration endpoint that provides comprehensive access to Sites "
				+ "as well as exposes basic operations that can be done in context of a Site")
//@formatter:on
public class SitesRestController
{
	//@formatter:off
	@Operation(
			summary     = "Retrieve all sites",
			description = "Returns complete information about all sites including with its all allocations"
						+ "resource types, services and policyes",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
	})
	//@formatter:on
	@GetMapping()
	public List<Site> getSites()
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	//@formatter:off
	@Operation(
			summary     = "Retrieve particular site information",
			description = "Returns complete information about site including its all allocations, "
						+ "resource types, services and policyes",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = {@Content})
	})
	//@formatter:on
	@GetMapping("/{siteId}")
	public Site getSite(@PathVariable("siteId") String siteId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Site's allocations.
	 * 
	 ********************************************************************************************/
	//@formatter:off
	@Operation(
			summary     = "Retrieve all site's allocations information",
			description = "Retrieve all site's allocations information",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = {@Content})
	})
	//@formatter:on
	@GetMapping("/{siteId}/allocations")
	public List<SiteAllocation> getAllSiteAllocations(@PathVariable("siteId") String siteId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	//@formatter:off
	@Operation(
			summary     = "Retrieve particular site's allocation information",
			description = "Retrieve particular site's allocation information",
					security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
			)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or allocation not found", content = {@Content})
	})
	//@formatter:on
	@GetMapping("/{siteId}/allocations/{siteAllocationId}")
	public SiteAllocation getSiteAllocation(@PathVariable("siteId") String siteId,
			@PathVariable("siteAllocationId") String siteAllocationId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Site's resource types.
	 * 
	 ********************************************************************************************/
	//@formatter:off
	@Operation(
			summary     = "Retrieve all site's resource types information",
			description = "Retrieve all site's resource types information",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = {@Content})
	})
	//@formatter:on
	@GetMapping("/{siteId}/resourceTypes")
	public List<ResourceType> getAllSiteRecourceTypes(@PathVariable("siteId") String siteId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	//@formatter:off
	@Operation(
			summary     = "Retrieve particular site's resource type information",
			description = "Retrieve particular site's resource type information",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or resource type not found", content = {@Content})
	})
	//@formatter:on
	@GetMapping("/{siteId}/resourceTypes/{resourceTypeIds}")
	public ResourceType getSiteRecourceType(@PathVariable("siteId") String siteId,
			@PathVariable("resourceTypeIds") String resourceTypeIds)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Site's services.
	 * 
	 ********************************************************************************************/
	//@formatter:off
	@Operation(
			summary     = "Retrieve all site's services information",
			description = "Retrieve all site's services information",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = {@Content})
	})
	//@formatter:on
	@GetMapping("/{siteId}/services")
	public List<Service> getAllServices(@PathVariable("siteId") String siteId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	//@formatter:off
	@Operation(
			summary     = "Retrieve particular site's service information",
			description = "Retrieve particular site's service information",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or services not found", content = {@Content})
	})
	//@formatter:on
	@GetMapping("/{siteId}/services/{servicesId}")
	public Service getSiteService(@PathVariable("siteId") String siteId,
			@PathVariable("servicesId") String servicesId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Site's policies.
	 * 
	 ********************************************************************************************/
	//@formatter:off
	@Operation(
			summary     = "Retrieve all site's policies information",
			description = "Retrieve all site's policies information",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = {@Content})
	})
	//@formatter:on
	@GetMapping("/{siteId}/policies")
	public List<Policy> getAllPolicies(@PathVariable("siteId") String siteId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	//@formatter:off
	@Operation(
			summary     = "Retrieve particular site's policiy information",
			description = "Retrieve particular site's policiy information",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or policy not found", content = {@Content})
	})
	//@formatter:on
	@GetMapping("/{siteId}/policies/{policyId}")
	public Policy getPolicy(@PathVariable("siteId") String siteId,
			@PathVariable("policyId") String servicesId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Site's policy acceptance.
	 * 
	 ********************************************************************************************/
	//@formatter:off
	@Operation(
			summary     = "Retrieve site policies acceptance",
			description = "Returns list of all site's users with policies and status whether given policy "
						+ "is accepcted or not",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = {@Content})
	})
	//@formatter:on
	@GetMapping("/{siteId}/policyAcceptance")
	public List<PolicyAcceptance> getPolicyAcceptance(@PathVariable("siteId") String siteId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	//@formatter:off
	@Operation(
			summary     = "Accepts sites policy on behalf of user",
			description = "Performs opertion of accepting the policy on behalf of given user",
					security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site, policy or user not found", content = {@Content})
	})
	//@formatter:on
	@PostMapping("/{siteId}/policy/{policyId}/accept/{fenixUserId}")
	public List<PolicyAcceptance> getAcceptPolicy(@PathVariable("siteId") String siteId,
			@PathVariable("policyId") String policyId,
			@PathVariable("fenixUserId") String fenixUserId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	/********************************************************************************************
	 * 
	 * Site's protocol messages.
	 * 
	 ********************************************************************************************/
	//@formatter:off
	@Operation(
			summary     = "Retrieve site's protocol messages",
			description = "Returns all pending site's messages exchanged with site agents.",
					security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site not found", content = {@Content})
	})
	//@formatter:on
	@GetMapping("/{siteId}/protocolMessages")
	public List<ProtocolMessage> getProtocolMessages(@PathVariable("siteId") String siteId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	//@formatter:off
	@Operation(
			summary     = "Delete the protocol message",
			description = "Removes the protocol message out of FURMS control.",
					security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
			)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or message not found", content = {@Content})
	})
	//@formatter:on
	@DeleteMapping("/{siteId}/protocolMessages/{messageId}")
	public void deleteProtocolMessage(@PathVariable("siteId") String siteId,
			@PathVariable("messageId") String messageId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	//@formatter:off
	@Operation(
			summary     = "Retry the protocol message",
			description = "Retry sending protocol message to site agents.",
			security = {@SecurityRequirement(name = APIDocConstants.FURMS_SECURITY_SCHEME)}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "403", description = "Permission denied"),
			@ApiResponse(responseCode = "404", description = "Site or message not found", content = {@Content})
	})
	//@formatter:on
	@PostMapping("/{siteId}/protocolMessages/{messageId}")
	public void retryProtocolMessage(@PathVariable("siteId") String siteId,
			@PathVariable("messageId") String messageId)
	{
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}
}
