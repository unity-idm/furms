/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.projects;

import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.CommunityAdminsAndProjectAdmins;
import io.imunity.furms.domain.projects.ProjectAdminControlledAttributes;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.GroupedUsers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProjectService {

	boolean existsById(String id);

	Set<Project> findAll(Set<String> ids);

	Optional<Project> findById(String id);

	Set<Project> findAllByCommunityId(String communityId);

	Set<Project> findAllNotExpiredByCommunityId(String communityId);

	Set<Project> findAll();

	Set<Project> findAllByCurrentUserId();

	boolean isProjectInTerminalState(String projectId);

	boolean isProjectInTerminalState(String communityId, String projectId);

	boolean isProjectExpired(String projectId);

	String create(Project project);

	void update(Project project);

	void update(ProjectAdminControlledAttributes project);

	void delete(String projectId, String communityId);

	List<FURMSUser> findAllAdmins(String communityId, String projectId);

	CommunityAdminsAndProjectAdmins findAllCommunityAndProjectAdmins(String communityId, String projectId);

	boolean isAdmin(String projectId);

	boolean hasAdminRights(String projectId);

	void addAdmin(String communityId, String projectId, PersistentId userId);

	Set<Invitation> findAllAdminsInvitations(String projectId);

	Set<Invitation> findAllUsersInvitations(String projectId);

	void inviteAdmin(String projectId, PersistentId userId);

	void inviteAdmin(String projectId, String email);

	void removeInvitation(String projectId, InvitationId id);

	void resendInvitation(String projectId, InvitationId id);

	void removeAdmin(String communityId, String projectId, PersistentId userId);

	List<FURMSUser> findAllUsers(String communityId, String projectId);

	List<FURMSUser> findAllProjectAdminsAndUsers(String communityId, String projectId);

	List<FURMSUser> findAllUsers(String projectId);

	Optional<FURMSUser> findProjectLeaderInfoAsInstalledUser(String projectId);

	boolean isUser(String projectId);

	Set<String> getUsersProjectIds();

	void addUser(String communityId, String projectId, PersistentId userId);

	void inviteUser(String projectId, PersistentId userId);

	void inviteUser(String projectId, String email);

	void removeUser(String communityId, String projectId, PersistentId userId);

	void resignFromMembership(String communityId, String projectId);
}
