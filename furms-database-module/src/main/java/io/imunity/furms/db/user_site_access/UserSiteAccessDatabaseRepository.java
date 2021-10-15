/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_site_access;

import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.user_site_access.UserSiteAccessRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
class UserSiteAccessDatabaseRepository implements UserSiteAccessRepository {
	private final UserSiteAccessEntityRepository userSiteAccessEntityRepository;

	UserSiteAccessDatabaseRepository(UserSiteAccessEntityRepository userSiteAccessEntityRepository) {
		this.userSiteAccessEntityRepository = userSiteAccessEntityRepository;
	}

	@Override
	public Set<String> findAllUserProjectIds(String siteId, FenixUserId userId) {
		return userSiteAccessEntityRepository.findAllBySiteIdAndUserId(UUID.fromString(siteId), userId.id).stream()
			.map(userSiteAccessEntity -> userSiteAccessEntity.projectId.toString())
			.collect(Collectors.toSet());
	}

	@Override
	public void add(String siteId, String projectId, FenixUserId userId){
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(UUID.fromString(siteId), UUID.fromString(projectId), userId.id));
	}

	@Override
	public void remove(String siteId, String projectId, FenixUserId userId){
		userSiteAccessEntityRepository.deleteBy(UUID.fromString(siteId), UUID.fromString(projectId), userId.id);
	}

	@Override
	public boolean exists(String siteId, String projectId, FenixUserId userId){
		return userSiteAccessEntityRepository.existsBySiteIdAndProjectIdAndUserId(UUID.fromString(siteId), UUID.fromString(projectId), userId.id);
	}
}
