/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.transaction;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ServiceMockImpl implements ServiceMock{
	private final CommunityRepository communityRepository;
	private final WebClient webClient;

	public ServiceMockImpl(CommunityRepository communityRepository, WebClient webClient) {
		this.communityRepository = communityRepository;
		this.webClient = webClient;
	}

	@Transactional
	public void update(Community community) {
		communityRepository.update(community);
		webClient.update(community);
	}
}
