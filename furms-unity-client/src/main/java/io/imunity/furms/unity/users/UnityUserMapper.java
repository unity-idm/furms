/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.unity.common.AttributeValueMapper;
import io.imunity.rest.api.RestGroupMemberWithAttributes;
import io.imunity.rest.api.types.basic.RestAttribute;
import io.imunity.rest.api.types.basic.RestAttributeExt;
import io.imunity.rest.api.types.basic.RestEntity;
import io.imunity.rest.api.types.basic.RestEntityInformation;
import io.imunity.rest.api.types.basic.RestIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.users.UserStatus.DISABLED;
import static io.imunity.furms.domain.users.UserStatus.ENABLED;
import static io.imunity.furms.unity.client.UnityGroupParser.getResourceId;
import static io.imunity.furms.unity.common.UnityConst.FENIX_GROUP;
import static io.imunity.furms.unity.common.UnityConst.IDENTIFIER_IDENTITY;
import static io.imunity.furms.unity.common.UnityConst.PERSISTENT_IDENTITY;
import static io.imunity.furms.unity.common.UnityConst.ROOT_GROUP;
import static io.imunity.furms.unity.common.UnityPaths.USERS_PATTERN;
import static org.springframework.util.StringUtils.hasText;

public class UnityUserMapper {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String FIRSTNAME = "firstname";
	private static final String SURNAME = "surname";
	private static final String EMAIL = "email";

	public static Optional<FURMSUser> map(RestGroupMemberWithAttributes groupMember, String group){
		return getFurmsUser(() -> buildUser(groupMember, group));
	}

	public static Optional<FURMSUser> map(PersistentId userId, RestEntity entity, List<RestAttribute> attributes){
		return getFurmsUser(() -> buildUser(userId, getFenixId(entity.identities), attributes, entity));
	}

	public static Optional<FURMSUser> map(FenixUserId userId, RestEntity entity, List<RestAttribute> attributes){
		return getFurmsUser(() -> buildUser(getPersistentId(entity.identities), userId, attributes, entity));
	}

	public static Optional<FURMSUser> map(List<RestIdentity> identities, Collection<? extends RestAttribute> attributes,
	                                      RestEntityInformation information, String group) {
		return getFurmsUser(() -> buildUser(identities, attributes, information, group));
	}

	private static Optional<FURMSUser> getFurmsUser(Supplier<FURMSUser> userGetter) {
		FURMSUser user;
		try {
			user = userGetter.get();
		} catch (IllegalArgumentException e) {
			LOG.error("User has skipped, because it doesn't have email property");
			return Optional.empty();
		}
		if (user.id.isEmpty()) {
			LOG.error("User has skipped, because it doesn't have id property");
			return Optional.empty();
		}
		return Optional.of(user);
	}

	private static FURMSUser buildUser(RestGroupMemberWithAttributes groupMember, String group) {
		return FURMSUser.builder()
			.id(new PersistentId(getIdFromIdentities(groupMember.identities)))
			.fenixUserId(getFenixIdFromIdentities(groupMember.identities))
			.firstName(getFirstAttributeValueFromAttributes(groupMember.attributes, "firstname"))
			.lastName(getFirstAttributeValueFromAttributes(groupMember.attributes, "surname"))
			.email(getFirstAttributeValueFromAttributes(groupMember.attributes, "email"))
			.status(getStatus(groupMember.entityInformation))
			.roles(getRolesFromAttributes(group, groupMember.attributes))
			.build();
	}

	private static Map<ResourceId, Set<Role>> getRoles(String group, Collection<? extends RestAttribute> attributeExts) {
		if(!isGroupContainingUsersInPath(group))
			return Map.of();
		ResourceId resourceId = getResourceId(group);
		Set<Role> roles = attributeExts.stream()
			.filter(attribute -> attribute.name.toUpperCase().contains("ROLE"))
			.flatMap(attribute -> attribute.values.stream()
				.map(attributeValue -> Role.translateRole(attribute.name, attributeValue)))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());
		return Map.of(resourceId, roles);
	}

	private static Map<ResourceId, Set<Role>> getRolesFromAttributes(String group,
	                                                          Collection<RestAttributeExt> attributeExts) {
		if(group.equals(FENIX_GROUP) || group.equals(ROOT_GROUP))
			return Map.of();
		ResourceId resourceId = getResourceId(group);
		Set<Role> roles = attributeExts.stream()
			.filter(attribute -> attribute.name.toUpperCase().contains("ROLE"))
			.flatMap(attribute -> attribute.values.stream()
				.map(attributeValue -> Role.translateRole(attribute.name, attributeValue)))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());
		return Map.of(resourceId, roles);
	}

	private static boolean isGroupContainingUsersInPath(String group) {
		return group.endsWith(USERS_PATTERN);
	}

	private static FURMSUser buildUser(
		List<RestIdentity> identities,
		Collection<? extends RestAttribute> attributes,
		RestEntityInformation entityInformation,
		String group) {
		return FURMSUser.builder()
			.id(new PersistentId(getId(identities)))
			.fenixUserId(getFenixId(identities))
			.firstName(getFirstAttributeValue(attributes, FIRSTNAME))
			.lastName(getFirstAttributeValue(attributes, SURNAME))
			.email(getFirstAttributeValue(attributes, EMAIL))
			.status(getStatus(entityInformation))
			.roles(getRoles(group, attributes))
			.build();
	}

	private static FURMSUser buildUser(PersistentId userId, FenixUserId fenixUserId, List<RestAttribute> attributes,
	                                   RestEntity entity) {
		return FURMSUser.builder()
			.id(userId)
			.fenixUserId(fenixUserId)
			.firstName(getFirstAttributeValue(attributes, FIRSTNAME))
			.lastName(getFirstAttributeValue(attributes, SURNAME))
			.email(getFirstAttributeValue(attributes, EMAIL))
			.status(getStatus(entity.entityInformation.state))
			.build();
	}

	private static String getId(List<RestIdentity> identities) {
		return identities.stream()
			.filter(identity -> identity.typeId.equals(PERSISTENT_IDENTITY))
			.findAny()
			.map(identity -> identity.comparableValue)
			.orElse(null);
	}

	private static String getIdFromIdentities(List<RestIdentity> identities) {
		return identities.stream()
			.filter(identity -> identity.typeId.equals(PERSISTENT_IDENTITY))
			.findAny()
			.map(identity -> identity.comparableValue)
			.orElse(null);
	}

	private static FenixUserId getFenixId(List<RestIdentity> identities) {
		return identities.stream()
			.filter(identity -> identity.typeId.equals(IDENTIFIER_IDENTITY))
			.findAny()
			.map(identity -> identity.comparableValue)
			.map(FenixUserId::new)
			.orElse(null);
	}

	private static FenixUserId getFenixIdFromIdentities(List<RestIdentity> identities) {
		return identities.stream()
			.filter(identity -> identity.typeId.equals(IDENTIFIER_IDENTITY))
			.findAny()
			.map(identity -> identity.comparableValue)
			.map(FenixUserId::new)
			.orElse(null);
	}

	private static PersistentId getPersistentId(List<RestIdentity> identities) {
		return identities.stream().filter(identity -> identity.typeId.equals(PERSISTENT_IDENTITY))
			.findAny().map(identity -> identity.comparableValue).map(PersistentId::new).orElse(null);
	}

	private static UserStatus getStatus(RestEntityInformation entityInformation) {
		return getStatus(entityInformation.state);
	}

	private static UserStatus getStatus(String entityState) {
		return hasText(entityState) && (entityState.equals("valid") || entityState.equals("onlyLoginPermitted"))
				? ENABLED
				: DISABLED;
	}

	private static String getFirstAttributeValue(Collection<? extends RestAttribute> attributes, String attributeValue) {
		return attributes
			.stream()
			.filter(attribute -> attribute.name.equals(attributeValue))
			.filter(attribute -> !attribute.values.isEmpty())
			.map(attribute -> AttributeValueMapper.toFurmsAttributeValue(attribute, attribute.values.get(0)))
			.findFirst()
			.orElse(null);
	}

	private static String getFirstAttributeValueFromAttributes(Collection<? extends RestAttributeExt> attributes,
	                                              String attributeValue) {
		return attributes
			.stream()
			.filter(attribute -> attribute.name.equals(attributeValue))
			.filter(attribute -> !attribute.values.isEmpty())
			.map(attribute -> AttributeValueMapper.toFurmsAttributeValue(attribute, attribute.values.get(0)))
			.findFirst()
			.orElse(null);
	}
}
