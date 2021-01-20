/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.server;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.sites.SiteWebClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
@Profile("demo-data-provisioning")
class DemoDataInitializer {
	private final CommunityRepository communityRepository;
	private final CommunityGroupsDAO communityGroupsDAO;

	private final SiteRepository siteRepository;
	private final SiteWebClient siteWebClient;

	public DemoDataInitializer(
			CommunityRepository communityRepository,
			CommunityGroupsDAO communityGroupsDAO,
			SiteRepository siteRepository,
			SiteWebClient siteWebClient) {
		this.communityRepository = communityRepository;
		this.communityGroupsDAO = communityGroupsDAO;
		this.siteRepository = siteRepository;
		this.siteWebClient = siteWebClient;
	}

	@PostConstruct
	public void init() throws IOException {
		initCommunities();
		initSites();
	}

	private void initCommunities() throws IOException {
		if(communityRepository.findAll().isEmpty()) {
			byte[] imgHBPFile = getClass().getClassLoader().getResourceAsStream("demo/HBP.png").readAllBytes();
			Community community = Community.builder()
					.name("HBP")
					.description("Human Brain Project")
					.logo(imgHBPFile, "png")
					.build();

			byte[] imgPRACEFile = getClass().getClassLoader().getResourceAsStream("demo/PRACE.png").readAllBytes();
			Community community2 = Community.builder()
					.name("PRACE")
					.description("Partnership for Advance Computing")
					.logo(imgPRACEFile, "png")
					.build();

			String communityId = communityRepository.create(community);
			communityGroupsDAO.create(new CommunityGroup(communityId, community.getName()));
			String community2Id = communityRepository.create(community2);
			communityGroupsDAO.create(new CommunityGroup(community2Id, community.getName()));
		}
	}

	private void initSites() {
		if (siteRepository.findAll().isEmpty()) {
			Site cineca = Site.builder()
					.name("CINECA")
					.build();
			Site fzj = Site.builder()
					.name("FZJ")
					.build();
			Site bsc = Site.builder()
					.name("BSC")
					.build();

			String cinecaId = siteRepository.create(cineca);
			String fzjId = siteRepository.create(fzj);
			String bscId = siteRepository.create(bsc);

			siteWebClient.create(Site.builder().id(cinecaId).name(cineca.getName()).build());
			siteWebClient.create(Site.builder().id(fzjId).name(fzj.getName()).build());
			siteWebClient.create(Site.builder().id(bscId).name(bsc.getName()).build());
		}
	}
}
