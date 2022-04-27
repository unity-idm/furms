/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyFile;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;
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
import io.imunity.furms.spi.user_site_access.UserSiteAccessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
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
	@Autowired
	private UserSiteAccessRepository userSiteAccessRepository;

	private SiteId siteId;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.connectionInfo("alala")
			.build();
		siteId = siteRepository.create(site, new SiteExternalId("id"));
	}

	@Test
	void shouldFindById() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		Optional<PolicyDocument> policyDocument = repository.findById(new PolicyId(saved.getId()));

		assertThat(policyDocument).isPresent();

		assertThat(policyDocument.get().siteId.id).isEqualTo(saved.siteId);
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
			.siteId(siteId.id)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity policyDocumentEntity1 = PolicyDocumentEntity.builder()
			.siteId(siteId.id)
			.name("name2")
			.workflow(1)
			.revision(0)
			.contentType(1)
			.file(new byte[1])
			.fileType("pdf")
			.build();

		policyDocumentEntityRepository.save(policyDocumentEntity);
		policyDocumentEntityRepository.save(policyDocumentEntity1);

		Set<PolicyDocument> policyDocuments = repository.findAllBySiteId(siteId);

		assertThat(policyDocuments.size()).isEqualTo(2);
	}

	@Test
	void shouldFindAllPolicyUsers() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("wysiwygText")
			.build();
		PolicyDocumentEntity servicePolicy = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocumentEntity policyDocumentEntity2 = PolicyDocumentEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();
		PolicyDocumentEntity sitePolicy = policyDocumentEntityRepository.save(policyDocumentEntity2);

		FenixUserId fenixUserId = initUserWithAccessToSiteAndServicePolicies(servicePolicy, sitePolicy);


		Set<FenixUserId> userIds = repository.findAllPolicyUsers(siteId, new PolicyId(sitePolicy.getId()));

		assertThat(userIds.size()).isEqualTo(1);
		assertThat(userIds.iterator().next()).isEqualTo(fenixUserId);
	}

	@Test
	void shouldFindAllUsersPolicies() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("wysiwygText")
			.build();
		PolicyDocumentEntity servicePolicy = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocumentEntity policyDocumentEntity2 = PolicyDocumentEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();
		PolicyDocumentEntity sitePolicy = policyDocumentEntityRepository.save(policyDocumentEntity2);

		FenixUserId fenixUserId = initUserWithAccessToSiteAndServicePolicies(servicePolicy, sitePolicy);


		Map<FenixUserId, Set<PolicyDocument>> allUsersPolicies = repository.findAllUsersPolicies(siteId);

		assertThat(allUsersPolicies.size()).isEqualTo(1);
		assertThat(allUsersPolicies.containsKey(fenixUserId)).isTrue();
		assertThat(allUsersPolicies.get(fenixUserId).size()).isEqualTo(2);
		assertThat(allUsersPolicies.get(fenixUserId).stream().map(pd -> pd.id.id).collect(toSet())).contains(servicePolicy.getId(), sitePolicy.getId());
	}

	@Test
	void shouldFindAllByUserId() {
		FenixUserId fenixUserId = initUserWithAccessToSiteAndServicePolicies();
		LocalDateTime now = LocalDateTime.now();
		Set<PolicyDocumentExtended> policyDocuments = repository.findAllByUserId(fenixUserId, (x,y) -> now);

		assertThat(policyDocuments.size()).isEqualTo(2);
	}

	private FenixUserId initUserWithAccessToSiteAndServicePolicies() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("wysiwygText")
			.build();
		PolicyDocumentEntity servicePolicy = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocumentEntity policyDocumentEntity2 = PolicyDocumentEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();
		PolicyDocumentEntity sitePolicy = policyDocumentEntityRepository.save(policyDocumentEntity2);
		return initUserWithAccessToSiteAndServicePolicies(servicePolicy, sitePolicy);
	}

	private FenixUserId initUserWithAccessToSiteAndServicePolicies(PolicyDocumentEntity servicePolicy, PolicyDocumentEntity sitePolicy) {
		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		CommunityId communityId = communityRepository.create(community);

		Project project = Project.builder()
			.communityId(communityId)
			.name("name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		ProjectId projectId = projectRepository.create(project);

		FenixUserId fenixUserId = new FenixUserId("id");
		userSiteAccessRepository.add(siteId, projectId, fenixUserId);

		InfraService service = InfraService.builder()
			.siteId(siteId)
			.name("serviceName")
			.policyId(new PolicyId(servicePolicy.getId()))
			.build();

		InfraServiceId serviceId = infraServiceRepository.create(service);

		ResourceType resourceType = ResourceType.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.KILO)
			.build();
		ResourceTypeId resourceTypeId = resourceTypeRepository.create(resourceType);

		ResourceCreditId resourceCreditId = resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.splittable(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build()
		);

		CommunityAllocationId communityAllocationId = communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		ProjectAllocationId projectAllocationId = projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("anem")
				.amount(new BigDecimal(5))
				.build()
		);

		GrantAccess grantAccess = GrantAccess.builder()
			.fenixUserId(fenixUserId)
			.siteId(siteId)
			.projectId(projectId)
			.allocationId(projectAllocationId)
			.build();
		resourceAccessRepository.create(CorrelationId.randomID(), grantAccess, AccessStatus.GRANTED);

		Site updateSite = Site.builder()
			.id(siteId)
			.name("name2")
			.connectionInfo("alala")
			.policyId(new PolicyId(sitePolicy.getId()))
			.build();
		siteRepository.update(updateSite);
		return fenixUserId;
	}

	@Test
	void shouldCreateTextPolicyDocument() {
		PolicyDocument policyDocument = PolicyDocument.builder()
			.siteId(siteId)
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

		assertThat(policyDocumentEntity.get().siteId).isEqualTo(policyDocument.siteId.id);
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
			.siteId(siteId)
			.name("name")
			.workflow(PolicyWorkflow.PAPER_BASED)
			.revision(0)
			.contentType(PolicyContentType.PDF)
			.wysiwygText(" ")
			.file(new byte[1], "pdf", "name-rev0")
			.build();

		PolicyId policyId = repository.create(policyDocument);

		Optional<PolicyDocumentEntity> policyDocumentEntity = policyDocumentEntityRepository.findById(policyId.id);

		assertThat(policyDocumentEntity).isPresent();

		assertThat(policyDocumentEntity.get().siteId).isEqualTo(policyDocument.siteId.id);
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
			.siteId(siteId.id)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(new PolicyId(saved.getId()))
			.siteId(siteId)
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.revision(0)
			.contentType(PolicyContentType.PDF)
			.wysiwygText(" ")
			.file(new byte[1], "pdf", "name-rev0")
			.build();

		repository.update(policyDocument, true);


		Optional<PolicyDocumentEntity> readPolicyDocumentEntity = policyDocumentEntityRepository.findById(saved.getId());
		assertThat(readPolicyDocumentEntity).isPresent();

		assertThat(readPolicyDocumentEntity.get().siteId).isEqualTo(policyDocument.siteId.id);
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
			.siteId(siteId.id)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(new PolicyId(saved.getId()))
			.siteId(siteId)
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.revision(0)
			.contentType(PolicyContentType.PDF)
			.wysiwygText(" ")
			.file(new byte[1], "pdf", "name-rev0")
			.build();

		repository.update(policyDocument, false);


		Optional<PolicyDocumentEntity> readPolicyDocumentEntity = policyDocumentEntityRepository.findById(saved.getId());
		assertThat(readPolicyDocumentEntity).isPresent();

		assertThat(readPolicyDocumentEntity.get().siteId).isEqualTo(policyDocument.siteId.id);
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
			.siteId(siteId.id)
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
			.siteId(siteId.id)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		policyDocumentEntityRepository.save(policyDocumentEntity);

		assertThat(repository.isNamePresent(siteId, "name2")).isTrue();
	}

	@Test
	void shouldNameNotBePresent() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		policyDocumentEntityRepository.save(policyDocumentEntity);

		assertThat(repository.isNamePresent(siteId, "name")).isFalse();
	}


}