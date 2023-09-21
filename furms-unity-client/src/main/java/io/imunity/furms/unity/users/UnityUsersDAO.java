/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.users;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.users.AllUsersAndFenixAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.GroupedUsers;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserAttribute;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.invitations.InvitationDAO;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.client.UnityGroupParser;
import io.imunity.furms.unity.client.users.UserService;
import io.imunity.furms.unity.common.AttributeValueMapper;
import io.imunity.furms.unity.common.UnityConst;
import io.imunity.furms.unity.common.UnityPaths;
import io.imunity.rest.api.types.basic.RestAttribute;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Role.FENIX_ADMIN;
import static io.imunity.furms.unity.client.UnityGroupParser.usersGroupPredicate4Attr;
import static io.imunity.furms.unity.common.UnityConst.FENIX_GROUP;
import static io.imunity.furms.unity.common.UnityConst.FENIX_PATTERN;
import static io.imunity.furms.unity.common.UnityConst.ID;
import static io.imunity.furms.unity.common.UnityConst.IDENTIFIER_IDENTITY;
import static io.imunity.furms.unity.common.UnityConst.PERSISTENT_IDENTITY;
import static io.imunity.furms.unity.common.UnityPaths.ENTITY_BASE;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_ATTRIBUTES;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

@Component
class UnityUsersDAO implements UsersDAO {

	private final UnityClient unityClient;
	private final UserService userService;
	private final InvitationDAO invitationDAO;

	UnityUsersDAO(UnityClient unityClient, UserService userService, InvitationDAO invitationDAO) {
		this.unityClient = unityClient;
		this.userService = userService;
		this.invitationDAO = invitationDAO;
	}

	@Override
	public List<FURMSUser> getAllUsers() {
		return userService.getAllUsersFromGroup(FENIX_GROUP, attr -> true);
	}

	@Override
	public InvitationCode inviteUser(ResourceId resourceId, Role role, String email, Instant invitationExpiration) {
		InvitationCode code = invitationDAO.createInvitation(resourceId, role, email, invitationExpiration);
		invitationDAO.sendInvitation(code);
		return code;
	}

	@Override
	public InvitationCode findByRegistrationId(String registrationId) {
		return invitationDAO.findInvitationCode(registrationId);
	}

	@Override
	public void removeInvitation(InvitationCode invitationCode) {
		invitationDAO.removeInvitation(invitationCode);
	}

	@Override
	public void resendInvitation(Invitation invitation, Instant invitationExpiration) {
		invitationDAO.updateInvitation(invitation.resourceId, invitation.role, invitation.email, invitation.code, invitationExpiration);
		invitationDAO.sendInvitation(invitation.code);
	}

	@Override
	public void resendInvitation(Invitation invitation, Instant invitationExpiration, Role role) {
		invitationDAO.updateInvitation(invitation.resourceId, role, invitation.email, invitation.code, invitationExpiration);
		invitationDAO.sendInvitation(invitation.code);
	}

	@Override
	public void setUserStatus(FenixUserId fenixUserId, UserStatus status) {
		String unityStatus = status == UserStatus.ENABLED ? "valid" : "disabled";
		String uri = UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.path(fenixUserId.id)
			.path("/status/")
			.path(unityStatus)
			.toUriString();
		try {
			unityClient.put(uri, null, Map.of("identityType", "identifier"));
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
	}

	@Override
	public UserStatus getUserStatus(FenixUserId fenixUserId) {
		String uri = UriComponentsBuilder.newInstance()
				.path(ENTITY_BASE)
				.path(fenixUserId.id)
				.toUriString();
		try {
			ObjectNode response = unityClient.get(uri, ObjectNode.class, Map.of("identityType", "identifier"));
			String statusStr = response.get("entityInformation").get("state").asText();
			return statusStr.equals("valid") ? UserStatus.ENABLED : UserStatus.DISABLED;
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
	}

	@Override
	public Optional<FURMSUser> findById(PersistentId userId) {
		return userService.getUser(userId);
	}

	@Override
	public Optional<FURMSUser> findById(FenixUserId userId) {
		return userService.getUser(userId);
	}
	
	@Override
	public PersistentId getPersistentId(FenixUserId userId) {
		return userService.getPersistentId(userId);
	}
	
	@Override
	public FenixUserId getFenixUserId(PersistentId userId) {
		return userService.getFenixUserId(userId);
	}

	@Override
	public UserAttributes getUserAttributes(FenixUserId fenixUserId) {
		Map<String, List<RestAttribute>> userAttributes = fetchUserAttributes(fenixUserId);
		Set<String> userGroups = fetchUserGroups(fenixUserId);
		return getUserAttributes(userAttributes, userGroups);
	}

	@Override
	public UserAttributes getUserAttributes(PersistentId persistentId) {
		Map<String, List<RestAttribute>> userAttributes = fetchUserAttributes(persistentId);
		Set<String> userGroups = fetchUserGroups(persistentId);
		return getUserAttributes(userAttributes, userGroups);
	}

	private UserAttributes getUserAttributes(Map<String, List<RestAttribute>> userAttributes, Set<String> userGroups) {
		Map<ResourceId, Set<RestAttribute>> resourceToAttributesMap = getResourceToAttributesMap(userAttributes);
		Map<ResourceId, Set<RestAttribute>> resourceToAttributesMapComplete = addEmptyMemberships(
			resourceToAttributesMap, userGroups);
		List<RestAttribute> rootAttributes = userAttributes.getOrDefault(UnityConst.ROOT_GROUP, emptyList());
		return new UserAttributes(toFurmsAttributes(rootAttributes),
			toFurmsAttributesMap(resourceToAttributesMapComplete));
	}

	@Override
	public AllUsersAndFenixAdmins getAllUsersAndFenixAdmins() {
		GroupedUsers groupedUsers = userService.getUsersFromGroupsFilteredByRoles(Map.of(
			FENIX_PATTERN,
			Set.of(FENIX_ADMIN),
			FENIX_GROUP,
			Set.of()
		));
		return new AllUsersAndFenixAdmins(groupedUsers.getUsers(FENIX_GROUP), groupedUsers.getUsers(FENIX_PATTERN));
	}

	private Map<ResourceId, Set<RestAttribute>> addEmptyMemberships(
			Map<ResourceId, Set<RestAttribute>> resourceToAttributesMap, Set<String> userGroups)
	{
		Map<ResourceId, Set<RestAttribute>> ret = new HashMap<>(resourceToAttributesMap);
		userGroups.stream()
			.filter(UnityGroupParser.COMMUNITY_BASE_GROUP_PREDICATE)
			.map(UnityGroupParser::getResourceId)
			.filter(resourceId -> !resourceToAttributesMap.containsKey(resourceId))
			.forEach(resourceId -> ret.put(resourceId, Collections.emptySet()));
		
		return ret;
	}

	private Map<ResourceId, Set<UserAttribute>> toFurmsAttributesMap(
			Map<ResourceId, Set<RestAttribute>> unityAttributesMap) {
		return unityAttributesMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> toFurmsAttributes(entry.getValue())));
	}
	
	private Set<UserAttribute> toFurmsAttributes(Collection<RestAttribute> unityAttributes) {
		return unityAttributes.stream()
				.map(this::toFurmsAttribute)
				.collect(Collectors.toSet());
	}
	
	private UserAttribute toFurmsAttribute(RestAttribute unityAttribute) {
		return new UserAttribute(unityAttribute.name,
				unityAttribute.values.stream()
					.map(value -> AttributeValueMapper.toFurmsAttributeValue(unityAttribute, value))
					.collect(Collectors.toList()));
	}
	
	private Map<String, List<RestAttribute>> fetchUserAttributes(FenixUserId fenixUserId) {
		return fetchUserAttributes(fenixUserId.id, IDENTIFIER_IDENTITY);
	}

	private Map<String, List<RestAttribute>> fetchUserAttributes(PersistentId persistentId) {
		return fetchUserAttributes(persistentId.id, PERSISTENT_IDENTITY);
	}

	private Map<String, List<RestAttribute>> fetchUserAttributes(String id, String identityType) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(GROUP_ATTRIBUTES)
			.uriVariables(Map.of(ID, id))
			.build()
			.toUriString();

		try {
			return unityClient.getWithListParam(path, new ParameterizedTypeReference<>() {
				},
				Map.of("groupsPatterns", List.of("/", "/fenix/**/users"),
					"identityType", List.of(identityType)));
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
	}

	private Set<String> fetchUserGroups(FenixUserId fenixUserId) {
		return fetchUserGroups(fenixUserId.id, IDENTIFIER_IDENTITY);
	}

	private Set<String> fetchUserGroups(PersistentId persistentId) {
		return fetchUserGroups(persistentId.id, PERSISTENT_IDENTITY);
	}

	private Set<String> fetchUserGroups(String id, String identityType) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(UnityPaths.ENTITY_GROUPS)
			.uriVariables(Map.of(ID, id))
			.build()
			.toUriString();

		try {
			return unityClient.get(path, new ParameterizedTypeReference<>() {
				},
				Map.of("identityType", identityType));
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
	}

	private static Map<ResourceId, Set<RestAttribute>> getResourceToAttributesMap(Map<String, List<RestAttribute>> attributes) {
		return attributes.values().stream()
			.flatMap(Collection::stream)
			.filter(usersGroupPredicate4Attr)
			.collect(Collectors.groupingBy(UnityGroupParser::attr2Resource, Collectors.toSet()))
			.entrySet().stream()
			.filter(mapEntry -> !mapEntry.getValue().isEmpty())
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
