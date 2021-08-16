/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.workshop;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.resource_credits.CreateResourceCreditEvent;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentService;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.sites.SiteGroupDAO;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

@Component
@Profile(WorkshopService.ACTIVATION_PROFILE_NAME)
class WorkshopService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	static final String ACTIVATION_PROFILE_NAME = "workshop";

	private final CommunityRepository communityRepository;
	private final CommunityGroupsDAO communityGroupsDAO;
	private final SiteRepository siteRepository;
	private final SiteGroupDAO siteGroupDAO;
	private final UsersDAO usersDAO;
	private final ResourceCreditRepository resourceCreditRepository;
	private final CommunityAllocationRepository communityAllocationRepository;
	private final SiteAgentService siteAgentService;

	WorkshopService(CommunityRepository communityRepository,
			CommunityGroupsDAO communityGroupsDAO,
			SiteRepository siteRepository,
			SiteGroupDAO siteGroupDAO,
			UsersDAO usersDAO,
			ResourceCreditRepository resourceCreditRepository,
			CommunityAllocationRepository communityAllocationRepository,
			SiteAgentService siteAgentService) {
		this.communityRepository = communityRepository;
		this.communityGroupsDAO = communityGroupsDAO;
		this.siteRepository = siteRepository;
		this.siteGroupDAO = siteGroupDAO;
		this.usersDAO = usersDAO;
		this.resourceCreditRepository = resourceCreditRepository;
		this.communityAllocationRepository = communityAllocationRepository;
		this.siteAgentService = siteAgentService;
	}

	
	@Async
	@EventListener
	void distributeToMateCommunity(CreateResourceCreditEvent event) {
		LOG.info("Received credit allocation event {}", event);
		Optional<ResourceCredit> credit = resourceCreditRepository.findById(event.id);
		credit.ifPresent(rc -> {
			getCorrespondingCommunityId(rc, event.originator).ifPresent(community -> {
				LOG.info("Making community allocation from {} to {}", rc, community);
				CommunityAllocation communityAllocation = CommunityAllocation.builder()
						.communityId(community.getId())
						.resourceCreditId(rc.id)
						.name("Allocation " + ExternalIdGenerator.generate(id -> true))
						.amount(rc.amount)
						.build();
				communityAllocationRepository.create(communityAllocation);
			});
		});
	}
	
	private Optional<Community> getCorrespondingCommunityId(ResourceCredit rc, PersistentId originator) {
		Optional<Site> site = siteRepository.findById(rc.siteId);
		String key = site.get().getExternalId().id;
		
		final Set<Community> communities = communityRepository.findAll();
		Optional<Community> community = communities.stream()
				.filter(c -> c.getName().contains(key))
				.findAny();
		
		Optional<FURMSUser> user = usersDAO.findById(originator);
		if (user.isEmpty() || user.get().fenixUserId.isEmpty())
		{
			LOG.info("No corresponding community for user w/o fenix id");
			return Optional.empty();
		}
		
		if (community.isEmpty()) {
			LOG.info("Community name has been changed, using different heuristic basing on {}", originator);
			for (Community c : communities)
			{
				boolean isOriginatorAdmin = communityGroupsDAO.getAllAdmins(c.getId()).stream()
					.map(u -> u.id)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.anyMatch(id -> id.equals(originator));
				if (isOriginatorAdmin)
				{
					community = Optional.of(c);
					break;
				}
			}
		}
		
		return community;
	}
	
	void equipUsers() {
		
		Set<Site> allSites = siteRepository.findAll();
		Set<Community> allCommunities = communityRepository.findAll();
		usersDAO.getAllUsers().forEach(user -> equipUser(user, allSites, allCommunities));
	}
	
	private void equipUser(FURMSUser user, Set<Site> allSites, Set<Community> allCommunities) {
		SiteExternalId siteId = addSiteAndMakeUserAdmin(user, allSites);
		addCommunityAndMakeUserAdmin(user, siteId, allCommunities);
	}

	private void addCommunityAndMakeUserAdmin(FURMSUser user, SiteExternalId siteId, Set<Community> allCommunities) {
		String emailHandle = getUserEmailHandle(user);
		
		if (allCommunities.stream().anyMatch(c -> c.getDescription().contains(emailHandle)))
			return;
		
		LOG.info("Adding community for for {}", user.email);
		Community community = Community.builder()
				.name("PRACE-" + siteId.id)
				.description("Community for " + emailHandle)
				.logo(FurmsImage.empty())
				.build(); 
		String communityId = communityRepository.create(community);
		communityGroupsDAO.create(new CommunityGroup(communityId, community.getName()));
		
		LOG.info("Making user {} community {} admin", user.email, communityId);
		communityGroupsDAO.addAdmin(communityId, user.id.orElseThrow());
	}

	private SiteExternalId addSiteAndMakeUserAdmin(FURMSUser user, Set<Site> allSites) {
		
		String emailHandle = getUserEmailHandle(user);
		
		final Predicate<? super Site> siteForUser = site -> 
			site.getConnectionInfo() != null && site.getConnectionInfo().contains(emailHandle);
		if (allSites.stream().anyMatch(siteForUser))
			return allSites.stream().filter(siteForUser).findAny().get().getExternalId();
		
		LOG.info("Adding site for {}", user.email);
		final SiteExternalId siteExternalId = generateSiteId();
		
		final Site site = Site.builder()
			.name("SITE-" + siteExternalId.id)
			.connectionInfo("Site for " + emailHandle)
			.build();
		String siteId = siteRepository.create(site, siteExternalId);
		
		siteAgentService.initializeSiteConnection(siteExternalId);
		siteGroupDAO.create(Site.builder().id(siteId).name(site.getName()).build());
		
		LOG.info("Making user {} site {} admin", user.email, siteId);
		siteGroupDAO.addSiteUser(siteId, user.id.orElseThrow(), Role.SITE_ADMIN);
		
		return siteExternalId;
	}
	
	private SiteExternalId generateSiteId() {
		return new SiteExternalId(ExternalIdGenerator.generate(siteExternalId ->
			!siteRepository.existsByExternalId(new SiteExternalId(siteExternalId))));
	}

	private String getUserEmailHandle(FURMSUser user) {
		return user.email.split("@")[0];
	}

	private static class ExternalIdGenerator {
		private static final char[] CHARS = "346789bcdfghjkmpqrtvwxy".toCharArray();
		private static final int GENERATED_ID_SIZE = 5;
		private static final Random RANDOM = new Random();

		public static String generate(Predicate<String> uniquenessPred) {
			do {
				String candidate = generate();
				if (uniquenessPred.test(candidate))
					return candidate;
			} while (true);
		}

		private static String generate() {
			return generate(GENERATED_ID_SIZE, RANDOM, CHARS);
		}

		private static String generate(int size, Random random, char[] alphabet) {
			char[] chars = new char[size];
			for (int i = 0; i < size; i++)
				chars[i] = alphabet[random.nextInt(alphabet.length)];
			String candidate = String.valueOf(chars);
			return candidate;
		}
	}
}
