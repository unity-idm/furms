/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.users;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.unity.common.AttributeValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.Entity;
import pl.edu.icm.unity.types.basic.EntityState;
import pl.edu.icm.unity.types.basic.GroupMember;
import pl.edu.icm.unity.types.basic.Identity;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static io.imunity.furms.domain.users.UserStatus.DISABLED;
import static io.imunity.furms.domain.users.UserStatus.ENABLED;
import static io.imunity.furms.unity.common.UnityConst.IDENTIFIER_IDENTITY;
import static io.imunity.furms.unity.common.UnityConst.PERSISTENT_IDENTITY;
import static org.springframework.util.StringUtils.isEmpty;
import static pl.edu.icm.unity.types.basic.EntityState.onlyLoginPermitted;
import static pl.edu.icm.unity.types.basic.EntityState.valid;

public class UnityUserMapper {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static Optional<FURMSUser> map(GroupMember groupMember){
		return getFurmsUser(() -> buildUser(groupMember));
	}

	public static Optional<FURMSUser> map(PersistentId userId, Entity entity, List<Attribute> attributes){
		return getFurmsUser(() -> buildUser(userId, getFenixId(entity.getIdentities()), attributes, entity));
	}

	public static Optional<FURMSUser> map(FenixUserId userId, Entity entity, List<Attribute> attributes){
		return getFurmsUser(() -> buildUser(getPersistentId(entity.getIdentities()), userId, attributes, entity));
	}

	public static Optional<FURMSUser> map(List<Identity> identities, Collection<? extends Attribute> attributes){
		return getFurmsUser(() -> buildUser(identities, attributes));
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
			.firstName(getFirstAttributeValue(groupMember, "firstname"))
			.lastName(getFirstAttributeValue(groupMember, "surname"))
			.email(getFirstAttributeValue(groupMember, "email"))
			.status(getStatus(groupMember))
			.build();
	}

	private static FURMSUser buildUser(List<Identity> identities, Collection<? extends Attribute> attributes) {
		return FURMSUser.builder()
			.id(new PersistentId(getId(identities)))
			.fenixUserId(getFenixId(identities))
			.firstName(getFirstAttributeValue(attributes, "firstname"))
			.lastName(getFirstAttributeValue(attributes, "surname"))
			.email(getFirstAttributeValue(attributes, "email"))
			.status(getStatus(attributes))
			.build();
	}

	private static FURMSUser buildUser(PersistentId userId, FenixUserId fenixUserId, List<Attribute> attributes, Entity entity) {
		return FURMSUser.builder()
			.id(userId)
			.fenixUserId(fenixUserId)
			.firstName(getFirstAttributeValue(attributes, "firstname"))
			.lastName(getFirstAttributeValue(attributes, "surname"))
			.email(getFirstAttributeValue(attributes, "email"))
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

	private static PersistentId getPersistentId(List<Identity> identities) {
		return identities.stream().filter(identity -> identity.getTypeId().equals(PERSISTENT_IDENTITY))
			.findAny().map(Identity::getComparableValue).map(PersistentId::new).orElse(null);
	}

	private static UserStatus getStatus(GroupMember groupMember) {
		return getStatus(groupMember.getEntity().getEntityInformation().getState().name());
	}

	private static UserStatus getStatus(Collection<? extends Attribute> attributes) {
		return getStatus(getFirstAttributeValue(attributes, "entityState"));
	}

	private static UserStatus getStatus(EntityState state) {
		return getStatus(state.name());
	}

	private static UserStatus getStatus(String entityState) {
		return !isEmpty(entityState) && (entityState.equals(valid.name()) || entityState.equals(onlyLoginPermitted.name()))
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
}
