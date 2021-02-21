/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.users;

import io.imunity.furms.domain.users.User;
import io.imunity.furms.unity.common.AttributeValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.GroupMember;
import pl.edu.icm.unity.types.basic.Identity;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import static io.imunity.furms.unity.common.UnityConst.PERSISTENT_IDENTITY;

public class UnityUserMapper {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static Optional<User> map(GroupMember groupMember){
		User user = buildUser(groupMember);
		if(user.id == null || user.email == null) {
			LOG.error("User " + user.id + " has skipped, because it doesn't have email property");
			return Optional.empty();
		}
		return Optional.of(user);
	}

	public static Optional<User> map(String userId, List<Attribute> attributes){
		User user = buildUser(userId, attributes);
		if(user.id == null || user.email == null) {
			LOG.error("User " + user.id + " has skipped, because it doesn't have email property");
			return Optional.empty();
		}
		return Optional.of(user);
	}

	private static User buildUser(GroupMember groupMember) {
		return User.builder()
			.id(getId(groupMember))
			.firstName(getFirstAttributeValue(groupMember, "firstname"))
			.lastName(getFirstAttributeValue(groupMember, "surname"))
			.email(getFirstAttributeValue(groupMember, "email"))
			.build();
	}

	private static User buildUser(String userId, List<Attribute> attributes) {
		return User.builder()
			.id(userId)
			.firstName(getFirstAttributeValue(attributes, "firstname"))
			.lastName(getFirstAttributeValue(attributes, "surname"))
			.email(getFirstAttributeValue(attributes, "email"))
			.build();
	}

	private static String getId(GroupMember groupMember) {
		return groupMember.getEntity().getIdentities().stream()
			.filter(identity -> identity.getTypeId().equals(PERSISTENT_IDENTITY))
			.findAny()
			.map(Identity::getComparableValue)
			.orElse(null);
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
