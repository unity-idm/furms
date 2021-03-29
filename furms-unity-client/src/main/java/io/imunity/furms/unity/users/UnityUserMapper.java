/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.users;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.unity.common.AttributeValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.GroupMember;
import pl.edu.icm.unity.types.basic.Identity;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import static io.imunity.furms.domain.users.UserStatus.*;
import static io.imunity.furms.unity.common.UnityConst.PERSISTENT_IDENTITY;
import static org.springframework.util.StringUtils.isEmpty;
import static pl.edu.icm.unity.types.basic.EntityState.onlyLoginPermitted;
import static pl.edu.icm.unity.types.basic.EntityState.valid;

public class UnityUserMapper {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static Optional<FURMSUser> map(GroupMember groupMember){
		FURMSUser user = buildUser(groupMember);
		if(user.id == null || user.email == null) {
			LOG.error("User " + user.id + " has skipped, because it doesn't have email property");
			return Optional.empty();
		}
		return Optional.of(user);
	}

	public static Optional<FURMSUser> map(PersistentId userId, List<Attribute> attributes){
		FURMSUser user = buildUser(userId, attributes);
		if(user.id == null || user.email == null) {
			LOG.error("User " + user.id + " has skipped, because it doesn't have email property");
			return Optional.empty();
		}
		return Optional.of(user);
	}

	private static FURMSUser buildUser(GroupMember groupMember) {
		return FURMSUser.builder()
			.id(new PersistentId(getId(groupMember)))
			.firstName(getFirstAttributeValue(groupMember, "firstname"))
			.lastName(getFirstAttributeValue(groupMember, "surname"))
			.email(getFirstAttributeValue(groupMember, "email"))
			.status(getStatus(groupMember))
			.build();
	}

	private static FURMSUser buildUser(PersistentId userId, List<Attribute> attributes) {
		return FURMSUser.builder()
			.id(userId)
			.firstName(getFirstAttributeValue(attributes, "firstname"))
			.lastName(getFirstAttributeValue(attributes, "surname"))
			.email(getFirstAttributeValue(attributes, "email"))
			.status(getStatus(attributes))
			.build();
	}

	private static String getId(GroupMember groupMember) {
		return groupMember.getEntity().getIdentities().stream()
			.filter(identity -> identity.getTypeId().equals(PERSISTENT_IDENTITY))
			.findAny()
			.map(Identity::getComparableValue)
			.orElse(null);
	}


	private static UserStatus getStatus(final GroupMember groupMember) {
		return getStatus(groupMember.getEntity().getEntityInformation().getState().name());
	}

	private static UserStatus getStatus(final List<Attribute> attributes) {
		return getStatus(getFirstAttributeValue(attributes, "entityState"));
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

	private static String getFirstAttributeValue(List<Attribute> attributes, String attributeValue) {
		return attributes
			.stream()
			.filter(attribute -> attribute.getName().equals(attributeValue))
			.filter(attribute -> !attribute.getValues().isEmpty())
			.map(attribute -> AttributeValueMapper.toFurmsAttributeValue(attribute, attribute.getValues().get(0)))
			.findFirst()
			.orElse(null);
	}
}
