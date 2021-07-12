/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.users.UnityUserMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.AttributeExt;
import pl.edu.icm.unity.types.basic.Entity;
import pl.edu.icm.unity.types.basic.GroupMember;
import pl.edu.icm.unity.types.basic.Identity;

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

	public void addUserToGroup(PersistentId userId, String group){
		String path = prepareGroupRequestPath(userId, group);
		unityClient.post(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
	}

	private String prepareGroupRequestPath(PersistentId userId, String group) {
		Map<String, String> uriVariables = Map.of(GROUP_PATH, group, ID, userId.id);
		return UriComponentsBuilder.newInstance()
			.path(GROUP_BASE)
			.pathSegment("{" + GROUP_PATH + "}")
			.path(ENTITY_BASE)
			.pathSegment("{" + ID + "}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();
	}

	public void addUserRole(PersistentId userId, String group, Role role){
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

	private String prepareRoleRequestPath(PersistentId userId) {
		return UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.pathSegment("{" + ID + "}")
			.path(ATTRIBUTE_PATTERN)
			.buildAndExpand(Map.of(ID, userId.id))
			.toUriString();
	}

	public void removeUserFromGroup(PersistentId userId, String group){
		String path = prepareGroupRequestPath(userId, group);
		unityClient.delete(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
	}

	public void removeUserRole(PersistentId userId, String group, Role role){
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

	public Set<String> getRoleValues(PersistentId userId, String group, Role role) {
		return getAttributesFromGroup(userId, group)
			.stream()
			.filter(attribute -> attribute.getName().equals(role.unityRoleAttribute))
			.flatMap(attribute -> attribute.getValues().stream())
			.collect(toSet());
	}

	public Optional<FURMSUser> getUser(PersistentId userId){
		List<Attribute> attributesFromGroup = getAttributesFromRootGroup(userId);
		Entity entity  = getEntity(userId);
		
		return UnityUserMapper.map(userId, entity.getIdentities(), attributesFromGroup);
	}

	public Optional<FURMSUser> getUser(FenixUserId userId){
		List<Attribute> attributesFromGroup = getAttributesFromRootGroup(userId);
		Entity entity  = getEntity(userId);

		return UnityUserMapper.map(userId, entity.getIdentities(), attributesFromGroup);
	}

	private List<Attribute> getAttributesFromGroup(PersistentId userId, String group) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(GROUP_ATTRIBUTES)
			.uriVariables(Map.of(ID, userId.id))
			.build()
			.toUriString();
		Map<String, List<Attribute>> groupedAttributes =
			unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(GROUPS_PATTERNS, ALL_GROUPS_PATTERNS));
		return groupedAttributes.getOrDefault(group, Collections.emptyList());
	}

	private List<Attribute> getAttributesFromRootGroup(PersistentId userId) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(ENTITY_ATTRIBUTES)
			.uriVariables(Map.of(ID, userId.id))
			.build()
			.toUriString();
		return unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(GROUP, ROOT_GROUP));
	}

	private List<Attribute> getAttributesFromRootGroup(FenixUserId userId) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(ENTITY_ATTRIBUTES)
			.uriVariables(Map.of(ID, userId.id))
			.build()
			.toUriString();
		return unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(GROUP, ROOT_GROUP, IDENTITY_TYPE, IDENTIFIER_IDENTITY));
	}

	private Entity getEntity(PersistentId userId) {
		String path = UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.path(userId.id)
			.build()
			.toUriString();
		return unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
	}
	
	private Entity getEntity(FenixUserId userId) {
		String path = UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.path(userId.id)
			.build()
			.toUriString();
		return unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(IDENTITY_TYPE, IDENTIFIER_IDENTITY));
	}
	
	public List<FURMSUser> getAllUsersByRole(String group, Role role) {
		Predicate<AttributeExt> filter = attribute ->
			attribute.getName().equals(role.unityRoleAttribute) &&
				attribute.getValues().contains(role.unityRoleValue);
		return getAllUsersFromGroup(group, filter);
	}

	public List<FURMSUser> getAllUsersFromGroup(String group, Predicate<AttributeExt> filter){
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

	public PersistentId getPersistentId(FenixUserId userId) {
		return getEntity(userId).getIdentities().stream()
				.filter(identity -> identity.getTypeId().equals(PERSISTENT_IDENTITY)).findAny()
				.map(Identity::getComparableValue).map(id -> new PersistentId(id)).orElse(null);

	}
	
	public FenixUserId getFenixUserId(PersistentId userId) {
		return getEntity(userId).getIdentities().stream()
				.filter(identity -> identity.getTypeId().equals(IDENTIFIER_IDENTITY)).findAny()
				.map(Identity::getComparableValue).map(id -> new FenixUserId(id)).orElse(null);

	}
}
