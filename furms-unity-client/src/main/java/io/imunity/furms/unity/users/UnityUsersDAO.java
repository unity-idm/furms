/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.users;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.EntityState;

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
	public List<FURMSUser> getAdminUsers() {
		return userService.getAllUsersByRole(FENIX_PATTERN, FENIX_ADMIN);
	}

	@Override
	public List<FURMSUser> getAllUsers() {
		return userService.getAllUsersFromGroup(FENIX_GROUP, attr -> true);
	}

	@Override
	public InvitationCode inviteFenixAdmin(String email, Instant invitationExpiration) {
		String code = invitationDAO.createInvitation(email, invitationExpiration);
		invitationDAO.sendInvitation(code);
		return new InvitationCode(code);
	}

	@Override
	public InvitationCode findByRegistrationId(String registrationId) {
		return invitationDAO.findInvitationCode(registrationId);
	}

	@Override
	public void removeFenixAdminInvitation(InvitationCode invitationCode) {
		invitationDAO.removeInvitation(invitationCode.code);
	}

	@Override
	public void resendFenixAdminInvitation(InvitationCode invitationCode) {
		invitationDAO.sendInvitation(invitationCode.code);
	}

	@Override
	public void addFenixAdminRole(PersistentId userId) {
		userService.addUserToGroup(userId, FENIX_PATTERN);
		userService.addUserRole(userId, FENIX_PATTERN, FENIX_ADMIN);
	}

	@Override
	public void removeFenixAdminRole(PersistentId userId) {
		userService.removeUserFromGroup(userId, FENIX_PATTERN);
	}
	
	@Override
	public void setUserStatus(FenixUserId fenixUserId, UserStatus status) {
		EntityState unityStatus = status == UserStatus.ENABLED ? EntityState.valid : EntityState.disabled;
		String uri = UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.path(fenixUserId.id)
			.path("/status/")
			.path(unityStatus.name())
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
			EntityState unityState = EntityState.valueOf(statusStr);
			return unityState == EntityState.valid ? UserStatus.ENABLED : UserStatus.DISABLED;
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
	
	private Map<String, List<Attribute>> fetchUserAttributes(FenixUserId fenixUserId) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(GROUP_ATTRIBUTES)
			.uriVariables(Map.of(ID, fenixUserId.id))
			.build()
			.toUriString();

		try {
			return unityClient.getWithListParam(path, new ParameterizedTypeReference<>() {}, 
					Map.of("groupsPatterns", List.of("/", "/fenix/**/users"),
							"identityType", List.of("identifier")));
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
	}
	
	private Set<String> fetchUserGroups(FenixUserId fenixUserId) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(UnityPaths.ENTITY_GROUPS)
			.uriVariables(Map.of(ID, fenixUserId.id))
			.build()
			.toUriString();

		try {
			return unityClient.get(path, new ParameterizedTypeReference<>() {}, 
					Map.of("identityType", "identifier"));
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
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
