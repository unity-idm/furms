/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.projects;

import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProjectService {
	Optional<Project> findById(String id);

	Set<Project> findAll(String communityId);

	void create(Project project);

	void update(Project project);

	void delete(String projectId, String communityId);

	List<User> findUsers(String communityId, String projectId);

	boolean isMember(String communityId, String projectId, String userId);

	void addMember(String communityId, String projectId, String userId);

	void removeMember(String communityId, String projectId, String userId);
}
