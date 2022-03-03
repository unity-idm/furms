/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.projects;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.users.CommunityAdminsAndProjectAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;

public interface ProjectGroupsDAO {
	Optional<ProjectGroup> get(String communityId, String projectId);

	void create(ProjectGroup projectGroup);

	void update(ProjectGroup projectGroup);

	void delete(String communityId, String projectId);

	List<FURMSUser> getAllAdmins(String communityId, String projectId);
	void removeAdmin(String communityId, String projectId, PersistentId userId);

	CommunityAdminsAndProjectAdmins getAllCommunityAndProjectAdmins(String communityId, String projectId);
	List<FURMSUser> getAllUsers(String communityId, String projectId);
	List<FURMSUser> getAllProjectAdminsAndUsers(String communityId, String projectId);
	List<FURMSUser> getAllUsers(String communityId);
	void addProjectUser(String communityId, String projectId, PersistentId userId, Role role);
	void removeUser(String communityId, String projectId, PersistentId userId);
}