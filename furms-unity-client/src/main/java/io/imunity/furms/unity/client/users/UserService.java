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
import io.imunity.furms.domain.users.GroupedUsers;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.users.UnityUserMapper;
import io.imunity.rest.api.RestGroupMemberWithAttributes;
import io.imunity.rest.api.RestMultiGroupMembersWithAttributes;
import io.imunity.rest.api.types.basic.RestAttribute;
import io.imunity.rest.api.types.basic.RestAttributeExt;
import io.imunity.rest.api.types.basic.RestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
	private final UnityClient unityClient;

	public UserService(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	public void addUserToGroup(PersistentId userId, String group){
		String path = prepareGroupRequestPath(userId, group);
		unityClient.post(path);
	}

	public void sendUserNotification(PersistentId userId, String templateId, Map<String, String> parameters){
		String path = prepareUserNotificationRequestPath(userId, templateId);

		Map<String, String> encodedParams = parameters.entrySet().stream()
			.collect(toMap(Map.Entry::getKey, x -> URLEncoder.encode(x.getValue(), StandardCharsets.UTF_8)));

		unityClient.post(path, null, encodedParams);
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

	private String prepareUserNotificationRequestPath(PersistentId userId, String templateId) {
		Map<String, String> uriVariables = Map.of("identityValue", userId.id, "templateId", templateId);
		return UriComponentsBuilder.newInstance()
			.path("/userNotification-trigger/entity/")
			.pathSegment("{" + "identityValue" + "}")
			.path("/template/")
			.pathSegment("{" + "templateId" + "}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();
	}

	public void addUserRole(PersistentId userId, String group, Role role){
		String uriComponents = prepareAttributeRequestPath(userId);
		Set<String> roleValues = getRoleValues(userId, group, role);
		roleValues.add(role.unityRoleValue);

		RestAttribute attribute = RestAttribute.builder()
			.withName(role.unityRoleAttribute)
			.withValueSyntax(ENUMERATION)
			.withGroupPath(group)
			.withValues(new ArrayList<>(roleValues))
			.build();
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

		RestAttribute attribute = RestAttribute.builder()
			.withName(FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE)
			.withValueSyntax(STRING)
			.withGroupPath("/")
			.withValues(policyAcceptances.stream()
				.map(PolicyAcceptanceArgument::valueOf)
				.map(PolicyAcceptanceParser::parse)
				.toList())
			.build();
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

		RestAttribute attribute = RestAttribute.builder()
			.withName(role.unityRoleAttribute)
			.withValueSyntax(ENUMERATION)
			.withGroupPath(group)
			.withValues(new ArrayList<>(roleValues))
			.build();
		unityClient.put(uriComponents, attribute);
	}

	public Set<String> getRoleValues(PersistentId userId, String group, Role role) {
		return getAttributesFromGroup(userId, group)
			.stream()
			.filter(attribute -> attribute.name.equals(role.unityRoleAttribute))
			.flatMap(attribute -> attribute.values.stream())
			.collect(toSet());
	}

	public Set<PolicyAcceptance> getPolicyAcceptances(FenixUserId userId) {
		return getPolicyAcceptances(getAttributesFromRootGroup(userId));
	}

	public Set<PolicyAcceptance> getPolicyAcceptances(Collection<? extends RestAttribute> attributes) {
		return attributes
			.stream()
			.filter(attribute -> attribute.name.equals(FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE))
			.flatMap(attribute -> attribute.values.stream())
			.map(PolicyAcceptanceParser::parse)
			.map(PolicyAcceptanceArgument::toPolicyAcceptance)
			.collect(toSet());
	}

	public Set<PolicyAcceptance> getPolicyAcceptancesFromAttributes(Collection<? extends RestAttributeExt> attributes) {
		return attributes
			.stream()
			.filter(attribute -> attribute.name.equals(FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE))
			.flatMap(attribute -> attribute.values.stream())
			.map(PolicyAcceptanceParser::parse)
			.map(PolicyAcceptanceArgument::toPolicyAcceptance)
			.collect(toSet());
	}

	public Optional<FURMSUser> getUser(PersistentId userId){
		RestEntity entity  = getEntity(userId).orElse(null);
		if (entity == null)
			return Optional.empty();
		List<RestAttribute> attributesFromGroup = getAttributesFromRootGroup(userId);
		return UnityUserMapper.map(userId, entity, attributesFromGroup);
	}

	public Optional<FURMSUser> getUser(FenixUserId userId) {
		RestEntity entity = getEntity(userId).orElse(null);
		if (entity == null)
			return Optional.empty();
		List<RestAttribute> attributesFromGroup = getAttributesFromRootGroup(userId);
		return UnityUserMapper.map(userId, entity, attributesFromGroup);
	}
	
	private List<RestAttribute> getAttributesFromGroup(PersistentId userId, String group) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(GROUP_ATTRIBUTES)
			.uriVariables(Map.of(ID, userId.id))
			.build()
			.toUriString();
		Map<String, List<RestAttribute>> groupedAttributes =
			unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(GROUPS_PATTERNS, ALL_GROUPS_PATTERNS));
		return groupedAttributes.getOrDefault(group, Collections.emptyList());
	}

	private List<RestAttribute> getAttributesFromRootGroup(PersistentId userId) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(ENTITY_ATTRIBUTES)
			.uriVariables(Map.of(ID, userId.id))
			.build()
			.toUriString();
		return unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(GROUP, ROOT_GROUP));
	}

	private List<RestAttribute> getAttributesFromRootGroup(FenixUserId userId) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(ENTITY_ATTRIBUTES)
			.uriVariables(Map.of(ID, userId.id))
			.build()
			.toUriString();
		return unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(GROUP, ROOT_GROUP, IDENTITY_TYPE, IDENTIFIER_IDENTITY));
	}
	
	private Optional<RestEntity> getEntity(PersistentId userId) {
		try {
			RestEntity entity = getEntityThrowing(userId);
			return Optional.of(entity);
		} catch (BadRequest e) {
			LOG.debug("User {} not found", userId, e);
			return Optional.empty();
		}
	}
	
	private Optional<RestEntity> getEntity(FenixUserId userId) {
		try {
			RestEntity entity = getEntityThrowing(userId);
			return Optional.of(entity);
		} catch (BadRequest e) {
			LOG.debug("User {} not found", userId, e);
			return Optional.empty();
		}
	}

	private RestEntity getEntityThrowing(PersistentId userId) {
		String path = UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.path(userId.id)
			.build()
			.toUriString();
		return unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
	}
	
	private RestEntity getEntityThrowing(FenixUserId userId) {
		String path = UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.path(userId.id)
			.build()
			.toUriString();
		return unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(IDENTITY_TYPE, IDENTIFIER_IDENTITY));
	}
	
	public List<FURMSUser> getAllUsersByRole(String group, Role role) {
		Predicate<RestAttributeExt> filter = attribute ->
			attribute.name.equals(role.unityRoleAttribute) &&
				attribute.values.contains(role.unityRoleValue);
		return getAllUsersFromGroup(group, filter);
	}

	public List<FURMSUser> getAllUsersByRoles(String group, Set<Role> roles) {
		Predicate<RestAttributeExt> filter = attribute -> {
			for(Role role : roles){
				if(role.unityRoleAttribute.equals(attribute.name) && attribute.values.contains(role.unityRoleValue))
					return true;
			}
			return false;
		};
		return getAllUsersFromGroup(group, filter);
	}

	public List<FURMSUser> getAllUsersFromGroup(String group, Predicate<RestAttributeExt> filter){
		Map<String, String> uriVariables = Map.of(ROOT_GROUP_PATH, group);
		String path = UriComponentsBuilder.newInstance()
			.path(GROUP_MEMBERS)
			.pathSegment("{" + ROOT_GROUP_PATH + "}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();

		return unityClient.get(path, new ParameterizedTypeReference<List<RestGroupMemberWithAttributes>>() {}).stream()
			.filter(groupMember -> groupMember.attributes.stream().anyMatch(filter))
			.map(x -> UnityUserMapper.map(x, group))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(toList());
	}

	public Set<UserPolicyAcceptances> getAllUsersPolicyAcceptanceFromGroups(Map<String, Set<String>> ids){
		String path = UriComponentsBuilder.newInstance()
			.path(GROUP_MEMBERS_MULTI)
			.encode()
			.toUriString();

		List<String> allGroups = ids.entrySet().stream()
			.map(entry -> getProjectPaths(entry.getKey(), entry.getValue()))
			.flatMap(Collection::stream)
			.collect(toList());
		
		Map<String, List<RestGroupMemberWithAttributes>> members = GroupMembersMultiPaginationProvider.get(allGroups).stream()
			.map(page -> unityClient.get(path, Map.of("groups", page.getGroups()), 
					new ParameterizedTypeReference<RestMultiGroupMembersWithAttributes>() {}))
			.map(attr -> attr.members)
			.flatMap(map -> map.entrySet().stream())
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		return allGroups.stream()
			.flatMap(group -> members.getOrDefault(group, List.of()).stream()
				.map(attributes -> UnityUserMapper.map(attributes, group)
					.map(user -> new UserPolicyAcceptances(user, getPolicyAcceptancesFromAttributes(attributes.attributes)))
				)
			)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(toSet());
	}

	public GroupedUsers getUsersFromGroupsFilteredByRoles(Map<String, Set<Role>> groupsWithRoles) {
		String path = UriComponentsBuilder.newInstance()
			.path(GROUP_MEMBERS_MULTI)
			.encode()
			.toUriString();

		RestMultiGroupMembersWithAttributes multiGroupMembers = unityClient.get(path, Map.of("groups",
				new ArrayList<>(groupsWithRoles.keySet())),
			new ParameterizedTypeReference<>() {});

		Map<String, List<FURMSUser>> groupedUsers = multiGroupMembers.members.entrySet().stream()
			.collect(toMap(
				Map.Entry::getKey,
				entry -> filerUsersByRoles(groupsWithRoles.get(entry.getKey()), getUsers(entry.getValue(), entry.getKey())))
			);

		return new GroupedUsers(groupedUsers);
	}

	private List<FURMSUser> filerUsersByRoles(Set<Role> roles, List<FURMSUser> users) {
		if(roles.isEmpty())
			return users;
		return users.stream()
			.filter(user -> user.roles.values().stream()
				.flatMap(Collection::stream)
				.anyMatch(roles::contains))
			.collect(toList());
	}

	private List<FURMSUser> getUsers(List<RestGroupMemberWithAttributes> simpleGroupMembers, String group) {
		return simpleGroupMembers.stream()
			.map(u -> UnityUserMapper.map(u, group))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(toList());
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
		return getEntityThrowing(userId).identities.stream()
				.filter(identity -> identity.typeId.equals(PERSISTENT_IDENTITY)).findAny()
				.map(identity -> identity.comparableValue).map(PersistentId::new).orElse(null);

	}
	
	public FenixUserId getFenixUserId(PersistentId userId) {
		return getEntityThrowing(userId).identities.stream()
				.filter(identity -> identity.typeId.equals(IDENTIFIER_IDENTITY)).findAny()
				.map(identity -> identity.comparableValue).map(FenixUserId::new).orElse(null);

	}
}
