/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.projects;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.CommunityAdminsAndProjectAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.GroupedUsers;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.client.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Role.COMMUNITY_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.PROJECT_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.PROJECT_USER;
import static io.imunity.furms.unity.common.UnityConst.COMMUNITY_GROUP_PATTERN;
import static io.imunity.furms.unity.common.UnityConst.COMMUNITY_ID;
import static io.imunity.furms.unity.common.UnityConst.COMMUNITY_PATTERN;
import static io.imunity.furms.unity.common.UnityConst.ID;
import static io.imunity.furms.unity.common.UnityConst.PROJECT_GROUP_PATTERN;
import static io.imunity.furms.unity.common.UnityConst.PROJECT_ID;
import static io.imunity.furms.unity.common.UnityConst.PROJECT_PATTERN;
import static io.imunity.furms.unity.common.UnityConst.RECURSIVE;
import static io.imunity.furms.unity.common.UnityConst.WITH_PARENTS;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_BASE;
import static io.imunity.furms.unity.common.UnityPaths.META;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static java.lang.Boolean.TRUE;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
class UnityProjectGroupsDAO implements ProjectGroupsDAO {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UnityClient unityClient;
	private final UserService userService;

	public UnityProjectGroupsDAO(UnityClient unityClient, UserService userService) {
		this.unityClient = unityClient;
		this.userService = userService;
	}

	@Override
	public Optional<ProjectGroup> get(CommunityId communityId, ProjectId projectId) {
		if (isEmpty(communityId) || isEmpty(projectId)) {
			throw new IllegalArgumentException("Could not get Project from Unity. Missing Community or Project ID");
		}
		Map<String, Object> uriVariables = getUriVariables(communityId, projectId);
		String path = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(PROJECT_PATTERN)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();

		Group group = unityClient.get(path, Group.class);
		return Optional.ofNullable(ProjectGroup.builder()
			.id(projectId)
			.name(group.getDisplayedName().getDefaultValue())
			.communityId(communityId)
			.build());
	}

	@Override
	public void create(ProjectGroup projectGroup) {
		if (projectGroup == null || isEmpty(projectGroup.getId()) || isEmpty(projectGroup.getCommunityId())) {
			throw new IllegalArgumentException("Could not create Project in Unity. Missing Community or Project ID");
		}
		Map<String, Object> uriVariables = getUriVariables(projectGroup.getCommunityId(), projectGroup.getId());
		String groupPath = UriComponentsBuilder.newInstance()
			.path(GROUP_BASE)
			.pathSegment(PROJECT_PATTERN)
			.uriVariables(uriVariables)
			.buildAndExpand().encode().toUriString();

		unityClient.post(groupPath, null, Map.of(WITH_PARENTS, TRUE.toString()));
		updateGroupName(projectGroup);
		LOG.debug("Project group {} under Community group {} was crated in Unity", projectGroup.getId(), projectGroup.getCommunityId());
	}

	@Override
	public void update(ProjectGroup projectGroup) {
		if (projectGroup == null || isEmpty(projectGroup.getId()) || isEmpty(projectGroup.getCommunityId())) {
			throw new IllegalArgumentException("Could not update Project in Unity. Missing Community or Project ID");
		}
		updateGroupName(projectGroup);
		LOG.debug("Project group {} name was updated to: {}", projectGroup.getId(), projectGroup.getName());

	}

	private void updateGroupName(ProjectGroup projectGroup) {
		Map<String, Object> uriVariables = getUriVariables(projectGroup.getCommunityId(), projectGroup.getId());
		String metaCommunityPath = UriComponentsBuilder.newInstance()
				.path(PROJECT_GROUP_PATTERN)
				.uriVariables(uriVariables)
				.buildAndExpand().toUriString();
		Group group = new Group(metaCommunityPath);
		group.setDisplayedName(new I18nString(projectGroup.getName()));
		unityClient.put(GROUP_BASE, group);
	}

	@Override
	public void delete(CommunityId communityId, ProjectId projectId) {
		if (isEmpty(communityId) || isEmpty(projectId)) {
			throw new IllegalArgumentException("Missing Community or Project ID");
		}
		Map<String, Object> uriVariables = getUriVariables(communityId, projectId);
		Map<String, String> queryParams = Map.of(RECURSIVE, TRUE.toString());
		String deleteCommunityPath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(PROJECT_GROUP_PATTERN)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		unityClient.delete(deleteCommunityPath, queryParams);
		LOG.debug("Project group {} under Community group {} was deleted", projectId, communityId);
	}

	private Map<String, Object> getUriVariables(CommunityId communityId, ProjectId projectId) {
		return Map.of(COMMUNITY_ID, communityId.id.toString(), PROJECT_ID, projectId.id.toString());
	}

	private Map<String, Object> getUriVariables(CommunityId communityId) {
		return Map.of(ID, communityId.id.toString());
	}

	@Override
	public List<FURMSUser> getAllAdmins(CommunityId communityId, ProjectId projectId) {
		assertTrue(!isEmpty(communityId),
			() -> new IllegalArgumentException("Could not get Project Admin from Unity. Missing Project or Community ID"));
		String communityPath = getPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		return userService.getAllUsersByRole(communityPath, PROJECT_ADMIN);
	}

	@Override
	public CommunityAdminsAndProjectAdmins getAllCommunityAndProjectAdmins(CommunityId communityId, ProjectId projectId) {
		assertTrue(!isEmpty(communityId),
			() -> new IllegalArgumentException("Could not get Project Users from Unity. Missing Project or Community ID"));
		String communityPath = getPath(getUriVariables(communityId), COMMUNITY_PATTERN);
		String projectPath = getPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		GroupedUsers groupedUsers = userService.getUsersFromGroupsFilteredByRoles(
			Map.of(
				communityPath,
				Set.of(COMMUNITY_ADMIN),
				projectPath,
				Set.of(PROJECT_ADMIN)
			)
		);
		return new CommunityAdminsAndProjectAdmins(groupedUsers.getUsers(communityPath), groupedUsers.getUsers(projectPath));
	}

	@Override
	public List<FURMSUser> getAllUsers(CommunityId communityId, ProjectId projectId) {
		assertTrue(!isEmpty(communityId),
			() -> new IllegalArgumentException("Could not get Project Users from Unity. Missing Project or Community ID"));
		String communityPath = getPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		return userService.getAllUsersByRole(communityPath, PROJECT_USER);
	}

	@Override
	public List<FURMSUser> getAllProjectAdminsAndUsers(CommunityId communityId, ProjectId projectId) {
		assertTrue(!isEmpty(communityId),
			() -> new IllegalArgumentException("Could not get Project Users from Unity. Missing Project or Community ID"));
		String projectPath = getPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		return userService.getAllUsersByRoles(projectPath, Set.of(PROJECT_ADMIN, PROJECT_USER));
	}

	@Override
	public List<FURMSUser> getAllUsers(CommunityId communityId) {
		assertTrue(!isEmpty(communityId),
			() -> new IllegalArgumentException("Could not get Project Users from Unity. Missing Project or Community ID"));
		String communityPath = getPath(getUriVariables(communityId), COMMUNITY_GROUP_PATTERN);
		return userService.getAllUsersFromGroup(communityPath, attr -> true);
	}

	@Override
	public void addProjectUser(CommunityId communityId, ProjectId projectId, PersistentId userId, Role role) {
		assertTrue(!isEmpty(communityId) && !isEmpty(userId),
			() -> new IllegalArgumentException("Could not add Project Admin in Unity. Missing Project ID or User ID or Community "));

		String projectPath = getPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		userService.addUserToGroup(userId, projectPath);
		userService.addUserRole(userId, projectPath, role);
	}

	@Override
	public void removeAdmin(CommunityId communityId, ProjectId projectId, PersistentId userId) {
		removeRole(PROJECT_ADMIN, communityId, projectId, userId);
	}

	@Override
	public void removeUser(CommunityId communityId, ProjectId projectId, PersistentId userId) {
		removeRole(PROJECT_USER, communityId, projectId, userId);
	}
	
	private void removeRole(Role role, CommunityId communityId, ProjectId projectId, PersistentId userId) {
		assertTrue(!isEmpty(communityId) && !isEmpty(userId),
				() -> new IllegalArgumentException("Could not remove " + role.name() 
					+ " in Unity. Missing Project ID or User ID or Community "));

		String projectPath = getPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		Set<String> roleValues = userService.getRoleValues(userId, projectPath, role);
		if (roleValues.contains(role.unityRoleValue)) {
			if(roleValues.size() == 1)
				userService.removeUserFromGroup(userId, projectPath);
			else
				userService.removeUserRole(userId, projectPath, role);
		}
	}

	private String getPath(Map<String, Object> uriVariables, String pattern) {
		return UriComponentsBuilder.newInstance()
			.path(pattern)
			.uriVariables(uriVariables)
			.toUriString();
	}
}
