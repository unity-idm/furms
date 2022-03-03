/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.projects;

import io.imunity.furms.domain.projects.Project;

import java.util.Optional;
import java.util.Set;

public interface ProjectRepository {
	Optional<Project> findById(String id);

	Set<Project> findAllByCommunityId(String communityId);

	Set<Project> findAllByCommunityIds(Set<String> communityIds);

	Set<Project> findAllNotExpiredByCommunityId(String communityId);

	Set<Project> findAll();

	Set<Project> findAll(Set<String> ids);

	String create(Project community);

	void update(Project community);

	boolean exists(String id);

	boolean isProjectRelatedWithCommunity(String communityId, String projectId);

	boolean isNamePresent(String communityId, String name);

	void delete(String id);
	
	void deleteAll();
}
