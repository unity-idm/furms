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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.unity.model.RestAttributeExt;
import pl.edu.icm.unity.model.RestEntityInformation;
import pl.edu.icm.unity.model.RestGroupMemberWithAttributes;
import pl.edu.icm.unity.model.RestIdentity;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.Entity;
import pl.edu.icm.unity.types.basic.EntityInformation;
import pl.edu.icm.unity.types.basic.EntityState;
import pl.edu.icm.unity.types.basic.GroupMember;
import pl.edu.icm.unity.types.basic.Identity;

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
import static pl.edu.icm.unity.types.basic.EntityState.onlyLoginPermitted;
import static pl.edu.icm.unity.types.basic.EntityState.valid;

public class UnityUserMapper {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String FIRSTNAME = "firstname";
	private static final String SURNAME = "surname";
	private static final String EMAIL = "email";

	public static Optional<FURMSUser> map(GroupMember groupMember){
		return getFurmsUser(() -> buildUser(groupMember));
	}

	public static Optional<FURMSUser> map(RestGroupMemberWithAttributes groupMember, String group){
		return getFurmsUser(() -> buildUser(groupMember, group));
	}

	public static Optional<FURMSUser> map(PersistentId userId, Entity entity, List<Attribute> attributes){
		return getFurmsUser(() -> buildUser(userId, getFenixId(entity.getIdentities()), attributes, entity));
	}

	public static Optional<FURMSUser> map(FenixUserId userId, Entity entity, List<Attribute> attributes){
		return getFurmsUser(() -> buildUser(getPersistentId(entity.getIdentities()), userId, attributes, entity));
	}

	public static Optional<FURMSUser> map(List<Identity> identities, Collection<? extends Attribute> attributes,
	                                      EntityInformation information, String group) {
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

	private static FURMSUser buildUser(GroupMember groupMember) {
		return FURMSUser.builder()
			.id(new PersistentId(getId(groupMember)))
			.fenixUserId(getFenixId(groupMember))
			.firstName(getFirstAttributeValue(groupMember, FIRSTNAME))
			.lastName(getFirstAttributeValue(groupMember, SURNAME))
			.email(getFirstAttributeValue(groupMember, EMAIL))
			.status(getStatus(groupMember))
			.roles(getRoles(groupMember.getGroup(), groupMember.getAttributes()))
			.build();
	}

	private static FURMSUser buildUser(RestGroupMemberWithAttributes groupMember, String group) {
		return FURMSUser.builder()
			.id(new PersistentId(getIdFromIdentities(groupMember.getIdentities())))
			.fenixUserId(getFenixIdFromIdentities(groupMember.getIdentities()))
			.firstName(getFirstAttributeValueFromAttributes(groupMember.getAttributes(), "firstname"))
			.lastName(getFirstAttributeValueFromAttributes(groupMember.getAttributes(), "surname"))
			.email(getFirstAttributeValueFromAttributes(groupMember.getAttributes(), "email"))
			.status(getStatus(groupMember.getEntityInformation()))
			.roles(getRolesFromAttributes(group, groupMember.getAttributes()))
			.build();
	}

	private static Map<ResourceId, Set<Role>> getRoles(String group, Collection<? extends Attribute> attributeExts) {
		if(!isGroupContainingUsersInPath(group))
			return Map.of();
		ResourceId resourceId = getResourceId(group);
		Set<Role> roles = attributeExts.stream()
			.filter(attribute -> attribute.getName().toUpperCase().contains("ROLE"))
			.flatMap(attribute -> attribute.getValues().stream()
				.map(attributeValue -> Role.translateRole(attribute.getName(), attributeValue)))
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
			.filter(attribute -> attribute.getName().toUpperCase().contains("ROLE"))
			.flatMap(attribute -> attribute.getValues().stream()
				.map(attributeValue -> Role.translateRole(attribute.getName(), attributeValue)))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());
		return Map.of(resourceId, roles);
	}

	private static boolean isGroupContainingUsersInPath(String group) {
		return group.endsWith(USERS_PATTERN);
	}

	private static FURMSUser buildUser(
		List<Identity> identities,
		Collection<? extends Attribute> attributes,
		EntityInformation entityInformation,
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

	private static FURMSUser buildUser(PersistentId userId, FenixUserId fenixUserId, List<Attribute> attributes, Entity entity) {
		return FURMSUser.builder()
			.id(userId)
			.fenixUserId(fenixUserId)
			.firstName(getFirstAttributeValue(attributes, FIRSTNAME))
			.lastName(getFirstAttributeValue(attributes, SURNAME))
			.email(getFirstAttributeValue(attributes, EMAIL))
			.status(getStatus(entity.getState()))
			.build();
	}

	private static String getId(GroupMember groupMember) {
		return getId(groupMember.getEntity().getIdentities());
	}

	private static String getId(List<Identity> identities) {
		return identities.stream()
			.filter(identity -> identity.getTypeId().equals(PERSISTENT_IDENTITY))
			.findAny()
			.map(Identity::getComparableValue)
			.orElse(null);
	}

	private static String getIdFromIdentities(List<RestIdentity> identities) {
		return identities.stream()
			.filter(identity -> identity.getTypeId().equals(PERSISTENT_IDENTITY))
			.findAny()
			.map(RestIdentity::getComparableValue)
			.orElse(null);
	}

	private static FenixUserId getFenixId(GroupMember groupMember) {
		return getFenixId(groupMember.getEntity().getIdentities());
	}

	private static FenixUserId getFenixId(List<Identity> identities) {
		return identities.stream()
			.filter(identity -> identity.getTypeId().equals(IDENTIFIER_IDENTITY))
			.findAny()
			.map(Identity::getComparableValue)
			.map(FenixUserId::new)
			.orElse(null);
	}

	private static FenixUserId getFenixIdFromIdentities(List<RestIdentity> identities) {
		return identities.stream()
			.filter(identity -> identity.getTypeId().equals(IDENTIFIER_IDENTITY))
			.findAny()
			.map(RestIdentity::getComparableValue)
			.map(FenixUserId::new)
			.orElse(null);
	}

	private static PersistentId getPersistentId(List<Identity> identities) {
		return identities.stream().filter(identity -> identity.getTypeId().equals(PERSISTENT_IDENTITY))
			.findAny().map(Identity::getComparableValue).map(PersistentId::new).orElse(null);
	}

	private static UserStatus getStatus(GroupMember groupMember) {
		return getStatus(groupMember.getEntity().getEntityInformation().getState().name());
	}

	private static UserStatus getStatus(EntityInformation entityInformation) {
		return getStatus(entityInformation.getState().name());
	}

	private static UserStatus getStatus(RestEntityInformation entityInformation) {
		return getStatus(entityInformation.getEntityState());
	}

	private static UserStatus getStatus(EntityState state) {
		return getStatus(state.name());
	}

	private static UserStatus getStatus(String entityState) {
		return hasText(entityState) && (entityState.equals(valid.name()) || entityState.equals(onlyLoginPermitted.name()))
				? ENABLED
				: DISABLED;
	}

	private static String getFirstAttributeValue(GroupMember groupMember, String attributeValue) {
		return groupMember.getAttributes()
			.stream()
			.filter(attribute -> attribute.getName().equals(attributeValue))
			.filter(attribute -> !attribute.getValues().isEmpty())
			.map(attribute -> AttributeValueMapper.toFurmsAttributeValue(attribute, attribute.getValues().get(0)))
			.findFirst()
			.orElse(null);
	}

	private static String getFirstAttributeValue(Collection<? extends Attribute> attributes, String attributeValue) {
		return attributes
			.stream()
			.filter(attribute -> attribute.getName().equals(attributeValue))
			.filter(attribute -> !attribute.getValues().isEmpty())
			.map(attribute -> AttributeValueMapper.toFurmsAttributeValue(attribute, attribute.getValues().get(0)))
			.findFirst()
			.orElse(null);
	}

	private static String getFirstAttributeValueFromAttributes(Collection<? extends RestAttributeExt> attributes,
	                                              String attributeValue) {
		return attributes
			.stream()
			.filter(attribute -> attribute.getName().equals(attributeValue))
			.filter(attribute -> !attribute.getValues().isEmpty())
			.map(attribute -> AttributeValueMapper.toFurmsAttributeValue(attribute, attribute.getValues().get(0)))
			.findFirst()
			.orElse(null);
	}
}
