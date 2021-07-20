/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyFile;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PolicyDocumentDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private PolicyDocumentEntityRepository policyDocumentEntityRepository;
	@Autowired
	private PolicyDocumentDatabaseRepository repository;
	@Autowired
	private ResourceAccessRepository resourceAccessRepository;
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
	void shouldFindById() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		Optional<PolicyDocument> policyDocument = repository.findById(new PolicyId(saved.getId()));

		assertThat(policyDocument).isPresent();

		assertThat(policyDocument.get().siteId).isEqualTo(saved.siteId.toString());
		assertThat(policyDocument.get().name).isEqualTo(saved.name);
		assertThat(policyDocument.get().workflow).isEqualTo(PolicyWorkflow.valueOf(saved.workflow));
		assertThat(policyDocument.get().revision).isEqualTo(saved.revision);
		assertThat(policyDocument.get().contentType).isEqualTo(PolicyContentType.valueOf(saved.contentType));
		assertThat(policyDocument.get().htmlText).isEqualTo(saved.htmlText);
		assertThat(policyDocument.get().policyFile).isEqualTo(PolicyFile.empty());
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

		PolicyDocumentEntity policyDocumentEntity1 = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name2")
			.workflow(1)
			.revision(0)
			.contentType(1)
			.file(new byte[1])
			.fileType("pdf")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);
		PolicyDocumentEntity saved1 = policyDocumentEntityRepository.save(policyDocumentEntity1);

		Set<PolicyDocument> policyDocuments = repository.findAllBySiteId(siteId.toString());

		assertThat(policyDocuments.size()).isEqualTo(2);
	}

	@Test
	void shouldFindAllByUserId() {
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

		PolicyDocumentEntity policyDocumentEntity2 = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();
		PolicyDocumentEntity saved2 = policyDocumentEntityRepository.save(policyDocumentEntity2);

		Site updateSite = Site.builder()
			.id(siteId.toString())
			.name("name2")
			.connectionInfo("alala")
			.policyId(new PolicyId(saved2.getId()))
			.externalId(new SiteExternalId("id2"))
			.build();
		siteRepository.update(updateSite);
		LocalDateTime now = LocalDateTime.now();
		Set<PolicyDocumentExtended> policyDocuments = repository.findAllByUserId(fenixUserId, x -> now);

		assertThat(policyDocuments.size()).isEqualTo(2);
	}

	@Test
	void shouldCreateTextPolicyDocument() {
		PolicyDocument policyDocument = PolicyDocument.builder()
			.siteId(siteId.toString())
			.name("name")
			.workflow(PolicyWorkflow.PAPER_BASED)
			.revision(0)
			.contentType(PolicyContentType.EMBEDDED)
			.wysiwygText("sdsadas")
			.file(PolicyFile.empty())
			.build();

		PolicyId policyId = repository.create(policyDocument);

		Optional<PolicyDocumentEntity> policyDocumentEntity = policyDocumentEntityRepository.findById(policyId.id);

		assertThat(policyDocumentEntity).isPresent();

		assertThat(policyDocumentEntity.get().siteId.toString()).isEqualTo(policyDocument.siteId);
		assertThat(policyDocumentEntity.get().name).isEqualTo(policyDocument.name);
		assertThat(policyDocumentEntity.get().workflow).isEqualTo(policyDocument.workflow.getPersistentId());
		assertThat(policyDocumentEntity.get().revision).isEqualTo(policyDocument.revision);
		assertThat(policyDocumentEntity.get().contentType).isEqualTo(policyDocument.contentType.getPersistentId());
		assertThat(policyDocumentEntity.get().htmlText).isEqualTo(policyDocument.htmlText);
		assertThat(policyDocumentEntity.get().file).isEqualTo(null);
		assertThat(policyDocumentEntity.get().fileType).isEqualTo(null);
	}

	@Test
	void shouldCreateFilePolicyDocument() {
		PolicyDocument policyDocument = PolicyDocument.builder()
			.siteId(siteId.toString())
			.name("name")
			.workflow(PolicyWorkflow.PAPER_BASED)
			.revision(0)
			.contentType(PolicyContentType.PDF)
			.wysiwygText(" ")
			.file(new byte[1], "pdf")
			.build();

		PolicyId policyId = repository.create(policyDocument);

		Optional<PolicyDocumentEntity> policyDocumentEntity = policyDocumentEntityRepository.findById(policyId.id);

		assertThat(policyDocumentEntity).isPresent();

		assertThat(policyDocumentEntity.get().siteId.toString()).isEqualTo(policyDocument.siteId);
		assertThat(policyDocumentEntity.get().name).isEqualTo(policyDocument.name);
		assertThat(policyDocumentEntity.get().workflow).isEqualTo(policyDocument.workflow.getPersistentId());
		assertThat(policyDocumentEntity.get().revision).isEqualTo(policyDocument.revision);
		assertThat(policyDocumentEntity.get().contentType).isEqualTo(policyDocument.contentType.getPersistentId());
		assertThat(policyDocumentEntity.get().htmlText).isEqualTo(null);
		assertThat(policyDocumentEntity.get().file).isEqualTo(new byte[1]);
		assertThat(policyDocumentEntity.get().fileType).isEqualTo("pdf");
	}

	@Test
	void shouldUpdateWithRevision() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(new PolicyId(saved.getId()))
			.siteId(siteId.toString())
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.revision(0)
			.contentType(PolicyContentType.PDF)
			.wysiwygText(" ")
			.file(new byte[1], "pdf")
			.build();

		repository.update(policyDocument, true);


		Optional<PolicyDocumentEntity> readPolicyDocumentEntity = policyDocumentEntityRepository.findById(saved.getId());
		assertThat(readPolicyDocumentEntity).isPresent();

		assertThat(readPolicyDocumentEntity.get().siteId.toString()).isEqualTo(policyDocument.siteId);
		assertThat(readPolicyDocumentEntity.get().name).isEqualTo(policyDocument.name);
		assertThat(readPolicyDocumentEntity.get().workflow).isEqualTo(policyDocumentEntity.workflow);
		assertThat(readPolicyDocumentEntity.get().revision).isEqualTo(1);
		assertThat(readPolicyDocumentEntity.get().contentType).isEqualTo(policyDocument.contentType.getPersistentId());
		assertThat(readPolicyDocumentEntity.get().htmlText).isEqualTo(null);
		assertThat(readPolicyDocumentEntity.get().file).isEqualTo(new byte[1]);
		assertThat(readPolicyDocumentEntity.get().fileType).isEqualTo("pdf");
	}

	@Test
	void shouldUpdateWithoutRevision() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(new PolicyId(saved.getId()))
			.siteId(siteId.toString())
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.revision(0)
			.contentType(PolicyContentType.PDF)
			.wysiwygText(" ")
			.file(new byte[1], "pdf")
			.build();

		repository.update(policyDocument, false);


		Optional<PolicyDocumentEntity> readPolicyDocumentEntity = policyDocumentEntityRepository.findById(saved.getId());
		assertThat(readPolicyDocumentEntity).isPresent();

		assertThat(readPolicyDocumentEntity.get().siteId.toString()).isEqualTo(policyDocument.siteId);
		assertThat(readPolicyDocumentEntity.get().name).isEqualTo(policyDocument.name);
		assertThat(readPolicyDocumentEntity.get().workflow).isEqualTo(policyDocumentEntity.workflow);
		assertThat(readPolicyDocumentEntity.get().revision).isEqualTo(0);
		assertThat(readPolicyDocumentEntity.get().contentType).isEqualTo(policyDocument.contentType.getPersistentId());
		assertThat(readPolicyDocumentEntity.get().htmlText).isEqualTo(null);
		assertThat(readPolicyDocumentEntity.get().file).isEqualTo(new byte[1]);
		assertThat(readPolicyDocumentEntity.get().fileType).isEqualTo("pdf");
	}

	@Test
	void shouldDeleteById() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		repository.deleteById(new PolicyId(saved.getId()));

		Optional<PolicyDocumentEntity> readPolicyDocumentEntity = policyDocumentEntityRepository.findById(saved.getId());
		assertThat(readPolicyDocumentEntity).isEmpty();
	}

	@Test
	void shouldNameBePresent() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		assertThat(repository.isNamePresent(siteId.toString(), "name2")).isTrue();
	}

	@Test
	void shouldNameNotBePresent() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		assertThat(repository.isNamePresent(siteId.toString(), "name")).isFalse();
	}
}