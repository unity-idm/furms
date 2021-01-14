/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import io.imunity.furms.domain.users.User;
import pl.edu.icm.unity.types.basic.GroupMember;
import pl.edu.icm.unity.types.basic.VerifiableElementBase;
import pl.edu.icm.unity.types.basic.VerifiableEmail;

class UnityUserMapper {
	static User map(GroupMember groupMember){
		return User.builder()
			.id(groupMember.getEntity().getId().toString())
			.firstName(getAttributeValue(groupMember, "firstname"))
			.lastName(getAttributeValue(groupMember, "surname"))
			.email(getEmailValue(groupMember))
			.build();
	}

	private static String getAttributeValue(GroupMember groupMember, String attribute) {
		return groupMember.getAttributes()
			.stream()
			.filter(y -> y.getName().equals(attribute))
			.flatMap(y -> y.getValues().stream())
			.findFirst()
			.orElse(null);
	}

	private static String getEmailValue(GroupMember groupMember) {
		return groupMember.getAttributes()
			.stream()
			.filter(y -> y.getName().equals("email"))
			.flatMap(y -> y.getValues().stream())
			.map(VerifiableEmail::fromJsonString)
			.map(VerifiableElementBase::getValue)
			.findFirst()
			.orElse(null);
	}
}
