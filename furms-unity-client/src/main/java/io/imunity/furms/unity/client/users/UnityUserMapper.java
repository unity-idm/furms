/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import io.imunity.furms.domain.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.unity.types.basic.GroupMember;
import pl.edu.icm.unity.types.basic.Identity;
import pl.edu.icm.unity.types.basic.VerifiableElementBase;
import pl.edu.icm.unity.types.basic.VerifiableEmail;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static io.imunity.furms.unity.client.common.UnityConst.PERSISTENT_IDENTITY;

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

	private static User buildUser(GroupMember groupMember) {
		return User.builder()
			.id(getId(groupMember))
			.firstName(getAttributeValue(groupMember, "firstname"))
			.lastName(getAttributeValue(groupMember, "surname"))
			.email(getEmailValue(groupMember))
			.build();
	}

	private static String getId(GroupMember groupMember) {
		return groupMember.getEntity().getIdentities().stream()
			.filter(identity -> identity.getTypeId().equals(PERSISTENT_IDENTITY))
			.findAny()
			.map(Identity::getComparableValue)
			.orElse(null);
	}

	private static String getAttributeValue(GroupMember groupMember, String attributeValue) {
		return groupMember.getAttributes()
			.stream()
			.filter(attribute -> attribute.getName().equals(attributeValue))
			.flatMap(attribute -> attribute.getValues().stream())
			.findFirst()
			.orElse(null);
	}

	private static String getEmailValue(GroupMember groupMember) {
		return groupMember.getAttributes()
			.stream()
			.filter(attribute -> attribute.getName().equals("email"))
			.flatMap(attribute -> attribute.getValues().stream())
			.map(VerifiableEmail::fromJsonString)
			.map(VerifiableElementBase::getValue)
			.findFirst()
			.orElse(null);
	}
}
