/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.projects;

import io.imunity.furms.domain.projects.ProjectAdminControlledAttributes;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProjectService {
	Optional<Project> findById(String id);

	Set<Project> findAll(String communityId);

	Set<Project> findAll();

	boolean isProjectInTerminalState(String projectId);

	boolean isProjectInTerminalState(String communityId, String projectId);

	void create(Project project);

	void update(Project project);

	void update(ProjectAdminControlledAttributes project);

	void delete(String projectId, String communityId);

	List<FURMSUser> findAllAdmins(String communityId, String projectId);

	boolean isAdmin(String projectId);

	void addAdmin(String communityId, String projectId, PersistentId userId);

	void inviteAdmin(String communityId, String projectId, PersistentId userId);

	void removeAdmin(String communityId, String projectId, PersistentId userId);

	List<FURMSUser> findAllUsers(String communityId, String projectId);

	List<FURMSUser> findAllUsers(String communityId);

	boolean isUser(String projectId);

	void addUser(String communityId, String projectId, PersistentId userId);

	void inviteUser(String communityId, String projectId, PersistentId userId);

	void removeUser(String communityId, String projectId, PersistentId userId);

	void resignFromMembership(String communityId, String projectId);
}
