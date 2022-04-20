/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_site_access;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.user_site_access.UserSiteAccessRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

@Repository
class UserSiteAccessDatabaseRepository implements UserSiteAccessRepository {
	private final UserSiteAccessEntityRepository userSiteAccessEntityRepository;

	UserSiteAccessDatabaseRepository(UserSiteAccessEntityRepository userSiteAccessEntityRepository) {
		this.userSiteAccessEntityRepository = userSiteAccessEntityRepository;
	}

	@Override
	public Set<String> findAllUserProjectIds(SiteId siteId, FenixUserId userId) {
		return userSiteAccessEntityRepository.findAllBySiteIdAndUserId(siteId.id, userId.id).stream()
			.map(userSiteAccessEntity -> userSiteAccessEntity.projectId.toString())
			.collect(toSet());
	}

	@Override
	public Map<SiteId, Set<FenixUserId>> findAllUserGroupedBySiteId(ProjectId projectId) {
		return userSiteAccessEntityRepository.findAllByProjectId(projectId.id).stream()
			.collect(groupingBy(
				entity -> new SiteId(entity.siteId.toString()),
				mapping(entity -> new FenixUserId(entity.userId), toSet()))
			);
	}

	@Override
	public void add(SiteId siteId, ProjectId projectId, FenixUserId userId){
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId.id, projectId.id, userId.id));
	}

	@Override
	public void remove(SiteId siteId, ProjectId projectId, FenixUserId userId){
		userSiteAccessEntityRepository.deleteBy(siteId.id, projectId.id, userId.id);
	}

	@Override
	public void remove(ProjectId projectId, FenixUserId userId){
		userSiteAccessEntityRepository.deleteBy(projectId.id, userId.id);
	}

	@Override
	public boolean exists(SiteId siteId, ProjectId projectId, FenixUserId userId){
		return userSiteAccessEntityRepository.existsBySiteIdAndProjectIdAndUserId(siteId.id, projectId.id, userId.id);
	}

	@Override
	public void deleteAll() {
		userSiteAccessEntityRepository.deleteAll();
	}
}
