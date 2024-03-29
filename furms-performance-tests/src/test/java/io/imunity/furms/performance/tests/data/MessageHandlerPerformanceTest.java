/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.performance.tests.data;

import com.google.common.base.Stopwatch;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
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
import static org.awaitility.Awaitility.await;

/**
 * Environmental prerequisites: The following services needs to be up and running:
 * - Posgress database
 * - Rabbitmq broker
 * - Unity-IdM
 */
@SpringBootTest
class MessageHandlerPerformanceTest {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final int WARM_UP_COUNT = 100;
	public static final int TESTED_COUNT = 1000;

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
	private final static long POLL_INTERVAL = 5;

	MessageHandlerPerformanceTest() {
	}

	@Test
	void shouldHandle1000MessagesInTimeLessThen200s() {
		//given
		SiteId siteId = createSite();
		CommunityId communityId = createCommunity();
		CommunityAllocationId communityAllocationId = createCommunityAllocation(communityId, siteId);
		List<Pair<ProjectId, ProjectAllocationId>> projectAndAllocationIds = createProjectAndAllocations(communityId,
			communityAllocationId);
		//when
		//warm-up
		sendMessages(WARM_UP_COUNT, siteId, projectAndAllocationIds);
		await()
			.timeout(Duration.of(50, ChronoUnit.SECONDS))
			.pollDelay(Duration.of(POLL_INTERVAL, ChronoUnit.SECONDS))
			.pollInterval(Duration.of(POLL_INTERVAL, ChronoUnit.SECONDS))
			.until(() -> resourceUsageRepository.findResourceUsagesHistoryByCommunityAllocationId(communityAllocationId).size() >= WARM_UP_COUNT);

		//main test
		Stopwatch stopwatch = Stopwatch.createStarted();
		sendMessages(TESTED_COUNT, siteId, projectAndAllocationIds);
		await()
			.timeout(Duration.of(200, ChronoUnit.SECONDS))
			.pollDelay(Duration.of(POLL_INTERVAL, ChronoUnit.SECONDS))
			.pollInterval(Duration.of(POLL_INTERVAL, ChronoUnit.SECONDS))
			.until(() -> resourceUsageRepository.findResourceUsagesHistoryByCommunityAllocationId(communityAllocationId).size() >= WARM_UP_COUNT + TESTED_COUNT);

		//then
		LOG.info("Handled and send 1000 messages in time less then: {} seconds", stopwatch.elapsed(TimeUnit.SECONDS));
	}

	private CommunityId createCommunity() {
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
			.map(Site::getId)
			.orElseThrow(IllegalArgumentException::new);
	}

	private void sendMessages(int messagesNumber, SiteId siteId,
	                          List<Pair<ProjectId, ProjectAllocationId>> projectAndAllocationIds) {
		int size = projectAndAllocationIds.size();
		Random random = new Random();
		LongStream.range(0, messagesNumber).parallel()
			.forEach(x -> {
				Pair<ProjectId, ProjectAllocationId> stringStringPair =
					projectAndAllocationIds.get(random.nextInt(size));
				rabbitTemplate.convertAndSend(siteId.externalId.id + "-site-pub", new Payload<>(
					new Header(VERSION, UUID.randomUUID().toString(), Status.OK, null),
					new CumulativeResourceUsageRecord(
						stringStringPair.getKey().id.toString(),
						stringStringPair.getValue().id.toString(),
						new BigDecimal(20),
						OffsetDateTime.now()
					)
				));
			});
	}

	private List<Pair<ProjectId, ProjectAllocationId>> createProjectAndAllocations(CommunityId communityId,
	                                                                               CommunityAllocationId communityAllocationId) {
		return LongStream.range(0, BIG_COMMUNITIES_PROJECTS_COUNT)
			.mapToObj(i -> {
					ProjectId projectId = projectRepository.create(Project.builder()
						.communityId(communityId)
						.name(randomName())
						.acronym(randomAcronym())
						.researchField(UUID.randomUUID().toString())
						.utcStartTime(LocalDateTime.now())
						.utcEndTime(LocalDateTime.now().plusYears(1))
						.build());
					ProjectAllocationId allocation_id = projectAllocationRepository.create(
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

	private CommunityAllocationId createCommunityAllocation(CommunityId communityId, SiteId siteId) {
		InfraService infraService = InfraService.builder()
			.name(randomName())
			.siteId(siteId)
			.description("Archive Bsc")
			.build();
		InfraServiceId infraId = serviceRepository.create(infraService);

		ResourceType resourceType = ResourceType.builder()
			.siteId(siteId)
			.serviceId(infraId)
			.name(randomName())
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.KB)
			.build();
		ResourceTypeId resourceId = resourceTypeRepository.create(resourceType);

		ResourceCredit resourceCredit = ResourceCredit.builder()
			.siteId(siteId)
			.resourceTypeId(resourceId)
			.name(randomName())
			.amount(new BigDecimal(2400000))
			.utcStartTime(LocalDateTime.of(2021, 10, 22, 11, 22))
			.utcEndTime(LocalDateTime.of(2024, 12, 8, 17, 32))
			.build();
		ResourceCreditId resourceCreditId = resourceCreditRepository.create(resourceCredit);


		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name(randomName())
			.amount(new BigDecimal(2400000))
			.build();
		return communityAllocationRepository.create(communityAllocation);
	}
}
