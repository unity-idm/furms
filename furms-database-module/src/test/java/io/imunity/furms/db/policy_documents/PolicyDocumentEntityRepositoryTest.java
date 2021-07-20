/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class PolicyDocumentEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private ResourceAccessRepository resourceAccessRepository;
	@Autowired
	private PolicyDocumentEntityRepository policyDocumentEntityRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private InfraServiceRepository infraServiceRepository;
	@Autowired
	private ResourceTypeRepository resourceTypeRepository;
	@Autowired
	private ResourceCreditRepository resourceCreditRepository;
	@Autowired
	private CommunityAllocationRepository communityAllocationRepository;
	@Autowired
	private ProjectAllocationRepository projectAllocationRepository;

	private UUID siteId;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.connectionInfo("alala")
			.build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));
	}

	@Test
	void shouldFindAllBySiteId() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		Set<PolicyDocumentEntity> policyDocumentEntities = policyDocumentEntityRepository.findAllBySiteId(siteId);

		assertThat(policyDocumentEntities.size()).isEqualTo(1);
		PolicyDocumentEntity next = policyDocumentEntities.iterator().next();

		assertThat(saved.siteId).isEqualTo(next.siteId);
		assertThat(saved.name).isEqualTo(next.name);
		assertThat(saved.workflow).isEqualTo(next.workflow);
		assertThat(saved.revision).isEqualTo(next.revision);
		assertThat(saved.contentType).isEqualTo(next.contentType);
		assertThat(saved.htmlText).isEqualTo(next.htmlText);
		assertThat(saved.file).isEqualTo(next.file);
		assertThat(saved.fileType).isEqualTo(next.fileType);
	}

	@Test
	void shouldFindAllSitePoliciesByUserId() {
		Site site = Site.builder()
			.name("name2")
			.connectionInfo("alala")
			.build();
		UUID siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id2")));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		UUID communityId = UUID.fromString(communityRepository.create(community));

		Project project = Project.builder()
			.communityId(communityId.toString())
			.name("name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		UUID projectId = UUID.fromString(projectRepository.create(project));

		InfraService service = InfraService.builder()
			.siteId(siteId.toString())
			.name("name")
			.build();

		UUID serviceId = UUID.fromString(infraServiceRepository.create(service));

		ResourceType resourceType = ResourceType.builder()
			.siteId(siteId.toString())
			.serviceId(serviceId.toString())
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.KILO)
			.build();
		UUID resourceTypeId = UUID.fromString(resourceTypeRepository.create(resourceType));

		UUID resourceCreditId = UUID.fromString(resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId.toString())
			.resourceTypeId(resourceTypeId.toString())
			.name("name")
			.splittable(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build()));

		UUID communityAllocationId = UUID.fromString(communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId.toString())
				.resourceCreditId(resourceCreditId.toString())
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		));

		UUID projectAllocationId = UUID.fromString(projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId.toString())
				.communityAllocationId(communityAllocationId.toString())
				.name("anem")
				.amount(new BigDecimal(5))
				.build()
		));

		FenixUserId fenixUserId = new FenixUserId("id");
		GrantAccess grantAccess = GrantAccess.builder()
			.fenixUserId(fenixUserId)
			.siteId(new SiteId(siteId.toString(), (String) null))
			.projectId(projectId.toString())
			.allocationId(projectAllocationId.toString())
			.build();
		resourceAccessRepository.create(CorrelationId.randomID(), grantAccess, AccessStatus.GRANTED);

		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();
		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		Site updateSite = Site.builder()
			.id(siteId.toString())
			.name("name2")
			.connectionInfo("alala")
			.policyId(new PolicyId(saved.getId()))
			.externalId(new SiteExternalId("id2"))
			.build();
		siteRepository.update(updateSite);

		Set<PolicyDocumentExtendedEntity> policyDocumentEntities = policyDocumentEntityRepository.findAllSitePoliciesByUserId(fenixUserId.id);

		assertThat(policyDocumentEntities.size()).isEqualTo(1);
		PolicyDocumentExtendedEntity next = policyDocumentEntities.iterator().next();

		assertThat(saved.siteId).isEqualTo(next.siteId);
		assertThat("name2").isEqualTo(next.siteName);
		assertThat(saved.name).isEqualTo(next.name);
		assertThat(saved.workflow).isEqualTo(next.workflow);
		assertThat(saved.revision).isEqualTo(next.revision);
		assertThat(saved.contentType).isEqualTo(next.contentType);
		assertThat(saved.htmlText).isEqualTo(next.htmlText);
		assertThat(saved.file).isEqualTo(next.file);
		assertThat(saved.fileType).isEqualTo(next.fileType);
	}

	@Test
	void shouldFindAllServicePoliciesByUserId() {
		Site site = Site.builder()
			.name("name2")
			.connectionInfo("alala")
			.build();
		UUID siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id2")));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		UUID communityId = UUID.fromString(communityRepository.create(community));

		Project project = Project.builder()
			.communityId(communityId.toString())
			.name("name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		UUID projectId = UUID.fromString(projectRepository.create(project));

		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("wysiwygText")
			.build();
		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		InfraService service = InfraService.builder()
			.siteId(siteId.toString())
			.name("serviceName")
			.policyId(new PolicyId(saved.getId()))
			.build();

		UUID serviceId = UUID.fromString(infraServiceRepository.create(service));

		ResourceType resourceType = ResourceType.builder()
			.siteId(siteId.toString())
			.serviceId(serviceId.toString())
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.KILO)
			.build();
		UUID resourceTypeId = UUID.fromString(resourceTypeRepository.create(resourceType));

		UUID resourceCreditId = UUID.fromString(resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId.toString())
			.resourceTypeId(resourceTypeId.toString())
			.name("name")
			.splittable(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build()));

		UUID communityAllocationId = UUID.fromString(communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId.toString())
				.resourceCreditId(resourceCreditId.toString())
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		));

		UUID projectAllocationId = UUID.fromString(projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId.toString())
				.communityAllocationId(communityAllocationId.toString())
				.name("anem")
				.amount(new BigDecimal(5))
				.build()
		));

		FenixUserId fenixUserId = new FenixUserId("id");
		GrantAccess grantAccess = GrantAccess.builder()
			.fenixUserId(fenixUserId)
			.siteId(new SiteId(siteId.toString(), (String) null))
			.projectId(projectId.toString())
			.allocationId(projectAllocationId.toString())
			.build();
		resourceAccessRepository.create(CorrelationId.randomID(), grantAccess, AccessStatus.GRANTED);

		Set<PolicyDocumentExtendedEntity> policyDocumentEntities = policyDocumentEntityRepository.findAllServicePoliciesByUserId(fenixUserId.id);

		assertThat(policyDocumentEntities.size()).isEqualTo(1);
		PolicyDocumentExtendedEntity next = policyDocumentEntities.iterator().next();

		assertThat(next.siteId).isEqualTo(saved.siteId);
		assertThat(next.siteName).isEqualTo("name2");
		assertThat(next.serviceName).isEqualTo("serviceName");
		assertThat(next.name).isEqualTo(saved.name);
		assertThat(next.workflow).isEqualTo(saved.workflow);
		assertThat(next.revision).isEqualTo(saved.revision);
		assertThat(next.contentType).isEqualTo(saved.contentType);
		assertThat(next.htmlText).isEqualTo(saved.htmlText);
		assertThat(next.file).isEqualTo(saved.file);
		assertThat(next.fileType).isEqualTo(saved.fileType);
	}

	@Test
	void shouldExistBySiteIdAndName() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		assertThat(policyDocumentEntityRepository.existsBySiteIdAndName(siteId, "name")).isTrue();
	}

	@Test
	void shouldNotExistBySiteIdAndName() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		assertThat(policyDocumentEntityRepository.existsBySiteIdAndName(UUID.randomUUID(), "name")).isFalse();
	}

	@Test
	void shouldCreateTextPolicyDocument() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocumentEntity next = policyDocumentEntityRepository.findById(saved.getId()).get();

		assertThat(saved.siteId).isEqualTo(next.siteId);
		assertThat(saved.name).isEqualTo(next.name);
		assertThat(saved.workflow).isEqualTo(next.workflow);
		assertThat(saved.revision).isEqualTo(next.revision);
		assertThat(saved.contentType).isEqualTo(next.contentType);
		assertThat(saved.htmlText).isEqualTo(next.htmlText);
		assertThat(saved.file).isEqualTo(next.file);
		assertThat(saved.fileType).isEqualTo(next.fileType);
	}

	@Test
	void shouldCreateFilePolicyDocument() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(1)
			.file(new byte[0])
			.fileType("pdf")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocumentEntity next = policyDocumentEntityRepository.findById(saved.getId()).get();

		assertThat(saved.siteId).isEqualTo(next.siteId);
		assertThat(saved.name).isEqualTo(next.name);
		assertThat(saved.workflow).isEqualTo(next.workflow);
		assertThat(saved.revision).isEqualTo(next.revision);
		assertThat(saved.contentType).isEqualTo(next.contentType);
		assertThat(saved.htmlText).isEqualTo(next.htmlText);
		assertThat(saved.file).isEqualTo(next.file);
		assertThat(saved.fileType).isEqualTo(next.fileType);
	}

	@Test
	void shouldNotCreateWhenContentTypeIs0AndFileIsNotNull() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(1)
			.wysiwygText("sdsadas")
			.file(new byte[0])
			.build();

		assertThrows(Exception.class,() -> policyDocumentEntityRepository.save(policyDocumentEntity));
	}

	@Test
	void shouldNotCreateWhenContentTypeIs1AndTextIsNotNull() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(1)
			.wysiwygText("sdsadas")
			.build();

		assertThrows(Exception.class,() -> policyDocumentEntityRepository.save(policyDocumentEntity));
	}

	@Test
	void shouldUpdate() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocumentEntity policyDocumentEntity1 = PolicyDocumentEntity.builder()
			.id(saved.getId())
			.siteId(siteId)
			.name("name2")
			.workflow(1)
			.revision(1)
			.contentType(1)
			.file(new byte[0])
			.fileType("pdf")
			.build();

		PolicyDocumentEntity saved1 = policyDocumentEntityRepository.save(policyDocumentEntity);
		PolicyDocumentEntity next = policyDocumentEntityRepository.findById(saved.getId()).get();


		assertThat(saved1.siteId).isEqualTo(next.siteId);
		assertThat(saved1.name).isEqualTo(next.name);
		assertThat(saved1.workflow).isEqualTo(next.workflow);
		assertThat(saved1.revision).isEqualTo(next.revision);
		assertThat(saved1.contentType).isEqualTo(next.contentType);
		assertThat(saved1.htmlText).isEqualTo(next.htmlText);
		assertThat(saved1.file).isEqualTo(next.file);
		assertThat(saved1.fileType).isEqualTo(next.fileType);
	}

	@Test
	void shouldDelete() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		policyDocumentEntityRepository.deleteById(saved.getId());

		assertThat(policyDocumentEntityRepository.findById(saved.getId())).isEmpty();
	}
}