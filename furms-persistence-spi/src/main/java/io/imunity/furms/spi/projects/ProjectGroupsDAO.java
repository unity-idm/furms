/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.projects;

import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.users.FURMSUser;

import java.util.List;
import java.util.Optional;

public interface ProjectGroupsDAO {
	Optional<ProjectGroup> get(String communityId, String projectId);

	void create(ProjectGroup projectGroup);

	void update(ProjectGroup projectGroup);

	void delete(String communityId, String projectId);

	List<FURMSUser> getAllAdmins(String communityId, String projectId);
	boolean isAdmin(String communityId, String projectId, String userId);
	void addAdmin(String communityId, String projectId, String userId);
	void removeAdmin(String communityId, String projectId, String userId);

	List<FURMSUser> getAllUsers(String communityId, String projectId);
	List<FURMSUser> getAllUsers(String communityId);
	boolean isUser(String communityId, String projectId, String userId);
	void addUser(String communityId, String projectId, String userId);
	void removeUser(String communityId, String projectId, String userId);
}