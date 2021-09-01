/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.math.BigDecimal.ONE;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SitesRestControllerTest extends RestApiControllerIntegrationTest {

	private final static String BASE_URL_SITES = "/rest-api/v1/sites";

	@Test
	void shouldFindAllSites() throws Exception {
		//given
		when(sitesRestService.findAll()).thenReturn(List.of(createSite("id1"), createSite("id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].id").value("id1"))
				.andExpect(jsonPath("$.[1].id").value("id2"))
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void shouldFindSiteById() throws Exception {
		//given
		final String siteId = "siteId";
		when(sitesRestService.findOneById(siteId)).thenReturn(createSite(siteId));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}", siteId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(siteId))
				.andExpect(jsonPath("$.name").value("name"))
				.andExpect(jsonPath("$.sitePolicyId").value("policyId2"))
				.andExpect(jsonPath("$.resourceCredits", hasSize(2)))
				.andExpect(jsonPath("$.resourceCredits[0].id.siteId").value(siteId))
				.andExpect(jsonPath("$.resourceCredits[0].id.creditId").value("creditId1"))
				.andExpect(jsonPath("$.resourceCredits[0].name").value("name"))
				.andExpect(jsonPath("$.resourceCredits[0].validity.from").isNotEmpty())
				.andExpect(jsonPath("$.resourceCredits[0].validity.to").isNotEmpty())
				.andExpect(jsonPath("$.resourceCredits[0].resourceTypeId").value("resourceTypeId"))
				.andExpect(jsonPath("$.resourceCredits[0].amount.amount").value("1"))
				.andExpect(jsonPath("$.resourceCredits[0].amount.unit").value("unit"))
				.andExpect(jsonPath("$.resourceTypes", hasSize(2)))
				.andExpect(jsonPath("$.resourceTypes[0].id.siteId").value(siteId))
				.andExpect(jsonPath("$.resourceTypes[0].id.typeId").value("typeId1"))
				.andExpect(jsonPath("$.resourceTypes[0].name").value("name"))
				.andExpect(jsonPath("$.resourceTypes[0].serviceId.siteId").value(siteId))
				.andExpect(jsonPath("$.resourceTypes[0].serviceId.serviceId").value("serviceId"))
				.andExpect(jsonPath("$.services", hasSize(2)))
				.andExpect(jsonPath("$.services[0].id.siteId").value(siteId))
				.andExpect(jsonPath("$.services[0].id.serviceId").value("serviceId1"))
				.andExpect(jsonPath("$.services[0].name").value("name"))
				.andExpect(jsonPath("$.services[0].policyId").value("policyId"))
				.andExpect(jsonPath("$.policies", hasSize(2)))
				.andExpect(jsonPath("$.policies[0].policyId").value("policyId1"))
				.andExpect(jsonPath("$.policies[0].name").value("name"))
				.andExpect(jsonPath("$.policies[0].revision").value(0));
	}

	@Test
	void shouldFindAllResourceCreditsBySiteId() throws Exception {
		//given
		final String siteId = "siteId";
		when(sitesRestService.findAllResourceCreditsBySiteId(siteId)).thenReturn(List.of(
				createResourceCredit(siteId, "id1"), createResourceCredit(siteId, "id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/credits", siteId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].id.creditId").value("id1"))
				.andExpect(jsonPath("$.[1].id.creditId").value("id2"))
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void shouldFindResourceCreditBySiteIdAndId() throws Exception {
		//given
		final String siteId = "siteId";
		final String creditId = "creditId";
		when(sitesRestService.findResourceCreditBySiteIdAndId(siteId, creditId))
				.thenReturn(createResourceCredit(siteId, creditId));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/credits/{creditId}", siteId, creditId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id.siteId").value(siteId))
				.andExpect(jsonPath("$.id.creditId").value(creditId))
				.andExpect(jsonPath("$.name").value("name"))
				.andExpect(jsonPath("$.validity.from").isNotEmpty())
				.andExpect(jsonPath("$.validity.to").isNotEmpty())
				.andExpect(jsonPath("$.resourceTypeId").value("resourceTypeId"))
				.andExpect(jsonPath("$.amount.amount").value("1"))
				.andExpect(jsonPath("$.amount.unit").value("unit"));
	}

	@Test
	void shouldFindAllResourceTypesBySiteId() throws Exception {
		//given
		final String siteId = "siteId";
		when(sitesRestService.findAllResourceTypesBySiteId(siteId)).thenReturn(List.of(
				createResourceType(siteId, "id1"), createResourceType(siteId, "id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/resourceTypes", siteId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].id.typeId").value("id1"))
				.andExpect(jsonPath("$.[1].id.typeId").value("id2"));
	}

	@Test
	void shouldFindResourceTypeBySiteIdAndResourceTypeId() throws Exception {
		//given
		final String siteId = "siteId";
		final String typeId = "typeId";
		when(sitesRestService.findResourceTypesBySiteIdAndId(siteId, typeId))
				.thenReturn(createResourceType(siteId, typeId));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/resourceTypes/{typeId}", siteId, typeId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id.siteId").value(siteId))
				.andExpect(jsonPath("$.id.typeId").value(typeId))
				.andExpect(jsonPath("$.name").value("name"))
				.andExpect(jsonPath("$.serviceId.siteId").value(siteId))
				.andExpect(jsonPath("$.serviceId.serviceId").value("serviceId"));
	}

	@Test
	void shouldFindAllServicesBySiteId() throws Exception {
		//given
		final String siteId = "siteId";
		when(sitesRestService.findAllServicesBySiteId(siteId)).thenReturn(List.of(
				createService(siteId, "id1"), createService(siteId, "id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/services", siteId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].id.serviceId").value("id1"))
				.andExpect(jsonPath("$.[1].id.serviceId").value("id2"));
	}

	@Test
	void shouldFindAllServicesBySiteIdAndServiceId() throws Exception {
		//given
		final String siteId = "siteId";
		final String serviceId = "serviceId";
		when(sitesRestService.findServiceBySiteIdAndId(siteId, serviceId)).thenReturn(createService(siteId, serviceId));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/services/{serviceId}", siteId, serviceId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id.siteId").value(siteId))
				.andExpect(jsonPath("$.id.serviceId").value(serviceId))
				.andExpect(jsonPath("$.name").value("name"))
				.andExpect(jsonPath("$.policyId").value("policyId"));
	}

	@Test
	void shouldFindAllProjectAllocationsBySiteId() throws Exception {
		//given
		final String siteId = "siteId";
		when(sitesRestService.findAllProjectAllocationsBySiteId(siteId)).thenReturn(List.of(
				createProjectAllocation("id1"), createProjectAllocation("id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/furmsAllocations", siteId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[1].id").value("id2"))
				.andExpect(jsonPath("$.[0].id").value("id1"))
				.andExpect(jsonPath("$.[0].communityAllocationId").value("id1"))
				.andExpect(jsonPath("$.[0].name").value("name"))
				.andExpect(jsonPath("$.[0].resourceTypeId").value("typeId"))
				.andExpect(jsonPath("$.[0].amount").value("1"));
	}

	@Test
	void shouldFindAllProjectAllocationsBySiteIdAndProjectId() throws Exception {
		//given
		final String siteId = "siteId";
		final String projectId = "projectId";
		when(sitesRestService.findAllProjectAllocationsBySiteIdAndProjectId(siteId, projectId)).thenReturn(List.of(
				createProjectAllocation("id1"), createProjectAllocation("id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/furmsAllocations/{projectId}", siteId, projectId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].id").value("id1"))
				.andExpect(jsonPath("$.[1].id").value("id2"));
	}

	@Test
	void shouldFindAllSiteAllocatedResourcesBySiteId() throws Exception {
		//given
		final String siteId = "siteId";
		when(sitesRestService.findAllSiteAllocatedResourcesBySiteId(siteId)).thenReturn(List.of(
				createSiteAllocatedResources(siteId, "id1"), createSiteAllocatedResources(siteId, "id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/siteAllocations", siteId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].allocationId").value("id1"))
				.andExpect(jsonPath("$.[1].allocationId").value("id2"))
				.andExpect(jsonPath("$.[1].siteId").value(siteId))
				.andExpect(jsonPath("$.[1].amount").value("1"))
				.andExpect(jsonPath("$.[1].validity.from").isNotEmpty())
				.andExpect(jsonPath("$.[1].validity.to").isNotEmpty());
	}

	@Test
	void shouldFindAllSiteAllocatedResourcesBySiteIdAndProjectId() throws Exception {
		//given
		final String siteId = "siteId";
		final String projectId = "projectId";
		when(sitesRestService.findAllSiteAllocatedResourcesBySiteIdAndProjectId(siteId, projectId)).thenReturn(
				List.of(createSiteAllocatedResources(siteId, "id1"), createSiteAllocatedResources(siteId, "id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/siteAllocations/{projectId}", siteId, projectId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].allocationId").value("id1"))
				.andExpect(jsonPath("$.[1].allocationId").value("id2"));
	}

	@Test
	void shouldFindAllProjectCumulativeResourceConsumptionBySiteIdAndProjectId() throws Exception {
		//given
		final String siteId = "siteId";
		final String projectId = "projectId";
		when(sitesRestService.findAllProjectCumulativeResourceConsumptionBySiteIdAndProjectId(siteId, projectId))
				.thenReturn(List.of(createProjectCumulativeResourceConsumption(siteId, "id1"),
						createProjectCumulativeResourceConsumption(siteId, "id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/cumulativeResourcesConsumption/{projectId}",
					siteId, projectId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].projectAllocationId").value("id1"))
				.andExpect(jsonPath("$.[1].projectAllocationId").value("id2"))
				.andExpect(jsonPath("$.[1].resourceTypeId").value("typeId"))
				.andExpect(jsonPath("$.[1].consumedAmount").value("1"))
				.andExpect(jsonPath("$.[1].consumedUntil").isNotEmpty());
	}

	@Test
	void shouldFindAllProjectUsageRecordBySiteIdAndProjectIdInPeriod() throws Exception {
		//given
		final String siteId = "siteId";
		final String projectId = "projectId";
		when(sitesRestService.findAllProjectUsageRecordBySiteIdAndProjectIdInPeriod(siteId, projectId, sampleFrom, sampleTo))
				.thenReturn(List.of(createProjectUsageRecord(siteId, "id1"), createProjectUsageRecord(siteId, "id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/usageRecords/{projectId}",
					siteId, projectId)
				.queryParam("from", sampleFrom.toString())
				.queryParam("until", sampleTo.toString())
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].projectAllocationId").value("id1"))
				.andExpect(jsonPath("$.[1].projectAllocationId").value("id2"))
				.andExpect(jsonPath("$.[1].resourceTypeId").value("typeId"))
				.andExpect(jsonPath("$.[1].consumedAmount").value("1"))
				.andExpect(jsonPath("$.[1].userFenixId").value("fenixIdentifier"))
				.andExpect(jsonPath("$.[1].from").isNotEmpty())
				.andExpect(jsonPath("$.[1].until").isNotEmpty());
	}

	private Site createSite(String id) {
		return new Site(id,  "name", "policyId2",
				List.of(createResourceCredit(id, "creditId1"), createResourceCredit(id, "creditId2")),
				List.of(createResourceType(id, "typeId1"), createResourceType(id, "typeId1")),
				List.of(createService(id, "serviceId1"),createService(id, "serviceId2")),
				List.of(new Policy("policyId1", "name", 0),
						new Policy("policyId2", "name", 1)));
	}

	private ResourceCredit createResourceCredit(String siteId, String id) {
		return new ResourceCredit(new SiteCreditId(siteId, id), "name", new Validity(sampleFrom, sampleTo),
				"resourceTypeId", createResourceAmount());
	}

	private ResourceType createResourceType(String siteId, String typeId) {
		return new ResourceType(new ResourceTypeId(siteId, typeId), "name", new ServiceId(siteId, "serviceId"));
	}

	private InfraService createService(String siteId, String serviceId) {
		return new InfraService(new ServiceId(siteId, serviceId), "name", "policyId");
	}

	private ResourceAmount createResourceAmount() {
		return new ResourceAmount(ONE, "unit");
	}

	private ProjectAllocation createProjectAllocation(String allocationId) {
		return new ProjectAllocation(allocationId, allocationId, "name", "typeId", ONE);
	}

	private SiteAllocatedResources createSiteAllocatedResources(String siteId, String id) {
		return new SiteAllocatedResources(id, siteId, ONE, new Validity(sampleFrom, sampleTo));
	}

	private ProjectCumulativeResourceConsumption createProjectCumulativeResourceConsumption(String siteId, String id) {
		return new ProjectCumulativeResourceConsumption(id, siteId, "typeId", ONE, sampleTo);
	}

	private ProjectUsageRecord createProjectUsageRecord(String siteId, String id) {
		return new ProjectUsageRecord(id,"typeId", siteId, ONE, sampleUser.fenixIdentifier, sampleFrom, sampleTo);
	}

}