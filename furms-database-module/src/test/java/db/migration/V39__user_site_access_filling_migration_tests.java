/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package db.migration;

import io.imunity.furms.db.user_site_access.UserSiteAccessEntity;
import io.imunity.furms.db.user_site_access.UserSiteAccessEntityRepository;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.images.FurmsImage;
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
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.assertj.core.groups.Tuple;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class V39__user_site_access_filling_migration_tests {

	@Autowired
	private ResourceAccessRepository repository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private UserSiteAccessEntityRepository userSiteAccessEntityRepository;
	@Autowired
	private InfraServiceRepository infraServiceRepository;
	@Autowired
	private ResourceTypeRepository resourceTypeRepository;
	@Autowired
	private ResourceCreditRepository resourceCreditRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private Flyway flyway;

	private SiteId siteId;
	private ProjectId projectId;

	private SiteId siteId1;
	private ProjectId projectId1;

	private ProjectAllocationId projectAllocationId;

	@BeforeEach
	void setUp() {
		flyway.clean();
		Flyway.configure()
				.configuration(flyway.getConfiguration())
				.target("38")
				.load()
				.migrate();

		Site site = Site.builder()
			.name("name")
			.build();

		siteId = siteRepository.create(site, new SiteExternalId("id"));

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
		projectId = projectRepository.create(project);


		Site site1 = Site.builder()
			.name("name1")
			.build();

		siteId1 = siteRepository.create(site1, new SiteExternalId("id1"));

		Community community1 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		CommunityId communityId1 = communityRepository.create(community1);

		Project project1 = Project.builder()
			.communityId(communityId1)
			.name("name1")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();
		projectId1 = projectRepository.create(project1);

		InfraService service = InfraService.builder()
			.siteId(siteId)
			.name("name")
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
			.build());

		UUID communityAllocationId = UUID.randomUUID();
		jdbcTemplate.update(
			"INSERT INTO community_allocation (id, community_id, resource_credit_id, name, amount) VALUES (?, ?, ?, ?, ?)",
			communityAllocationId, communityId.id, resourceCreditId.id, "anem", new BigDecimal(10)
		);
		projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		jdbcTemplate.update(
			"INSERT INTO project_allocation (id, project_id, community_allocation_id, name, amount) VALUES (?, ?, ?, ?, ?)",
			projectAllocationId.id, projectId.id, communityAllocationId, "anem", new BigDecimal(5)
		);
	}

	@AfterEach
	void tearDown() {
		flyway.clean();
		flyway.migrate();
	}

	@Test
	void shouldReduceProjectInstallationsToUniqueSiteIdAndProjectId() {
		repository.create(
			CorrelationId.randomID(),
			GrantAccess.builder()
				.siteId(siteId)
				.projectId(projectId)
				.allocationId(projectAllocationId)
				.fenixUserId(new FenixUserId("userId"))
				.build(),
			AccessStatus.GRANT_ACKNOWLEDGED
		);
		repository.create(
			CorrelationId.randomID(),
			GrantAccess.builder()
				.siteId(siteId)
				.projectId(projectId1)
				.allocationId(projectAllocationId)
				.fenixUserId(new FenixUserId("userId"))
				.build(),
			AccessStatus.GRANT_ACKNOWLEDGED
		);
		repository.create(
			CorrelationId.randomID(),
			GrantAccess.builder()
				.siteId(siteId1)
				.projectId(projectId)
				.allocationId(projectAllocationId)
				.fenixUserId(new FenixUserId("userId"))
				.build(),
			AccessStatus.GRANT_ACKNOWLEDGED
		);
		repository.create(
			CorrelationId.randomID(),
			GrantAccess.builder()
				.siteId(siteId1)
				.projectId(projectId1)
				.allocationId(projectAllocationId)
				.fenixUserId(new FenixUserId("userId"))
				.build(),
			AccessStatus.GRANT_ACKNOWLEDGED
		);
		repository.create(
			CorrelationId.randomID(),
			GrantAccess.builder()
				.siteId(siteId1)
				.projectId(projectId1)
				.allocationId(projectAllocationId)
				.fenixUserId(new FenixUserId("userId1"))
				.build(),
			AccessStatus.GRANT_ACKNOWLEDGED
		);

		V39__user_site_access_filling.migrate(jdbcTemplate);

		Iterable<UserSiteAccessEntity> all = userSiteAccessEntityRepository.findAll();
		assertEquals(all.spliterator().getExactSizeIfKnown(), 5);
		Set<Tuple> siteIdAndProjectIdAndUserIdTuples = stream(all.spliterator(), false)
			.map(entity -> Tuple.tuple(entity.siteId, entity.projectId, entity.userId))
			.collect(Collectors.toSet());
		assertEquals(
			Set.of(
				Tuple.tuple(siteId.id, projectId.id, "userId"),
				Tuple.tuple(siteId.id, projectId1.id, "userId"),
				Tuple.tuple(siteId1.id, projectId.id, "userId"),
				Tuple.tuple(siteId1.id, projectId1.id, "userId"),
				Tuple.tuple(siteId1.id, projectId1.id, "userId1")
			),
			siteIdAndProjectIdAndUserIdTuples
		);
	}
}