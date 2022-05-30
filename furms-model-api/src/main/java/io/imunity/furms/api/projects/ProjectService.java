/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.projects;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectAdminControlledAttributes;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.CommunityAdminsAndProjectAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProjectService {

	boolean existsById(ProjectId id);

	Set<Project> findAll(Set<ProjectId> ids);

	Optional<Project> findById(ProjectId id);

	Set<Project> findAllByCommunityId(CommunityId communityId);

	Set<Project> findAllNotExpiredByCommunityId(CommunityId communityId);

	Set<Project> findAll();

	Set<Project> findAllByCurrentUserId();

	boolean isProjectInTerminalState(ProjectId projectId);

	boolean isProjectInTerminalState(CommunityId communityId, ProjectId projectId);

	boolean isProjectExpired(ProjectId projectId);

	ProjectId create(Project project);

	void update(Project project);

	void update(ProjectAdminControlledAttributes project);

	void delete(ProjectId projectId, CommunityId communityId);

	List<FURMSUser> findAllAdmins(CommunityId communityId, ProjectId projectId);

	CommunityAdminsAndProjectAdmins findAllCommunityAndProjectAdmins(CommunityId communityId, ProjectId projectId);

	boolean isAdmin(ProjectId projectId);

	boolean hasAdminRights(ProjectId projectId);

	void addAdmin(CommunityId communityId, ProjectId projectId, PersistentId userId);

	Set<Invitation> findAllAdminsInvitations(ProjectId projectId);

	Set<Invitation> findAllUsersInvitations(ProjectId projectId);

	void inviteAdmin(ProjectId projectId, PersistentId userId);

	void inviteAdmin(ProjectId projectId, String email);

	void removeInvitation(ProjectId projectId, InvitationId id);

	void resendInvitation(ProjectId projectId, InvitationId id);

	void removeAdmin(CommunityId communityId, ProjectId projectId, PersistentId userId);

	List<FURMSUser> findAllUsers(CommunityId communityId, ProjectId projectId);

	List<FURMSUser> findAllProjectAdminsAndUsers(CommunityId communityId, ProjectId projectId);

	List<FURMSUser> findAllUsers(ProjectId projectId);

	Optional<FURMSUser> findProjectLeaderInfoAsInstalledUser(ProjectId projectId);

	boolean isUser(ProjectId projectId);

	Set<ProjectId> getUsersProjectIds();

	void addUser(CommunityId communityId, ProjectId projectId, PersistentId userId);

	void inviteUser(ProjectId projectId, PersistentId userId);

	void inviteUser(ProjectId projectId, String email);

	void removeUser(CommunityId communityId, ProjectId projectId, PersistentId userId);

	void resignFromMembership(CommunityId communityId, ProjectId projectId);
}
