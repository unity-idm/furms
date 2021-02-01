/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.unity.client.unity.UnityClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.AttributeExt;
import pl.edu.icm.unity.types.basic.GroupMember;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static io.imunity.furms.domain.authz.roles.Role.*;
import static io.imunity.furms.unity.client.common.UnityConst.*;
import static io.imunity.furms.unity.client.common.UnityPaths.*;
import static java.util.stream.Collectors.toList;

@Component
class UnityUsersDAO implements UsersDAO {

	private final UnityClient unityClient;

	UnityUsersDAO(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public List<User> getAdminUsers() {
		return getUsers(FENIX_USERS_GROUP);
	}

	@Override
	public List<User> getProjectUsers(String communityId, String projectId) {
		String path = prepareGroupPath(communityId, projectId);
		return getUsers(path,
			attribute ->
				attribute.getName().equals(PROJECT_MEMBER.unityRoleAttribute) &&
				attribute.getValues().contains(PROJECT_MEMBER.unityRoleValue)
		);
	}

	@Override
	public List<User> getAllUsers() {
		return getUsers(FENIX_GROUP);
	}

	private List<User> getUsers(String usersGroupPath) {
		return getUsers(usersGroupPath, attribute -> true);
	}

	private List<User> getUsers(String usersGroupPath, Predicate<AttributeExt> predicate) {
		Map<String, String> uriVariables = Map.of(ROOT_GROUP_PATH, usersGroupPath);
		String path = UriComponentsBuilder.newInstance()
			.path(GROUP_MEMBERS)
			.pathSegment("{" + ROOT_GROUP_PATH + "}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();

		return unityClient.get(path, new ParameterizedTypeReference<List<GroupMember>>() {}).stream()
			.filter(x -> x.getAttributes().stream().anyMatch(predicate))
			.map(UnityUserMapper::map)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(toList());
	}

	@Override
	public boolean isProjectMember(String communityId, String projectId, String userId) {
		List<Attribute> attributes = getAttributes(communityId, projectId, userId);
		return attributes.stream()
			.filter(x -> x.getName().equals(PROJECT_MEMBER.unityRoleAttribute))
			.flatMap(x -> x.getValues().stream())
			.anyMatch(x -> x.equals(PROJECT_MEMBER.unityRoleValue));
	}

	private List<Attribute> getAttributes(String communityId, String projectId, String userId) {
		String groupPath = prepareGroupPath(communityId, projectId);
		String path = UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.pathSegment("{" + ID + "}")
			.path(ATTRIBUTES_PATTERN)
			.buildAndExpand(Map.of(ID, userId))
			.toUriString();
		return unityClient.get(
			path,
			new ParameterizedTypeReference<>() {},
			Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY, GROUP, groupPath)
		);
	}

	private String prepareGroupPath(String communityId, String projectId) {
		return UriComponentsBuilder.newInstance()
			.path(PROJECTS_PATTERN)
			.buildAndExpand(Map.of(COMMUNITY_ID, communityId, PROJECT_ID, projectId))
			.toUriString();
	}

	@Override
	public void addProjectMemberRole(String communityId, String projectId, String userId) {
		String groupPath = prepareGroupPath(communityId, projectId);

		Role projectMember = PROJECT_MEMBER;
		List<String> attributes = getProjectRoleValues(communityId, projectId, userId, projectMember);
		attributes.add(projectMember.unityRoleValue);

		Attribute attribute = new Attribute(
			projectMember.unityRoleAttribute,
			ENUMERATION,
			groupPath,
			attributes
		);

		String path = prepareRoleRequestPath(userId);
		unityClient.put(path, attribute);
	}

	@Override
	public void addFenixAdminRole(String userId) {
		String path = prepareGroupRequestPath(userId);
		unityClient.post(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
		String uriComponents = prepareRoleRequestPath(userId);
		Role fenixAdmin = FENIX_ADMIN;
		Attribute attribute = new Attribute(
			fenixAdmin.unityRoleAttribute,
			ENUMERATION,
			FENIX_USERS_GROUP,
			List.of(fenixAdmin.unityRoleValue)
		);
		unityClient.put(uriComponents, attribute);
	}

	@Override
	public void removeFenixAdminRole(String userId) {
		String path = prepareGroupRequestPath(userId);
		unityClient.delete(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
	}

	@Override
	public void removeProjectMemberRole(String communityId, String projectId, String userId) {
		String groupPath = prepareGroupPath(communityId, projectId);
		String uriComponents = prepareRoleRequestPath(userId);

		Role projectMember = PROJECT_MEMBER;
		List<String> projectRoleValues = getProjectRoleValues(communityId, projectId, userId, projectMember);

		Attribute attribute = new Attribute(
			projectMember.unityRoleAttribute,
			ENUMERATION,
			groupPath,
			projectRoleValues
		);
		unityClient.put(uriComponents, attribute);
	}

	private List<String> getProjectRoleValues(String communityId, String projectId, String userId, Role projectMember) {
		return getAttributes(communityId, projectId, userId).stream()
			.filter(attribute -> attribute.getName().equals(projectMember.unityRoleAttribute))
			.flatMap(attribute -> attribute.getValues().stream())
			.filter(attribute -> !attribute.equals(projectMember.unityRoleValue))
			.collect(toList());
	}

	private String prepareRoleRequestPath(String userId) {
		return UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.pathSegment("{" + ID + "}")
			.path(ATTRIBUTE_PATTERN)
			.buildAndExpand(Map.of(ID, userId))
			.toUriString();
	}

	private String prepareGroupRequestPath(String userId) {
		Map<String, String> uriVariables = Map.of(GROUP_PATH, FENIX_USERS_GROUP, ID, userId);
		return UriComponentsBuilder.newInstance()
			.path(GROUP_BASE)
			.pathSegment("{" + GROUP_PATH + "}")
			.path(ENTITY_BASE)
			.pathSegment("{" + ID + "}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();
	}
}
