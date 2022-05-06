/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.communities;

import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.users.AllUsersAndCommunityAdmins;
import io.imunity.furms.domain.users.CommunityUsersAndAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.GroupedUsers;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.client.users.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Role.COMMUNITY_ADMIN;
import static io.imunity.furms.unity.common.UnityConst.COMMUNITY_GROUP_PATTERN;
import static io.imunity.furms.unity.common.UnityConst.COMMUNITY_PATTERN;
import static io.imunity.furms.unity.common.UnityConst.FENIX_GROUP;
import static io.imunity.furms.unity.common.UnityConst.ID;
import static io.imunity.furms.unity.common.UnityConst.RECURSIVE;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_BASE;
import static io.imunity.furms.unity.common.UnityPaths.META;
import static io.imunity.furms.unity.common.UnityPaths.USERS_PATTERN;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static java.lang.Boolean.TRUE;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
class UnityCommunityGroupsDAO implements CommunityGroupsDAO {

	private final UnityClient unityClient;
	private final UserService userService;

	public UnityCommunityGroupsDAO(UnityClient unityClient, UserService userService) {
		this.unityClient = unityClient;
		this.userService = userService;
	}

	@Override
	public Optional<CommunityGroup> get(String id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Could not get Community from Unity. Missing Community ID");
		}
		Map<String, Object> uriVariables = uriVariables(id);
		String path = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(COMMUNITY_GROUP_PATTERN)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();

		Group group = unityClient.get(path, Group.class);
		return Optional.ofNullable(CommunityGroup.builder()
			.id(id)
			.name(group.getDisplayedName().getDefaultValue())
			.build());
	}

	@Override
	public void create(CommunityGroup community) {
		if (community == null || isEmpty(community.getId())) {
			throw new IllegalArgumentException("Could not create Community in Unity. Missing Community or Community ID");
		}
		Map<String, Object> uriVariables = uriVariables(community);
		String groupPath = getCommunityPath(uriVariables, COMMUNITY_GROUP_PATTERN);
		Group group = new Group(groupPath);
		group.setDisplayedName(new I18nString(community.getName()));
		unityClient.post(GROUP_BASE, group);
		String createCommunityUsersPath = UriComponentsBuilder.newInstance()
			.path(GROUP_BASE)
			.pathSegment(groupPath + USERS_PATTERN)
			.toUriString();unityClient.post(createCommunityUsersPath);
	}

	@Override
	public void update(CommunityGroup community) {
		if (community == null || isEmpty(community.getId())) {
			throw new IllegalArgumentException("Could not update Community in Unity. Missing Community or Community ID");
		}
		Map<String, Object> uriVariables = uriVariables(community);
		String metaCommunityPath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(COMMUNITY_GROUP_PATTERN)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		Group group = unityClient.get(metaCommunityPath, Group.class);
		group.setDisplayedName(new I18nString(community.getName()));
		unityClient.put(GROUP_BASE, group);
	}

	@Override
	public void delete(String id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Missing Community ID");
		}
		Map<String, Object> uriVariables = uriVariables(id);
		Map<String, String> queryParams = Map.of(RECURSIVE, TRUE.toString());
		String deleteCommunityPath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(COMMUNITY_GROUP_PATTERN)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		unityClient.delete(deleteCommunityPath, queryParams);
	}

	@Override
	public List<FURMSUser> getAllAdmins(String communityId) {
		assertTrue(!isEmpty(communityId),
			() -> new IllegalArgumentException("Could not get Community Admin from Unity. Missing Community ID"));
		String communityPath = getCommunityPath(Map.of(ID, communityId), COMMUNITY_PATTERN);
		return userService.getAllUsersByRole(communityPath, COMMUNITY_ADMIN);
	}

	@Override
	public AllUsersAndCommunityAdmins getAllUsersAndCommunityAdmins(String communityId) {
		assertTrue(!isEmpty(communityId),
			() -> new IllegalArgumentException("Could not get Community Admin from Unity. Missing Community ID"));
		String communityPath = getCommunityPath(Map.of(ID, communityId), COMMUNITY_PATTERN);
		GroupedUsers groupedUsers = userService.getUsersFromGroupsFilteredByRoles(Map.of(
				FENIX_GROUP,
				Set.of(),
				communityPath,
				Set.of(COMMUNITY_ADMIN)
			));
		return new AllUsersAndCommunityAdmins(groupedUsers.getUsers(FENIX_GROUP), groupedUsers.getUsers(communityPath));
	}

	@Override
	public List<FURMSUser> getAllUsers(String communityId) {
		assertTrue(!isEmpty(communityId),
			() -> new IllegalArgumentException("Could not get Community Admin from Unity. Missing Community ID"));
		String communityPath = getCommunityPath(Map.of(ID, communityId), COMMUNITY_GROUP_PATTERN);
		return userService.getAllUsersFromGroup(communityPath, attributeExt -> true);
	}

	@Override
	public CommunityUsersAndAdmins getCommunityAdminsAndUsers(String communityId) {
		assertTrue(!isEmpty(communityId),
			() -> new IllegalArgumentException("Could not get Community Admin from Unity. Missing Community ID"));
		String communityAdminPath = getCommunityPath(Map.of(ID, communityId), COMMUNITY_PATTERN);
		String communityUserPath = getCommunityPath(Map.of(ID, communityId), COMMUNITY_GROUP_PATTERN);
		GroupedUsers groupedUsers = userService.getUsersFromGroupsFilteredByRoles(
			Map.of(
				communityAdminPath,
				Set.of(COMMUNITY_ADMIN),
				communityUserPath,
				Set.of()
			)
		);

		return new CommunityUsersAndAdmins(groupedUsers.getUsers(communityUserPath),
			groupedUsers.getUsers(communityAdminPath)
		);
	}

	@Override
	public void addAdmin(String communityId, PersistentId userId) {
		assertTrue(!isEmpty(communityId) && !isEmpty(userId),
			() -> new IllegalArgumentException("Could not add Community Admin in Unity. Missing Community ID or User ID"));

		String communityPath = getCommunityPath(Map.of(ID, communityId), COMMUNITY_PATTERN);
		userService.addUserToGroup(userId, communityPath);
		userService.addUserRole(userId, communityPath, COMMUNITY_ADMIN);
	}

	@Override
	public void removeAdmin(String communityId, PersistentId userId) {
		assertTrue(!isEmpty(communityId) && !isEmpty(userId),
			() -> new IllegalArgumentException("Could not remove Community Admin in Unity. Missing Community ID or User ID"));

		String communityPath = getCommunityPath(Map.of(ID, communityId), COMMUNITY_PATTERN);
		if(userService.getRoleValues(userId, communityPath, COMMUNITY_ADMIN).size() > 1)
			userService.removeUserRole(userId, communityPath, COMMUNITY_ADMIN);
		else
			userService.removeUserFromGroup(userId, communityPath);
	}

	private Map<String, Object> uriVariables(CommunityGroup community) {
		return uriVariables(community.getId());
	}

	private Map<String, Object> uriVariables(String id) {
		return Map.of(ID, id);
	}

	private String getCommunityPath(Map<String, Object> uriVariables, String pattern) {
		return UriComponentsBuilder.newInstance()
			.path(pattern)
			.uriVariables(uriVariables)
			.toUriString();
	}
}
