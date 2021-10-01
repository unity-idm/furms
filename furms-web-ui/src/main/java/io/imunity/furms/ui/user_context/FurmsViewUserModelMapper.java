/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import io.imunity.furms.domain.users.FURMSUser;

import java.util.List;
import java.util.stream.Collectors;

public class FurmsViewUserModelMapper {
	public static List<FurmsViewUserModel> mapList(List<FURMSUser> users){
		return users.stream()
			.map(user -> new FurmsViewUserModel(user.id, user.fenixUserId, user.firstName, user.lastName, user.email))
			.collect(Collectors.toList());
	}
}
