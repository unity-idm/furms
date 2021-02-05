/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import static io.imunity.furms.domain.authz.roles.Role.FENIX_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.PROJECT_MEMBER;
import static io.imunity.furms.unity.client.common.UnityConst.COMMUNITY_ID;
import static io.imunity.furms.unity.client.common.UnityConst.ENUMERATION;
import static io.imunity.furms.unity.client.common.UnityConst.FENIX_GROUP;
import static io.imunity.furms.unity.client.common.UnityConst.FENIX_PATTERN;
import static io.imunity.furms.unity.client.common.UnityConst.GROUP_PATH;
import static io.imunity.furms.unity.client.common.UnityConst.ID;
import static io.imunity.furms.unity.client.common.UnityConst.IDENTITY_TYPE;
import static io.imunity.furms.unity.client.common.UnityConst.PERSISTENT_IDENTITY;
import static io.imunity.furms.unity.client.common.UnityConst.PROJECT_ID;
import static io.imunity.furms.unity.client.common.UnityConst.PROJECT_PATTERN;
import static io.imunity.furms.unity.client.common.UnityConst.ROOT_GROUP_PATH;
import static io.imunity.furms.unity.client.common.UnityPaths.ATTRIBUTES_PATTERN;
import static io.imunity.furms.unity.client.common.UnityPaths.ATTRIBUTE_PATTERN;
import static io.imunity.furms.unity.client.common.UnityPaths.ENTITY_BASE;
import static io.imunity.furms.unity.client.common.UnityPaths.GROUP;
import static io.imunity.furms.unity.client.common.UnityPaths.GROUP_BASE;
import static io.imunity.furms.unity.client.common.UnityPaths.GROUP_MEMBERS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.logging.log4j.util.Strings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.unity.client.unity.UnityClient;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.AttributeExt;
import pl.edu.icm.unity.types.basic.EntityState;
import pl.edu.icm.unity.types.basic.GroupMember;

@Component
class UnityUsersDAO implements UsersDAO {

	private final UnityClient unityClient;

	UnityUsersDAO(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public List<User> getAdminUsers() {
		return getUsers(FENIX_PATTERN);
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

	@Override
	public Optional<User> findByEmail(String email) {
		if (Strings.isBlank(email)) {
			return Optional.empty();
		}
		return getAllUsers().stream()
				.filter(user -> user.email.equals(email))
				.findFirst();
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
			.filter(groupMember -> groupMember.getAttributes().stream().anyMatch(predicate))
			.map(UnityUserMapper::map)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(toList());
	}

	@Override
	public boolean isProjectMember(String communityId, String projectId, String userId) {
		List<Attribute> attributes = getAttributes(communityId, projectId, userId);
		return attributes.stream()
			.filter(attribute -> attribute.getName().equals(PROJECT_MEMBER.unityRoleAttribute))
			.flatMap(attribute -> attribute.getValues().stream())
			.anyMatch(attribute -> attribute.equals(PROJECT_MEMBER.unityRoleValue));
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

	@Override
	public void addProjectMemberRole(String communityId, String projectId, String userId) {
		String groupPath = prepareGroupPath(communityId, projectId);

		String addToGroupPath = prepareGroupRequestPath(userId, groupPath);
		unityClient.post(addToGroupPath, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));

		Role projectMember = PROJECT_MEMBER;
		Set<String> attributes = getProjectRoleValues(communityId, projectId, userId);
		attributes.add(projectMember.unityRoleValue);

		Attribute attribute = new Attribute(
			projectMember.unityRoleAttribute,
			ENUMERATION,
			groupPath,
			new ArrayList<>(attributes)
		);

		String addRolePath = prepareRoleRequestPath(userId);
		unityClient.put(addRolePath, attribute);
	}

	@Override
	public void addFenixAdminRole(String userId) {
		String path = prepareGroupRequestPath(userId, FENIX_PATTERN);
		unityClient.post(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
		String uriComponents = prepareRoleRequestPath(userId);
		Role fenixAdmin = FENIX_ADMIN;
		Attribute attribute = new Attribute(
			fenixAdmin.unityRoleAttribute,
			ENUMERATION,
			FENIX_PATTERN,
			List.of(fenixAdmin.unityRoleValue)
		);
		unityClient.put(uriComponents, attribute);
	}

	@Override
	public void addProjectAdminRole(String communityId, String projectId, String userId) {
		String groupPath = prepareGroupPath(communityId, projectId);
		String path = prepareGroupRequestPath(userId, groupPath);
		unityClient.post(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
		String uriComponents = prepareRoleRequestPath(userId);
		Role projectAdmin = Role.PROJECT_ADMIN;
		Attribute attribute = new Attribute(
			projectAdmin.unityRoleAttribute,
			ENUMERATION,
			groupPath,
			List.of(projectAdmin.unityRoleValue)
		);
		unityClient.put(uriComponents, attribute);
	}

	private String prepareGroupPath(String communityId, String projectId) {
		return UriComponentsBuilder.newInstance()
			.path(PROJECT_PATTERN)
			.buildAndExpand(Map.of(COMMUNITY_ID, communityId, PROJECT_ID, projectId))
			.toUriString();
	}

	@Override
	public void removeFenixAdminRole(String userId) {
		String path = prepareGroupRequestPath(userId, FENIX_PATTERN);
		unityClient.delete(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
	}

	@Override
	public void removeProjectMemberRole(String communityId, String projectId, String userId) {
		String uriComponents = prepareRoleRequestPath(userId);
		Role projectMember = PROJECT_MEMBER;
		Set<String> projectRoleValues = getProjectRoleValues(communityId, projectId, userId);
		projectRoleValues.remove(projectMember.unityRoleValue);

		if(projectRoleValues.isEmpty()){
			String groupPath = prepareGroupPath(communityId, projectId);
			String path = prepareGroupRequestPath(userId, groupPath);
			unityClient.delete(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
			return;
		}

		String groupPath = prepareGroupPath(communityId, projectId);
		Attribute attribute = new Attribute(
			projectMember.unityRoleAttribute,
			ENUMERATION,
			groupPath,
			new ArrayList<>(projectRoleValues)
		);
		unityClient.put(uriComponents, attribute);
	}

	private Set<String> getProjectRoleValues(String communityId, String projectId, String userId) {
		return getAttributes(communityId, projectId, userId).stream()
			.filter(attribute -> attribute.getName().equals(PROJECT_MEMBER.unityRoleAttribute))
			.flatMap(attribute -> attribute.getValues().stream())
			.collect(toSet());
	}

	private String prepareRoleRequestPath(String userId) {
		return UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.pathSegment("{" + ID + "}")
			.path(ATTRIBUTE_PATTERN)
			.buildAndExpand(Map.of(ID, userId))
			.toUriString();
	}

	private String prepareGroupRequestPath(String userId, String path) {
		Map<String, String> uriVariables = Map.of(GROUP_PATH, path, ID, userId);
		return UriComponentsBuilder.newInstance()
			.path(GROUP_BASE)
			.pathSegment("{" + GROUP_PATH + "}")
			.path(ENTITY_BASE)
			.pathSegment("{" + ID + "}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();
	}
	
	@Override
	public void setUserStatus(String fenixUserId, UserStatus status) {
		EntityState unityStatus = status == UserStatus.ENABLED ? EntityState.valid : EntityState.disabled;
		String uri = UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.path(fenixUserId)
			.path("/status/")
			.path(unityStatus.name())
			.toUriString();
		unityClient.put(uri);
	}

	@Override
	public UserStatus getUserStatus(String fenixUserId) {
		String uri = UriComponentsBuilder.newInstance()
				.path(ENTITY_BASE)
				.path(fenixUserId)
				.toUriString();
		ObjectNode response = unityClient.get(uri, ObjectNode.class);
		String statusStr = response.get("entityInformation").get("state").asText();
		EntityState unityState = EntityState.valueOf(statusStr);
		return unityState == EntityState.valid ? UserStatus.ENABLED : UserStatus.DISABLED;
	}
	
	// TODO: this method should directly query unity entity admin endpoint to get the user details
	// instead of retrieving all.
	@Override
	public Optional<User> findById(String userId) {
		if (Strings.isBlank(userId)) {
			return Optional.empty();
		}
		return getAllUsers().stream()
				.filter(user -> user.id.equals(userId))
				.findFirst();
	}
}
