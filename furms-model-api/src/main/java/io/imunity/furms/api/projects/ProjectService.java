/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.projects;

import io.imunity.furms.domain.projects.ProjectAdminControlledAttributes;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProjectService {
	Optional<Project> findById(String id);

	Set<Project> findAll(String communityId);

	Set<Project> findAll();

	void create(Project project);

	void update(Project project);

	void update(ProjectAdminControlledAttributes project);

	void delete(String projectId, String communityId);

	List<User> findAllAdmins(String communityId, String projectId);

	boolean isAdmin(String projectId);

	void addAdmin(String communityId, String projectId, String userId);

	void inviteAdmin(String communityId, String projectId, String userId);

	void removeAdmin(String communityId, String projectId, String userId);

	List<User> findAllUsers(String communityId, String projectId);

	List<User> findAllUsers(String communityId);

	boolean isUser(String projectId);

	void addUser(String communityId, String projectId, String userId);

	void inviteUser(String communityId, String projectId, String email);

	void removeUser(String communityId, String projectId, String userId);
}
