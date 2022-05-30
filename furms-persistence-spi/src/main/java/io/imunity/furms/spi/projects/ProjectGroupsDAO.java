/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.projects;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.CommunityAdminsAndProjectAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;

public interface ProjectGroupsDAO {
	Optional<ProjectGroup> get(CommunityId communityId, ProjectId projectId);

	void create(ProjectGroup projectGroup);

	void update(ProjectGroup projectGroup);

	void delete(CommunityId communityId, ProjectId projectId);

	List<FURMSUser> getAllAdmins(CommunityId communityId, ProjectId projectId);
	void removeAdmin(CommunityId communityId, ProjectId projectId, PersistentId userId);

	CommunityAdminsAndProjectAdmins getAllCommunityAndProjectAdmins(CommunityId communityId, ProjectId projectId);
	List<FURMSUser> getAllUsers(CommunityId communityId, ProjectId projectId);
	List<FURMSUser> getAllProjectAdminsAndUsers(CommunityId communityId, ProjectId projectId);
	List<FURMSUser> getAllUsers(CommunityId communityId);
	void addProjectUser(CommunityId communityId, ProjectId projectId, PersistentId userId, Role role);
	void removeUser(CommunityId communityId, ProjectId projectId, PersistentId userId);
}