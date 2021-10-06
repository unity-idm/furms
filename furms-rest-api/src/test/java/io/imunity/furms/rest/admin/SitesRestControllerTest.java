/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static io.imunity.furms.rest.admin.AcceptanceStatus.ACCEPTED;
import static java.math.BigDecimal.ONE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
				.andExpect(jsonPath("$.resourceCredits[0].creditId").value("creditId1"))
				.andExpect(jsonPath("$.resourceCredits[0].name").value("name"))
				.andExpect(jsonPath("$.resourceCredits[0].validity.from").isNotEmpty())
				.andExpect(jsonPath("$.resourceCredits[0].validity.to").isNotEmpty())
				.andExpect(jsonPath("$.resourceCredits[0].resourceTypeId").value("resourceTypeId"))
				.andExpect(jsonPath("$.resourceCredits[0].amount.amount").value("1"))
				.andExpect(jsonPath("$.resourceCredits[0].amount.unit").value("unit"))
				.andExpect(jsonPath("$.resourceTypes", hasSize(2)))
				.andExpect(jsonPath("$.resourceTypes[0].typeId").value("typeId1"))
				.andExpect(jsonPath("$.resourceTypes[0].name").value("name"))
				.andExpect(jsonPath("$.resourceTypes[0].serviceId").value("serviceId"))
				.andExpect(jsonPath("$.services", hasSize(2)))
				.andExpect(jsonPath("$.services[0].serviceId").value("serviceId1"))
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
				.andExpect(jsonPath("$.[0].creditId").value("id1"))
				.andExpect(jsonPath("$.[1].creditId").value("id2"))
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
				.andExpect(jsonPath("$.creditId").value(creditId))
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
				.andExpect(jsonPath("$.[0].typeId").value("id1"))
				.andExpect(jsonPath("$.[1].typeId").value("id2"));
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
				.andExpect(jsonPath("$.typeId").value(typeId))
				.andExpect(jsonPath("$.name").value("name"))
				.andExpect(jsonPath("$.serviceId").value("serviceId"));
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
				.andExpect(jsonPath("$.[0].serviceId").value(in(Set.of("id1", "id2"))))
				.andExpect(jsonPath("$.[1].serviceId").value(in(Set.of("id1", "id2"))));
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
				.andExpect(jsonPath("$.serviceId").value(serviceId))
				.andExpect(jsonPath("$.name").value("name"))
				.andExpect(jsonPath("$.policyId").value("policyId"));
	}

	@Test
	void shouldFindAllPoliciesBySiteId() throws Exception {
		//given
		final String siteId = "siteId";
		final String policyId1 = UUID.randomUUID().toString();
		final String policyId2 = UUID.randomUUID().toString();
		when(sitesRestService.findAllPolicies(siteId)).thenReturn(List.of(
				createPolicyDocument(policyId1), createPolicyDocument(policyId2)));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/policies", siteId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].policyId").value(in(Set.of(policyId1, policyId2))))
				.andExpect(jsonPath("$.[1].policyId").value(in(Set.of(policyId1, policyId2))));
	}

	@Test
	void shouldFindAllPoliciesBySiteIdAndPolicyId() throws Exception {
		//given
		final String siteId = "siteId";
		final Policy policy1 = createPolicyDocument(UUID.randomUUID().toString());
		final Policy policy2 = createPolicyDocument(UUID.randomUUID().toString());
		when(sitesRestService.findPolicy(siteId, policy1.policyId)).thenReturn(policy1);
		when(sitesRestService.findPolicy(siteId, policy2.policyId)).thenReturn(policy2);

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/policies/{policyId}", siteId, policy1.policyId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.policyId").value(policy1.policyId))
				.andExpect(jsonPath("$.name").value(policy1.name))
				.andExpect(jsonPath("$.revision").value(policy1.revision));
	}

	@Test
	void shouldFindAllPolicyAcceptancesBySiteId() throws Exception {
		//given
		final String siteId = "siteId";
		final String policyId1 = UUID.randomUUID().toString();
		final String policyId2 = UUID.randomUUID().toString();
		when(sitesRestService.findAllPoliciesAcceptances(siteId)).thenReturn(List.of(
				createPolicyAcceptance(policyId1, "f1"), createPolicyAcceptance(policyId2, "f2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/policyAcceptances", siteId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].policyId").value(in(Set.of(policyId1, policyId2))))
				.andExpect(jsonPath("$.[0].fenixUserId").value(in(Set.of("f1", "f2"))))
				.andExpect(jsonPath("$.[1].policyId").value(in(Set.of(policyId1, policyId2))))
				.andExpect(jsonPath("$.[1].fenixUserId").value(in(Set.of("f1", "f2"))));
	}

	@Test
	void shouldAcceptPolicyForUserOnSite() throws Exception {
		//given
		final String siteId = "siteId";
		final String policyId = UUID.randomUUID().toString();
		final String fenixId = "fx1";
		final PolicyAcceptance policyAcceptance = createPolicyAcceptance(policyId, fenixId);
		when(sitesRestService.addPolicyAcceptance(siteId, policyId, fenixId)).thenReturn(List.of(policyAcceptance));

		//when + then
		mockMvc.perform(post(BASE_URL_SITES + "/{siteId}/policies/{policyId}/acceptance/{fenixUserId}",
					siteId, policyId, fenixId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].policyId").value(policyId))
				.andExpect(jsonPath("$.[0].fenixUserId").value(fenixId))
				.andExpect(jsonPath("$.[0].acceptanceStatus").value("ACCEPTED"))
				.andExpect(jsonPath("$.[0].processedOn").isNotEmpty())
				.andExpect(jsonPath("$.[0].currentPolicyRevision").value(policyAcceptance.currentPolicyRevision))
				.andExpect(jsonPath("$.[0].acceptedRevision").value(policyAcceptance.acceptedRevision));
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
				.andExpect(jsonPath("$.[0].projectId").value("projectId"))
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
				.andExpect(jsonPath("$.[1].projectId").value("projectId"))
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
		return new ResourceCredit(id, "name", new Validity(sampleFrom, sampleTo),
				"resourceTypeId", createResourceAmount());
	}

	private ResourceType createResourceType(String siteId, String typeId) {
		return new ResourceType(typeId, "name", "serviceId");
	}

	private InfraService createService(String siteId, String serviceId) {
		return new InfraService(serviceId, "name", "policyId");
	}

	private Policy createPolicyDocument(String policyId) {
		return new Policy(policyId, "name", 1);
	}

	private PolicyAcceptance createPolicyAcceptance(String policyId, String fenixId) {
		return new PolicyAcceptance(policyId, 1, 1,fenixId, ACCEPTED, ZonedDateTime.now());
	}

	private ResourceAmount createResourceAmount() {
		return new ResourceAmount(ONE, "unit");
	}

	private ProjectAllocation createProjectAllocation(String allocationId) {
		return new ProjectAllocation(allocationId, "projectId", allocationId, "name", "typeId", "resourceUnit", "siteId",
				"siteName", "serviceId", "serviceName", ONE);
	}

	private SiteAllocatedResources createSiteAllocatedResources(String siteId, String id) {
		return new SiteAllocatedResources(id, siteId, "projectId", ONE, new Validity(sampleFrom, sampleTo));
	}

	private ProjectCumulativeResourceConsumption createProjectCumulativeResourceConsumption(String siteId, String id) {
		return new ProjectCumulativeResourceConsumption(id, siteId, "typeId", ONE, sampleTo);
	}

	private ProjectUsageRecord createProjectUsageRecord(String siteId, String id) {
		return new ProjectUsageRecord(id,"typeId", siteId, ONE, sampleUser.fenixIdentifier, sampleFrom, sampleTo);
	}

}