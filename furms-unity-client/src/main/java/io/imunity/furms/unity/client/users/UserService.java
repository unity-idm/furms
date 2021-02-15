/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.users.UnityUserMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.AttributeExt;
import pl.edu.icm.unity.types.basic.GroupMember;

import java.util.*;
import java.util.function.Predicate;

import static io.imunity.furms.unity.common.UnityConst.*;
import static io.imunity.furms.unity.common.UnityPaths.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class UserService {
	private final UnityClient unityClient;

	public UserService(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	public void addUserToGroup(String userId, String group){
		String path = prepareGroupRequestPath(userId, group);
		unityClient.post(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
	}

	private String prepareGroupRequestPath(String userId, String group) {
		Map<String, String> uriVariables = Map.of(GROUP_PATH, group, ID, userId);
		return UriComponentsBuilder.newInstance()
			.path(GROUP_BASE)
			.pathSegment("{" + GROUP_PATH + "}")
			.path(ENTITY_BASE)
			.pathSegment("{" + ID + "}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();
	}

	public void addUserRole(String userId, String group, Role role){
		String uriComponents = prepareRoleRequestPath(userId);
		Set<String> roleValues = getRoleValues(userId, group, role);
		roleValues.add(role.unityRoleValue);

		Attribute attribute = new Attribute(
			role.unityRoleAttribute,
			ENUMERATION,
			group,
			new ArrayList<>(roleValues)
		);
		unityClient.put(uriComponents, attribute);
	}

	private String prepareRoleRequestPath(String userId) {
		return UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.pathSegment("{" + ID + "}")
			.path(ATTRIBUTE_PATTERN)
			.buildAndExpand(Map.of(ID, userId))
			.toUriString();
	}

	public void removeUserFromGroup(String userId, String group){
		String path = prepareGroupRequestPath(userId, group);
		unityClient.delete(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
	}

	public void removeUserRole(String userId, String group, Role role){
		String uriComponents = prepareRoleRequestPath(userId);
		Set<String> roleValues = getRoleValues(userId, group, role);
		roleValues.remove(role.unityRoleValue);

		Attribute attribute = new Attribute(
			role.unityRoleAttribute,
			ENUMERATION,
			group,
			new ArrayList<>(roleValues)
		);
		unityClient.put(uriComponents, attribute);
	}

	public Set<String> getRoleValues(String userId, String group, Role role) {
		return getAttributesFromGroup(userId, group)
			.stream()
			.filter(attribute -> attribute.getName().equals(role.unityRoleAttribute))
			.flatMap(attribute -> attribute.getValues().stream())
			.collect(toSet());
	}

	public boolean hasRole(String userId, String group, Role role) {
		return getAttributesFromGroup(userId, group)
			.stream()
			.filter(attribute -> attribute.getName().equals(role.unityRoleAttribute))
			.flatMap(attribute -> attribute.getValues().stream())
			.anyMatch(attribute -> attribute.equals(role.unityRoleValue));
	}

	private List<Attribute> getAttributesFromGroup(String userId, String group) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(GROUP_ATTRIBUTES)
			.uriVariables(Map.of(ID, userId))
			.build()
			.toUriString();
		Map<String, List<Attribute>> groupedAttributes =
			unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(GROUPS_PATTERNS, ALL_GROUPS_PATTERNS));
		return groupedAttributes.getOrDefault(group, Collections.emptyList());
	}

	public List<User> getAllUsersByRole(String group, Role role) {
		Predicate<AttributeExt> filter = attribute ->
			attribute.getName().equals(role.unityRoleAttribute) &&
				attribute.getValues().contains(role.unityRoleValue);
		return getAllUsersFromGroup(group, filter);
	}

	public List<User> getAllUsersFromGroup(String group, Predicate<AttributeExt> filter){
		Map<String, String> uriVariables = Map.of(ROOT_GROUP_PATH, group);
		String path = UriComponentsBuilder.newInstance()
			.path(GROUP_MEMBERS)
			.pathSegment("{" + ROOT_GROUP_PATH + "}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();

		return unityClient.get(path, new ParameterizedTypeReference<List<GroupMember>>() {}).stream()
			.filter(groupMember -> groupMember.getAttributes().stream().anyMatch(filter))
			.map(UnityUserMapper::map)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(toList());
	}
}
