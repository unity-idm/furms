/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.performance.tests.data;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.rabbitmq.site.models.CumulativeResourceUsageRecord;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Status;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static io.imunity.furms.performance.tests.SecurityUserUtils.createSecurityUser;
import static io.imunity.furms.performance.tests.data.DataLoaderUtils.randomAcronym;
import static io.imunity.furms.performance.tests.data.DataLoaderUtils.randomName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@SpringBootTest
@Profile("performance_tests")
class MessageHandleSpeedTest {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired private CommunityRepository communityRepository;
	@Autowired private CommunityAllocationRepository communityAllocationRepository;
	@Autowired private ProjectAllocationRepository projectAllocationRepository;
	@Autowired private ProjectRepository projectRepository;
	@Autowired private ResourceCreditRepository resourceCreditRepository;
	@Autowired private ResourceTypeRepository resourceTypeRepository;
	@Autowired private ResourceUsageRepository resourceUsageRepository;
	@Autowired private InfraServiceRepository serviceRepository;
	@Autowired private SiteService siteService;
	@Autowired private RabbitTemplate rabbitTemplate;

	private final static long BIG_COMMUNITIES_PROJECTS_COUNT = 2400;
	private final static long SLEEP_INTERVAL = 5;

	@Test
	@Timeout(value = 200)
	void shouldHandle1000MessagesInTimeLessThen200s() throws InterruptedException {
		//given
		SiteId siteId = createSite();
		String communityId = createCommunity();
		String communityAllocationId = createCommunityAllocation(communityId, siteId.id);
		List<Pair<String, String>> projectAndAllocationIds = createProjectAndAllocations(communityId,
			communityAllocationId);

		//when
		send1000Messages(siteId, projectAndAllocationIds);

		int i = 0;
		UUID communityAllocId = UUID.fromString(communityAllocationId);

		do {
			TimeUnit.SECONDS.sleep(SLEEP_INTERVAL);
			i++;
		} while (resourceUsageRepository.findResourceUsagesHistoryByCommunityAllocationId(communityAllocId).size() < 1000);

		//then
		LOG.info("Handled messages in time less then: {} seconds", i * SLEEP_INTERVAL);
	}

	private String createCommunity() {
		return communityRepository.create(Community.builder()
			.name("community-" + randomName())
			.description(UUID.randomUUID().toString())
			.logo(FurmsImage.empty())
			.build());
	}

	private SiteId createSite() {
		createSecurityUser(Map.of());
		siteService.create(Site.builder()
			.name(randomName())
			.build());
		return siteService.findAll()
			.stream().findAny()
			.map(x -> new SiteId(x.getId(), x.getExternalId()))
			.orElseThrow(IllegalArgumentException::new);
	}

	private void send1000Messages(SiteId siteId, List<Pair<String, String>> projectAndAllocationIds) {
		int size = projectAndAllocationIds.size();
		Random random = new Random();
		LongStream.range(0, 1000).parallel()
				.forEach(x -> {
					Pair<String, String> stringStringPair =
						projectAndAllocationIds.get(random.nextInt(size));
					rabbitTemplate.convertAndSend(siteId.externalId.id + "-site-pub", new Payload<>(
						new Header(VERSION, UUID.randomUUID().toString(), Status.OK, null),
						new CumulativeResourceUsageRecord(
							stringStringPair.getKey(),
							stringStringPair.getValue(),
							new BigDecimal(20),
							OffsetDateTime.now()
						)
					));
				});
	}

	private List<Pair<String, String>> createProjectAndAllocations(String communityId, String communityAllocationId) {
		return LongStream.range(0, BIG_COMMUNITIES_PROJECTS_COUNT)
			.mapToObj(i -> {
					String projectId = projectRepository.create(Project.builder()
						.communityId(communityId)
						.name(randomName())
						.acronym(randomAcronym())
						.researchField(UUID.randomUUID().toString())
						.utcStartTime(LocalDateTime.now())
						.utcEndTime(LocalDateTime.now().plusYears(1))
						.build());
					String allocation_id = projectAllocationRepository.create(
						ProjectAllocation.builder()
							.projectId(projectId)
							.name(randomName())
							.amount(new BigDecimal(20))
							.communityAllocationId(communityAllocationId)
							.build()
					);
					return Pair.of(projectId, allocation_id);
				}
			).collect(Collectors.toList());
	}

	private String createCommunityAllocation(String communityId, String siteId) {
		InfraService infraService = InfraService.builder()
			.name(randomName())
			.siteId(siteId)
			.description("Archive Bsc")
			.build();
		String infraId = serviceRepository.create(infraService);

		ResourceType resourceType = ResourceType.builder()
			.siteId(siteId)
			.serviceId(infraId)
			.name(randomName())
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.KB)
			.build();
		String resourceId = resourceTypeRepository.create(resourceType);

		ResourceCredit resourceCredit = ResourceCredit.builder()
			.siteId(siteId)
			.resourceTypeId(resourceId)
			.name(randomName())
			.amount(new BigDecimal(2400000))
			.utcStartTime(LocalDateTime.of(2021, 10, 22, 11, 22))
			.utcEndTime(LocalDateTime.of(2024, 12, 8, 17, 32))
			.build();
		String resourceCreditId = resourceCreditRepository.create(resourceCredit);


		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name(randomName())
			.amount(new BigDecimal(2400000))
			.build();
		return communityAllocationRepository.create(communityAllocation);
	}
}
