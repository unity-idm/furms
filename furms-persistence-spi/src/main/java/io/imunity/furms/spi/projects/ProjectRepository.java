/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.projects;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;

import java.util.Optional;
import java.util.Set;

public interface ProjectRepository {
	Optional<Project> findById(ProjectId id);

	Set<Project> findAllByCommunityId(CommunityId communityId);

	Set<Project> findAllByCommunityIds(Set<CommunityId> communityIds);

	Set<Project> findAllNotExpiredByCommunityId(CommunityId communityId);

	Set<Project> findAll();

	Set<Project> findAll(Set<ProjectId> ids);

	String create(Project community);

	void update(Project community);

	boolean exists(ProjectId id);

	boolean isProjectRelatedWithCommunity(CommunityId communityId, ProjectId projectId);

	boolean isNamePresent(CommunityId communityId, String name);

	void delete(ProjectId id);
	
	void deleteAll();
}
