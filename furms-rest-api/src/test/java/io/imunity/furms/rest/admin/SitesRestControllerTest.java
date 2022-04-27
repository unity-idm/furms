/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rest.user.User;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static io.imunity.furms.rest.admin.AcceptanceStatus.ACCEPTED;
import static java.math.BigDecimal.ONE;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
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
		SiteId siteId1 = new SiteId(UUID.randomUUID());
		SiteId siteId2 = new SiteId(UUID.randomUUID());
		when(sitesRestService.findAll()).thenReturn(List.of(
			createSite(siteId1, new ResourceCreditId(UUID.randomUUID()), new ResourceTypeId(UUID.randomUUID()),
				new InfraServiceId(UUID.randomUUID()), new PolicyId(UUID.randomUUID()),
				new PolicyId(UUID.randomUUID())),
			createSite(siteId2, new ResourceCreditId(UUID.randomUUID()), new ResourceTypeId(UUID.randomUUID()),
				new InfraServiceId(UUID.randomUUID()), new PolicyId(UUID.randomUUID()),
				new PolicyId(UUID.randomUUID()))
		));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].id").value(siteId1.id.toString()))
				.andExpect(jsonPath("$.[1].id").value(siteId2.id.toString()))
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void shouldFindSiteById() throws Exception {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId typeId = new ResourceTypeId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyId policyId1 = new PolicyId(UUID.randomUUID());

		Site site = createSite(siteId, resourceCreditId, typeId, infraServiceId, policyId, policyId1);
		when(sitesRestService.findOneById(siteId)).thenReturn(site);

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}", siteId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(siteId.id.toString()))
				.andExpect(jsonPath("$.name").value("name"))
				.andExpect(jsonPath("$.sitePolicyId").value("policyId2"))
				.andExpect(jsonPath("$.resourceCredits", hasSize(2)))
				.andExpect(jsonPath("$.resourceCredits[0].creditId").value(resourceCreditId.id.toString()))
				.andExpect(jsonPath("$.resourceCredits[0].name").value("name"))
				.andExpect(jsonPath("$.resourceCredits[0].validity.from").isNotEmpty())
				.andExpect(jsonPath("$.resourceCredits[0].validity.to").isNotEmpty())
				.andExpect(jsonPath("$.resourceCredits[0].resourceTypeId").value(typeId.id.toString()))
				.andExpect(jsonPath("$.resourceCredits[0].amount.amount").value("1"))
				.andExpect(jsonPath("$.resourceCredits[0].amount.unit").value("unit"))
				.andExpect(jsonPath("$.resourceTypes", hasSize(2)))
				.andExpect(jsonPath("$.resourceTypes[0].typeId").value(typeId.id.toString()))
				.andExpect(jsonPath("$.resourceTypes[0].name").value("name"))
				.andExpect(jsonPath("$.resourceTypes[0].serviceId").value(infraServiceId.id.toString()))
				.andExpect(jsonPath("$.services", hasSize(2)))
				.andExpect(jsonPath("$.services[0].serviceId").value(infraServiceId.id.toString()))
				.andExpect(jsonPath("$.services[0].name").value("name"))
				.andExpect(jsonPath("$.services[0].policyId").value(policyId.id.toString()))
				.andExpect(jsonPath("$.policies", hasSize(2)))
				.andExpect(jsonPath("$.policies[0].policyId").value(policyId.id.toString()))
				.andExpect(jsonPath("$.policies[0].name").value("name"))
				.andExpect(jsonPath("$.policies[0].revision").value(0));
	}

	@Test
	void shouldFindAllResourceCreditsBySiteId() throws Exception {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceCreditId resourceCreditId1 = new ResourceCreditId(UUID.randomUUID());
		ResourceCreditId resourceCreditId2 = new ResourceCreditId(UUID.randomUUID());
		when(sitesRestService.findAllResourceCreditsBySiteId(siteId)).thenReturn(List.of(
				createResourceCredit(new ResourceTypeId(UUID.randomUUID()), resourceCreditId1), createResourceCredit(new ResourceTypeId(UUID.randomUUID()),
				resourceCreditId2)));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/credits", siteId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].creditId").value(resourceCreditId1.id.toString()))
				.andExpect(jsonPath("$.[1].creditId").value(resourceCreditId2.id.toString()))
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void shouldFindResourceCreditBySiteIdAndId() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		final ResourceCreditId creditId = new ResourceCreditId(UUID.randomUUID());
		ResourceTypeId typeId = new ResourceTypeId(UUID.randomUUID());
		when(sitesRestService.findResourceCreditBySiteIdAndId(siteId, creditId))
				.thenReturn(createResourceCredit(typeId, creditId));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/credits/{creditId}", siteId.id, creditId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.creditId").value(creditId.id.toString()))
				.andExpect(jsonPath("$.name").value("name"))
				.andExpect(jsonPath("$.validity.from").isNotEmpty())
				.andExpect(jsonPath("$.validity.to").isNotEmpty())
				.andExpect(jsonPath("$.resourceTypeId").value(typeId.id.toString()))
				.andExpect(jsonPath("$.amount.amount").value("1"))
				.andExpect(jsonPath("$.amount.unit").value("unit"));
	}

	@Test
	void shouldFindAllResourceTypesBySiteId() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		final ResourceTypeId typeId = new ResourceTypeId(UUID.randomUUID());
		final ResourceTypeId typeId2 = new ResourceTypeId(UUID.randomUUID());

		when(sitesRestService.findAllResourceTypesBySiteId(siteId)).thenReturn(List.of(
				createResourceType(new InfraServiceId(UUID.randomUUID()), typeId), createResourceType(new InfraServiceId(UUID.randomUUID()), typeId2)));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/resourceTypes", siteId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].typeId").value(typeId.id.toString()))
				.andExpect(jsonPath("$.[1].typeId").value(typeId2.id.toString()));
	}

	@Test
	void shouldFindResourceTypeBySiteIdAndResourceTypeId() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		final ResourceTypeId typeId = new ResourceTypeId(UUID.randomUUID());
		InfraServiceId serviceId = new InfraServiceId(UUID.randomUUID());
		when(sitesRestService.findResourceTypesBySiteIdAndId(siteId, typeId))
				.thenReturn(createResourceType(serviceId, typeId));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/resourceTypes/{typeId}", siteId.id, typeId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.typeId").value(typeId.id.toString()))
				.andExpect(jsonPath("$.name").value("name"))
				.andExpect(jsonPath("$.serviceId").value(serviceId.id.toString()));
	}

	@Test
	void shouldFindAllServicesBySiteId() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId serviceId1 = new InfraServiceId(UUID.randomUUID());
		InfraServiceId serviceId2 = new InfraServiceId(UUID.randomUUID());
		when(sitesRestService.findAllServicesBySiteId(siteId)).thenReturn(List.of(
				createService(new PolicyId(UUID.randomUUID()), serviceId1), createService(new PolicyId(UUID.randomUUID()), serviceId2)));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/services", siteId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].serviceId").value(in(Set.of(serviceId1.id.toString(), serviceId2.id.toString()))))
				.andExpect(jsonPath("$.[1].serviceId").value(in(Set.of(serviceId1.id.toString(), serviceId2.id.toString()))));
	}

	@Test
	void shouldFindAllServicesBySiteIdAndServiceId() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		final InfraServiceId serviceId = new InfraServiceId(UUID.randomUUID());
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		when(sitesRestService.findServiceBySiteIdAndId(siteId, serviceId)).thenReturn(createService(policyId,
			serviceId));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/services/{serviceId}", siteId.id, serviceId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.serviceId").value(serviceId.id.toString()))
				.andExpect(jsonPath("$.name").value("name"))
				.andExpect(jsonPath("$.policyId").value(policyId.id.toString()));
	}

	@Test
	void shouldFindAllPoliciesBySiteId() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		final String policyId1 = UUID.randomUUID().toString();
		final String policyId2 = UUID.randomUUID().toString();
		when(sitesRestService.findAllPolicies(siteId)).thenReturn(List.of(
				createPolicyDocument(new PolicyId(policyId1)), createPolicyDocument(new PolicyId(policyId2))));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/policies", siteId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].policyId").value(in(Set.of(policyId1, policyId2))))
				.andExpect(jsonPath("$.[1].policyId").value(in(Set.of(policyId1, policyId2))));
	}

	@Test
	void shouldFindAllPoliciesBySiteIdAndPolicyId() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		final Policy policy1 = createPolicyDocument(new PolicyId(UUID.randomUUID()));
		final Policy policy2 = createPolicyDocument(new PolicyId(UUID.randomUUID()));
		when(sitesRestService.findPolicy(siteId, policy1.policyId)).thenReturn(policy1);
		when(sitesRestService.findPolicy(siteId, policy2.policyId)).thenReturn(policy2);

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/policies/{policyId}", siteId.id, policy1.policyId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.policyId").value(policy1.policyId))
				.andExpect(jsonPath("$.name").value(policy1.name))
				.andExpect(jsonPath("$.revision").value(policy1.revision));
	}

	@Test
	void shouldFindAllPolicyAcceptancesBySiteId() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		final String policyId1 = UUID.randomUUID().toString();
		final String policyId2 = UUID.randomUUID().toString();
		when(sitesRestService.findAllPoliciesAcceptances(siteId)).thenReturn(List.of(
				createPolicyAcceptance(policyId1, "f1"), createPolicyAcceptance(policyId2, "f2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/policyAcceptances", siteId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].policyId").value(in(Set.of(policyId1, policyId2))))
				.andExpect(jsonPath("$.[0].fenixUserId").value(in(Set.of("f1", "f2"))))
				.andExpect(jsonPath("$.[1].policyId").value(in(Set.of(policyId1, policyId2))))
				.andExpect(jsonPath("$.[1].fenixUserId").value(in(Set.of("f1", "f2"))));
	}

	@Test
	void shouldFindSiteUser() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		final FenixUserId userId = new FenixUserId("userId");
		String projectId = UUID.randomUUID().toString();

		User user = new User(FURMSUser.builder()
			.email("admin@admin.pl")
			.build());
		when(sitesRestService.findSiteUserByUserIdAndSiteId(userId, siteId))
			.thenReturn(new SiteUser(user, "123", Set.of("key"), Set.of(projectId)));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/users/{userId}", siteId.id, userId.id)
				.header(AUTHORIZATION, authKey()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.email").value(is(user.email)))
			.andExpect(jsonPath("$.uid").value((is("123"))))
			.andExpect(jsonPath("$.sshKeys").value(hasItems("key")))
			.andExpect(jsonPath("$.projectIds").value(hasItems(projectId)));
	}

	@Test
	void shouldAcceptPolicyForUserOnSite() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		final String policyId = UUID.randomUUID().toString();
		final String fenixId = "fx1";
		final PolicyAcceptance policyAcceptance = createPolicyAcceptance(policyId, fenixId);
		when(sitesRestService.addPolicyAcceptance(siteId, policyId, fenixId)).thenReturn(List.of(policyAcceptance));

		//when + then
		mockMvc.perform(post(BASE_URL_SITES + "/{siteId}/policies/{policyId}/acceptance/{fenixUserId}",
					siteId.id, policyId, fenixId)
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
		SiteId siteId = new SiteId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId1 = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId2 = new ProjectAllocationId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());

		when(sitesRestService.findAllProjectAllocationsBySiteId(siteId)).thenReturn(List.of(
				createProjectAllocation(projectAllocationId1, communityAllocationId, projectId, resourceTypeId,
					siteId, infraServiceId),
				createProjectAllocation(projectAllocationId2, communityAllocationId, projectId, resourceTypeId,
					siteId, infraServiceId))
		);

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/furmsAllocations", siteId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[1].id").value(projectAllocationId2.id.toString()))
				.andExpect(jsonPath("$.[0].id").value(projectAllocationId1.id.toString()))
				.andExpect(jsonPath("$.[0].projectId").value(projectId.id.toString()))
				.andExpect(jsonPath("$.[0].communityAllocationId").value(communityAllocationId.id.toString()))
				.andExpect(jsonPath("$.[0].name").value("name"))
				.andExpect(jsonPath("$.[0].resourceTypeId").value(resourceTypeId.id.toString()))
				.andExpect(jsonPath("$.[0].amount").value("1"));
	}

	@Test
	void shouldFindAllProjectAllocationsBySiteIdAndProjectId() throws Exception {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId1 = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId2 = new ProjectAllocationId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());

		when(sitesRestService.findAllProjectAllocationsBySiteIdAndProjectId(siteId, projectId)).thenReturn(List.of(
				createProjectAllocation(projectAllocationId1, communityAllocationId, projectId, resourceTypeId,
					siteId, infraServiceId),
			createProjectAllocation(projectAllocationId2, communityAllocationId, projectId, resourceTypeId,
				siteId, infraServiceId)));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/furmsAllocations/{projectId}", siteId.id, projectId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].id").value(projectAllocationId1.id.toString()))
				.andExpect(jsonPath("$.[1].id").value(projectAllocationId2.id.toString()));
	}

	@Test
	void shouldFindAllSiteAllocatedResourcesBySiteId() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		when(sitesRestService.findAllSiteAllocatedResourcesBySiteId(siteId)).thenReturn(List.of(
				createSiteAllocatedResources(siteId, "id1", projectId), createSiteAllocatedResources(siteId, "id2", projectId)));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/siteAllocations", siteId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].allocationId").value("id1"))
				.andExpect(jsonPath("$.[1].allocationId").value("id2"))
				.andExpect(jsonPath("$.[1].siteId").value(siteId.id.toString()))
				.andExpect(jsonPath("$.[1].projectId").value(projectId.id.toString()))
				.andExpect(jsonPath("$.[1].amount").value("1"))
				.andExpect(jsonPath("$.[1].validity.from").isNotEmpty())
				.andExpect(jsonPath("$.[1].validity.to").isNotEmpty());
	}

	@Test
	void shouldFindAllSiteAllocatedResourcesBySiteIdAndProjectId() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		final ProjectId projectId = new ProjectId(UUID.randomUUID());
		when(sitesRestService.findAllSiteAllocatedResourcesBySiteIdAndProjectId(siteId, projectId)).thenReturn(
				List.of(createSiteAllocatedResources(siteId, "id1", projectId), createSiteAllocatedResources(siteId,
					"id2", projectId)));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/siteAllocations/{projectId}", siteId.id, projectId.id)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].allocationId").value("id1"))
				.andExpect(jsonPath("$.[1].allocationId").value("id2"));
	}

	@Test
	void shouldFindAllProjectCumulativeResourceConsumptionBySiteIdAndProjectId() throws Exception {
		//given
		final SiteId siteId = new SiteId(UUID.randomUUID());
		final ProjectId projectId = new ProjectId(UUID.randomUUID());
		when(sitesRestService.findAllProjectCumulativeResourceConsumptionBySiteIdAndProjectId(siteId, projectId))
				.thenReturn(List.of(createProjectCumulativeResourceConsumption(siteId, "id1"),
						createProjectCumulativeResourceConsumption(siteId, "id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/cumulativeResourcesConsumption/{projectId}",
					siteId.id, projectId.id)
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
		final SiteId siteId = new SiteId(UUID.randomUUID());
		final ProjectId projectId = new ProjectId(UUID.randomUUID());
		when(sitesRestService.findAllProjectUsageRecordBySiteIdAndProjectIdInPeriod(siteId, projectId, sampleFrom, sampleTo))
				.thenReturn(List.of(createProjectUsageRecord(siteId, "id1"), createProjectUsageRecord(siteId, "id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_SITES + "/{siteId}/usageRecords/{projectId}",
				siteId.id, projectId.id)
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

	private Site createSite(SiteId id, ResourceCreditId resourceCreditId, ResourceTypeId typeId,
	                        InfraServiceId infraServiceId,
	                        PolicyId policyId,
	                        PolicyId policyId1) {
		return new Site(id.id.toString(),  "name", "policyId2",
				List.of(createResourceCredit(typeId, resourceCreditId), createResourceCredit(typeId, resourceCreditId)),
				List.of(createResourceType(infraServiceId, typeId), createResourceType(infraServiceId,
					new ResourceTypeId(typeId))),
				List.of(createService(policyId, infraServiceId),createService(policyId,
					new InfraServiceId(infraServiceId))),
				List.of(new Policy(policyId.id.toString(), "name", 0),
						new Policy(policyId1.id.toString(), "name", 1)));
	}

	private ResourceCredit createResourceCredit(ResourceTypeId typeId, ResourceCreditId creditId) {
		return new ResourceCredit(creditId.id.toString(), "name", new Validity(sampleFrom, sampleTo),
			typeId.id.toString(), createResourceAmount());
	}

	private ResourceType createResourceType(InfraServiceId serviceId, ResourceTypeId typeId) {
		return new ResourceType(typeId.id.toString(), "name", serviceId.id.toString());
	}

	private InfraService createService(PolicyId policyId, InfraServiceId serviceId) {
		return new InfraService(serviceId.id.toString(), "name", policyId.id.toString());
	}

	private Policy createPolicyDocument(PolicyId policyId) {
		return new Policy(policyId.id.toString(), "name", 1);
	}

	private PolicyAcceptance createPolicyAcceptance(String policyId, String fenixId) {
		return new PolicyAcceptance(policyId, 1, 1, fenixId, ACCEPTED, ZonedDateTime.now());
	}

	private ResourceAmount createResourceAmount() {
		return new ResourceAmount(ONE, "unit");
	}

	private ProjectAllocation createProjectAllocation(ProjectAllocationId allocationId,
	                                                  CommunityAllocationId communityAllocationId, ProjectId projectId,
	                                                  ResourceTypeId typeId, SiteId siteId, InfraServiceId infraServiceId) {
		return new ProjectAllocation(
			allocationId.id.toString(),
			projectId.id.toString(),
			communityAllocationId.id.toString(),
			"name",
			typeId.id.toString(),
			"resourceUnit",
			siteId.id.toString(),
			"siteName",
			infraServiceId.id.toString(),
			"serviceName",
			ONE
		);
	}

	private SiteAllocatedResources createSiteAllocatedResources(SiteId siteId, String id, ProjectId projectId) {
		return new SiteAllocatedResources(id, siteId.id.toString(), projectId.id.toString(), ONE, new Validity(sampleFrom,
			sampleTo));
	}

	private ProjectCumulativeResourceConsumption createProjectCumulativeResourceConsumption(SiteId siteId, String id) {
		return new ProjectCumulativeResourceConsumption(id, siteId.id.toString(), "typeId", ONE, sampleTo);
	}

	private ProjectUsageRecord createProjectUsageRecord(SiteId siteId, String id) {
		return new ProjectUsageRecord(id,"typeId", siteId.id.toString(), ONE, sampleUser.fenixIdentifier, sampleFrom, sampleTo);
	}

}