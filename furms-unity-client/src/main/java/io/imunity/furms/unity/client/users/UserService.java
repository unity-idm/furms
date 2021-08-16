/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
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
import pl.edu.icm.unity.types.basic.MultiGroupMembers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.imunity.furms.unity.common.UnityConst.ALL_GROUPS_PATTERNS;
import static io.imunity.furms.unity.common.UnityConst.COMMUNITY_ID;
import static io.imunity.furms.unity.common.UnityConst.ENUMERATION;
import static io.imunity.furms.unity.common.UnityConst.FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE;
import static io.imunity.furms.unity.common.UnityConst.GROUPS_PATTERNS;
import static io.imunity.furms.unity.common.UnityConst.GROUP_PATH;
import static io.imunity.furms.unity.common.UnityConst.ID;
import static io.imunity.furms.unity.common.UnityConst.IDENTIFIER_IDENTITY;
import static io.imunity.furms.unity.common.UnityConst.IDENTITY_TYPE;
import static io.imunity.furms.unity.common.UnityConst.PERSISTENT_IDENTITY;
import static io.imunity.furms.unity.common.UnityConst.PROJECT_ID;
import static io.imunity.furms.unity.common.UnityConst.PROJECT_PATTERN;
import static io.imunity.furms.unity.common.UnityConst.ROOT_GROUP;
import static io.imunity.furms.unity.common.UnityConst.ROOT_GROUP_PATH;
import static io.imunity.furms.unity.common.UnityConst.STRING;
import static io.imunity.furms.unity.common.UnityPaths.ATTRIBUTE_PATTERN;
import static io.imunity.furms.unity.common.UnityPaths.ENTITY_ATTRIBUTES;
import static io.imunity.furms.unity.common.UnityPaths.ENTITY_BASE;
import static io.imunity.furms.unity.common.UnityPaths.GROUP;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_ATTRIBUTES;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_BASE;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_MEMBERS;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_MEMBERS_MULTI;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
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
		String uriComponents = prepareAttributeRequestPath(userId);
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

	public void addUserPolicyAcceptance(FenixUserId userId, PolicyAcceptance policyAcceptance) {
		String uriComponents = prepareAttributeRequestPath(userId);
		Set<PolicyAcceptance> policyAcceptances = getPolicyAcceptances(userId);
		Set<PolicyAcceptance> oldRevisionPolicyAcceptance = policyAcceptances.stream()
			.filter(x -> x.policyDocumentId.equals(policyAcceptance.policyDocumentId))
			.collect(toSet());
		policyAcceptances.removeAll(oldRevisionPolicyAcceptance);
		policyAcceptances.add(policyAcceptance);

		Attribute attribute = new Attribute(
				FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE,
			STRING,
			"/",
			policyAcceptances.stream()
				.map(PolicyAcceptanceArgument::valueOf)
				.map(PolicyAcceptanceParser::parse)
				.collect(Collectors.toUnmodifiableList())
		);
		unityClient.put(uriComponents, attribute, Map.of(IDENTITY_TYPE, IDENTIFIER_IDENTITY));
	}

	private String prepareAttributeRequestPath(PersistentId userId) {
		return UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.pathSegment("{" + ID + "}")
			.path(ATTRIBUTE_PATTERN)
			.buildAndExpand(Map.of(ID, userId.id))
			.toUriString();
	}

	private String prepareAttributeRequestPath(FenixUserId userId) {
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
		String uriComponents = prepareAttributeRequestPath(userId);
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

	public Set<PolicyAcceptance> getPolicyAcceptances(FenixUserId userId) {
		return getPolicyAcceptances(getAttributesFromRootGroup(userId));
	}

	public Set<PolicyAcceptance> getPolicyAcceptances(Collection<? extends Attribute> attributes) {
		return attributes
			.stream()
			.filter(attribute -> attribute.getName().equals(FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE))
			.flatMap(attribute -> attribute.getValues().stream())
			.map(PolicyAcceptanceParser::parse)
			.map(PolicyAcceptanceArgument::toPolicyAcceptance)
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

	public Set<UserPolicyAcceptances> getAllUsersPolicyAcceptanceFromGroups(String group, Map<String, Set<String>> ids){
		Map<String, String> uriVariables = Map.of(ROOT_GROUP_PATH, group);
		String path = UriComponentsBuilder.newInstance()
			.path(GROUP_MEMBERS_MULTI)
			.pathSegment("{" + ROOT_GROUP_PATH + "}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();

		List<String> groups = ids.entrySet().stream()
			.map(entry -> getProjectPaths(entry.getKey(), entry.getValue()))
			.flatMap(Collection::stream)
			.collect(toList());

		MultiGroupMembers multiGroupMembers = unityClient.post(path, groups, Map.of(), new ParameterizedTypeReference<MultiGroupMembers>() {});
		Map<Long, List<Identity>> collect = multiGroupMembers.entities.stream().collect(toMap(x -> x.getEntityInformation().getId(), Entity::getIdentities));

		return multiGroupMembers.members.values().stream()
			.flatMap(Collection::stream)
			.map(x -> UnityUserMapper.map(collect.getOrDefault(x.entityId, Collections.emptyList()), x.attributes)
				.map(y -> new UserPolicyAcceptances(y, getPolicyAcceptances(x.attributes)))
			)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(toSet());
	}

	private List<String> getProjectPaths(String communityId, Set<String> projectIds) {
		return projectIds.stream()
			.map(projectId ->
				UriComponentsBuilder.newInstance()
				.path(PROJECT_PATTERN)
				.uriVariables(Map.of(COMMUNITY_ID, communityId, PROJECT_ID, projectId))
				.toUriString()
			).collect(Collectors.toList());
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
