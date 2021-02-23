/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.projects;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.users.User;
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

import static io.imunity.furms.domain.authz.roles.Role.PROJECT_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.PROJECT_USER;
import static io.imunity.furms.unity.common.UnityConst.*;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_BASE;
import static io.imunity.furms.unity.common.UnityPaths.META;
import static io.imunity.furms.utils.ValidationUtils.check;
import static java.lang.Boolean.TRUE;
import static org.springframework.util.StringUtils.isEmpty;

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
	public Optional<ProjectGroup> get(String communityId, String projectId) {
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
	public void delete(String communityId, String projectId) {
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

	private Map<String, Object> getUriVariables(String communityId, String projectId) {
		return Map.of(COMMUNITY_ID, communityId, PROJECT_ID, projectId);
	}

	private Map<String, Object> getUriVariables(String communityId) {
		return Map.of(ID, communityId);
	}

	@Override
	public List<User> getAllAdmins(String communityId, String projectId) {
		check(!isEmpty(communityId),
			() -> new IllegalArgumentException("Could not get Project Admin from Unity. Missing Project or Community ID"));
		String communityPath = getProjectPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		return userService.getAllUsersByRole(communityPath, PROJECT_ADMIN);
	}

	@Override
	public List<User> getAllUsers(String communityId, String projectId) {
		check(!isEmpty(communityId),
			() -> new IllegalArgumentException("Could not get Project Users from Unity. Missing Project or Community ID"));
		String communityPath = getProjectPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		return userService.getAllUsersByRole(communityPath, PROJECT_USER);
	}

	@Override
	public List<User> getAllUsers(String communityId) {
		check(!isEmpty(communityId),
			() -> new IllegalArgumentException("Could not get Project Users from Unity. Missing Project or Community ID"));
		String communityPath = getProjectPath(getUriVariables(communityId), COMMUNITY_GROUP_PATTERN);
		return userService.getAllUsersFromGroup(communityPath, attr -> true);
	}

	@Override
	public void addAdmin(String communityId, String projectId, String userId) {
		check(!isEmpty(communityId) && !isEmpty(userId),
			() -> new IllegalArgumentException("Could not add Project Admin in Unity. Missing Project ID or User ID or Community "));

		String projectPath = getProjectPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		userService.addUserToGroup(userId, projectPath);
		userService.addUserRole(userId, projectPath, PROJECT_ADMIN);
	}

	@Override
	public void addUser(String communityId, String projectId, String userId) {
		check(!isEmpty(communityId) && !isEmpty(userId),
			() -> new IllegalArgumentException("Could not add Project Admin in Unity. Missing Project ID or User ID or Community "));

		String projectPath = getProjectPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		userService.addUserToGroup(userId, projectPath);
		userService.addUserRole(userId, projectPath, PROJECT_USER);
	}

	@Override
	public void removeAdmin(String communityId, String projectId, String userId) {
		removeRole(PROJECT_ADMIN, communityId, projectId, userId);
	}

	@Override
	public void removeUser(String communityId, String projectId, String userId) {
		removeRole(PROJECT_USER, communityId, projectId, userId);
	}
	
	private void removeRole(Role role, String communityId, String projectId, String userId) {
		check(!isEmpty(communityId) && !isEmpty(userId),
				() -> new IllegalArgumentException("Could not remove " + role.name() 
					+ " in Unity. Missing Project ID or User ID or Community "));

		String projectPath = getProjectPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		Set<String> roleValues = userService.getRoleValues(userId, projectPath, role);
		if (roleValues.contains(role.unityRoleValue)) {
			if(roleValues.size() == 1)
				userService.removeUserFromGroup(userId, projectPath);
			else
				userService.removeUserRole(userId, projectPath, role);
		}
	}

	@Override
	public boolean isAdmin(String communityId, String projectId, String userId) {
		String projectPath = getProjectPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		return userService.hasRole(userId, projectPath, PROJECT_ADMIN);
	}

	@Override
	public boolean isUser(String communityId, String projectId, String userId) {
		String projectPath = getProjectPath(getUriVariables(communityId, projectId), PROJECT_PATTERN);
		return userService.hasRole(userId, projectPath, PROJECT_USER);
	}

	private String getProjectPath(Map<String, Object> uriVariables, String pattern) {
		return UriComponentsBuilder.newInstance()
			.path(pattern)
			.uriVariables(uriVariables)
			.toUriString();
	}
}
