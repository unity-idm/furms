/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.server;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
@Profile("demo-data-provisioning")
class DemoCommunityInitializer {
	private final CommunityService communityRepository;

	DemoCommunityInitializer(CommunityService CommunityService) {
		this.communityRepository = CommunityService;
	}

	@PostConstruct
	public void init() throws IOException {
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

			communityRepository.create(community);
			communityRepository.create(community2);
		}
	}
}
