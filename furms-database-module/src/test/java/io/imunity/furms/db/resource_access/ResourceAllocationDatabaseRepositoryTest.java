/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrant;
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

import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_FAILED;
import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_PENDING;
import static io.imunity.furms.domain.resource_access.AccessStatus.REVOKE_PENDING;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ResourceAllocationDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
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
	private UserGrantEntityRepository userGrantEntityRepository;
	@Autowired
	private UserGrantJobEntityRepository userGrantJobEntityRepository;
	@Autowired
	private ResourceAccessDatabaseRepository resourceAccessDatabaseRepository;

	private SiteId siteId;
	private ProjectId projectId;
	private ProjectAllocationId projectAllocationId;


	@BeforeEach
	void init() {
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

		CommunityAllocationId communityAllocationId = communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		projectAllocationId = projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("anem")
				.amount(new BigDecimal(5))
				.build()
		);
	}

	@Test
	void shouldUpdate(){
		CorrelationId correlationId = CorrelationId.randomID();
		UserGrantEntity userAllocation = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(siteId.id)
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.userId("userId")
				.build()
		);
		userGrantJobEntityRepository.save(UserGrantJobEntity.builder()
			.userAllocationId(userAllocation.getId())
			.status(AccessStatus.REVOKED)
			.correlationId(UUID.randomUUID())
			.build()
		);

		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId(siteId.id.toString(), "mock"))
			.allocationId(projectAllocationId)
			.projectId(projectId)
			.fenixUserId(new FenixUserId("userId"))
			.build();

		resourceAccessDatabaseRepository.update(correlationId, grantAccess, REVOKE_PENDING);

		Optional<UserGrantResolved> userAllocationResolved = userGrantEntityRepository.findByUserIdAndProjectAllocationId("userId", projectAllocationId.id);
		assertThat(userAllocationResolved).isPresent();
		assertThat(userAllocationResolved.get().allocation).isEqualTo(userAllocation);
		assertThat(userAllocationResolved.get().job.status).isEqualTo(REVOKE_PENDING.getPersistentId());
	}

	@Test
	void shouldDelete(){
		CorrelationId correlationId = CorrelationId.randomID();
		UserGrantEntity userAllocation = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(siteId.id)
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.userId("userId")
				.build()
		);
		userGrantJobEntityRepository.save(UserGrantJobEntity.builder()
			.userAllocationId(userAllocation.getId())
			.status(AccessStatus.REVOKED)
			.correlationId(UUID.fromString(correlationId.id))
			.build()
		);

		resourceAccessDatabaseRepository.deleteByCorrelationId(correlationId);

		assertThat(userGrantEntityRepository.findAll()).isEmpty();
		assertThat(userGrantJobEntityRepository.findAll()).isEmpty();
	}

	@Test
	void shouldUpdateStatus(){
		CorrelationId correlationId = CorrelationId.randomID();
		UserGrantEntity userAllocation = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(siteId.id)
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.userId("userId")
				.build()
		);
		userGrantJobEntityRepository.save(UserGrantJobEntity.builder()
			.userAllocationId(userAllocation.getId())
			.status(AccessStatus.REVOKED)
			.correlationId(UUID.fromString(correlationId.id))
			.build()
		);

		resourceAccessDatabaseRepository.update(correlationId, GRANT_FAILED, "msg");

		Optional<UserGrantResolved> userAllocationResolved = userGrantEntityRepository.findByUserIdAndProjectAllocationId("userId", projectAllocationId.id);
		assertThat(userAllocationResolved).isPresent();
		assertThat(userAllocationResolved.get().allocation).isEqualTo(userAllocation);
		assertThat(userAllocationResolved.get().job.status).isEqualTo(GRANT_FAILED.getPersistentId());
		assertThat(userAllocationResolved.get().job.message).isEqualTo("msg");
	}

	@Test
	void shouldCreate(){
		CorrelationId correlationId = CorrelationId.randomID();
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId(siteId.id.toString(), "mock"))
			.allocationId(projectAllocationId)
			.projectId(projectId)
			.fenixUserId(new FenixUserId("userId"))
			.build();

		resourceAccessDatabaseRepository.create(correlationId, grantAccess, GRANT_PENDING);

		Optional<UserGrantResolved> userAllocationResolved = userGrantEntityRepository.findByUserIdAndProjectAllocationId("userId", projectAllocationId.id);
		assertThat(userAllocationResolved).isPresent();
		assertThat(userAllocationResolved.get().allocation.userId).isEqualTo("userId");
		assertThat(userAllocationResolved.get().job.status).isEqualTo(GRANT_PENDING.getPersistentId());
	}

	@Test
	void shouldFindUsersGrants(){
		CorrelationId correlationId = CorrelationId.randomID();
		UserGrantEntity userAllocation = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(siteId.id)
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.userId("userId")
				.build()
		);
		UserGrantJobEntity userGrantJobEntity = UserGrantJobEntity.builder()
			.userAllocationId(userAllocation.getId())
			.status(AccessStatus.GRANTED)
			.correlationId(UUID.fromString(correlationId.id))
			.message("text")
			.build();
		userGrantJobEntityRepository.save(userGrantJobEntity);

		Set<UserGrant> userGrants = resourceAccessDatabaseRepository.findUsersGrantsByProjectId(projectId);
		assertThat(userGrants.size()).isEqualTo(1);
		UserGrant userGrant = userGrants.iterator().next();
		assertThat(userGrant.userId.id).isEqualTo("userId");
		assertThat(userGrant.projectAllocationId.id).isEqualTo(projectAllocationId.id);
		assertThat(userGrant.status).isEqualTo(AccessStatus.GRANTED);
		assertThat(userGrant.errorMessage.get().message).isEqualTo("text");
	}
}