/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.server;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class TestCommunityInitializer {
	public final CommunityRepository communityRepository;

	public TestCommunityInitializer(CommunityRepository communityRepository) {
		this.communityRepository = communityRepository;
	}

	@PostConstruct
	public void init() throws IOException {
		if(communityRepository.findAll().isEmpty()) {
			byte[] imgTestFile = getClass().getClassLoader().getResourceAsStream("logo_test.jpg").readAllBytes();
			Community community = Community.builder()
				.name("HBP")
				.description("Human Brain Project")
				.logoImage(imgTestFile)
				.build();

			Community community2 = Community.builder()
				.name("PRACE")
				.description("Partnership for Advance Computing")
				.logoImage(imgTestFile)
				.build();

			communityRepository.create(community);
			communityRepository.create(community2);
		}
	}
}
