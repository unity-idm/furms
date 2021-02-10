/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import static io.imunity.furms.domain.authz.roles.Role.FENIX_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.PROJECT_USER;
import static io.imunity.furms.unity.client.common.UnityConst.ALL_GROUPS_PATTERNS;
import static io.imunity.furms.unity.client.common.UnityConst.COMMUNITY_ID;
import static io.imunity.furms.unity.client.common.UnityConst.ENUMERATION;
import static io.imunity.furms.unity.client.common.UnityConst.FENIX_GROUP;
import static io.imunity.furms.unity.client.common.UnityConst.FENIX_PATTERN;
import static io.imunity.furms.unity.client.common.UnityConst.GROUPS_PATTERNS;
import static io.imunity.furms.unity.client.common.UnityConst.GROUP_PATH;
import static io.imunity.furms.unity.client.common.UnityConst.ID;
import static io.imunity.furms.unity.client.common.UnityConst.IDENTITY_TYPE;
import static io.imunity.furms.unity.client.common.UnityConst.PERSISTENT_IDENTITY;
import static io.imunity.furms.unity.client.common.UnityConst.PROJECT_ID;
import static io.imunity.furms.unity.client.common.UnityConst.PROJECT_PATTERN;
import static io.imunity.furms.unity.client.common.UnityConst.ROOT_GROUP_PATH;
import static io.imunity.furms.unity.client.common.UnityPaths.ATTRIBUTE_PATTERN;
import static io.imunity.furms.unity.client.common.UnityPaths.ENTITY_BASE;
import static io.imunity.furms.unity.client.common.UnityPaths.GROUP_ATTRIBUTES;
import static io.imunity.furms.unity.client.common.UnityPaths.GROUP_BASE;
import static io.imunity.furms.unity.client.common.UnityPaths.GROUP_MEMBERS;
import static io.imunity.furms.unity.client.unity.UnityGroupParser.usersGroupPredicate4Attr;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.domain.users.UserAttribute;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.roles.RoleLoadingException;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.unity.client.common.AttributeValueMapper;
import io.imunity.furms.unity.client.common.UnityConst;
import io.imunity.furms.unity.client.common.UnityPaths;
import io.imunity.furms.unity.client.unity.UnityClient;
import io.imunity.furms.unity.client.unity.UnityGroupParser;
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
				attribute.getName().equals(PROJECT_USER.unityRoleAttribute) &&
				attribute.getValues().contains(PROJECT_USER.unityRoleValue)
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
			.filter(attribute -> attribute.getName().equals(PROJECT_USER.unityRoleAttribute))
			.flatMap(attribute -> attribute.getValues().stream())
			.anyMatch(attribute -> attribute.equals(PROJECT_USER.unityRoleValue));
	}

	private List<Attribute> getAttributes(String communityId, String projectId, String userId) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(GROUP_ATTRIBUTES)
			.uriVariables(Map.of(ID, userId))
			.build()
			.toUriString();
		String groupPath = prepareGroupPath(communityId, projectId);
		Map<String, List<Attribute>> groupedAttributes =
			unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(GROUPS_PATTERNS, ALL_GROUPS_PATTERNS));
		return groupedAttributes.getOrDefault(groupPath, Collections.emptyList());
	}

	@Override
	public void addProjectMemberRole(String communityId, String projectId, String userId) {
		String groupPath = prepareGroupPath(communityId, projectId);

		String addToGroupPath = prepareGroupRequestPath(userId, groupPath);
		unityClient.post(addToGroupPath, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));

		Role projectMember = PROJECT_USER;
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
		Role projectMember = PROJECT_USER;
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
			.filter(attribute -> attribute.getName().equals(PROJECT_USER.unityRoleAttribute))
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
		unityClient.put(uri, Map.of("identityType", "identifier"));
	}

	@Override
	public UserStatus getUserStatus(String fenixUserId) {
		String uri = UriComponentsBuilder.newInstance()
				.path(ENTITY_BASE)
				.path(fenixUserId)
				.toUriString();
		try {
			ObjectNode response = unityClient.get(uri, ObjectNode.class, Map.of("identityType", "identifier"));
			String statusStr = response.get("entityInformation").get("state").asText();
			EntityState unityState = EntityState.valueOf(statusStr);
			return unityState == EntityState.valid ? UserStatus.ENABLED : UserStatus.DISABLED;
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
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

	@Override
	public UserAttributes getUserAttributes(String fenixUserId) {
		Map<String, List<Attribute>> userAttributes = fetchUserAttributes(fenixUserId);
		Set<String> userGroups = fetchUserGroups(fenixUserId);
		Map<ResourceId, Set<Attribute>> resourceToAttributesMap = getResourceToAttributesMap(userAttributes);
		Map<ResourceId, Set<Attribute>> resourceToAttributesMapComplete = addEmptyMemberships(
				resourceToAttributesMap, userGroups);
		List<Attribute> rootAttributes = userAttributes.getOrDefault(UnityConst.ROOT_GROUP, emptyList()); 
		return new UserAttributes(toFurmsAttributes(rootAttributes), toFurmsAttributesMap(resourceToAttributesMapComplete));
	}

	private Map<ResourceId, Set<Attribute>> addEmptyMemberships(
			Map<ResourceId, Set<Attribute>> resourceToAttributesMap, Set<String> userGroups)
	{
		Map<ResourceId, Set<Attribute>> ret = new HashMap<>(resourceToAttributesMap);
		userGroups.stream()
			.filter(UnityGroupParser.COMMUNITY_BASE_GROUP_PREDICATE)
			.map(UnityGroupParser::getResourceId)
			.filter(resourceId -> !resourceToAttributesMap.containsKey(resourceId))
			.forEach(resourceId -> ret.put(resourceId, Collections.emptySet()));
		
		return ret;
	}

	private Map<ResourceId, Set<UserAttribute>> toFurmsAttributesMap(
			Map<ResourceId, Set<Attribute>> unityAttributesMap) {
		return unityAttributesMap.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> toFurmsAttributes(entry.getValue())));
	}
	
	private Set<UserAttribute> toFurmsAttributes(Collection<Attribute> unityAttributes) {
		return unityAttributes.stream()
				.map(this::toFurmsAttribute)
				.collect(Collectors.toSet());
	}
	
	private UserAttribute toFurmsAttribute(Attribute unityAttribute) {
		return new UserAttribute(unityAttribute.getName(), 
				unityAttribute.getValues().stream()
					.map(value -> AttributeValueMapper.toFurmsAttributeValue(unityAttribute, value))
					.collect(Collectors.toList()));
	}
	
	private Map<String, List<Attribute>> fetchUserAttributes(String fenixUserId) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(GROUP_ATTRIBUTES)
			.uriVariables(Map.of(ID, fenixUserId))
			.build()
			.toUriString();

		try {
			return unityClient.getWithListParam(path, new ParameterizedTypeReference<>() {}, 
					Map.of("groupsPatterns", List.of("/", "/fenix/**/users"),
							"identityType", List.of("identifier")));
		} catch (WebClientResponseException e) {
			throw new RoleLoadingException(e.getStatusCode().value(), e);
		}
	}
	
	private Set<String> fetchUserGroups(String fenixUserId) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(UnityPaths.ENTITY_GROUPS)
			.uriVariables(Map.of(ID, fenixUserId))
			.build()
			.toUriString();

		try {
			return unityClient.get(path, new ParameterizedTypeReference<>() {}, 
					Map.of("identityType", "identifier"));
		} catch (WebClientResponseException e) {
			throw new RoleLoadingException(e.getStatusCode().value(), e);
		}
	}
	
	private static Map<ResourceId, Set<Attribute>> getResourceToAttributesMap(Map<String, List<Attribute>> attributes) {
		return attributes.values().stream()
			.flatMap(Collection::stream)
			.filter(usersGroupPredicate4Attr)
			.collect(Collectors.groupingBy(UnityGroupParser::attr2Resource, Collectors.toSet()))
			.entrySet().stream()
			.filter(mapEntry -> !mapEntry.getValue().isEmpty())
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	

}
