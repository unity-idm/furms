/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.rabbitmq.site.models.AgentUser;

class UserMapper {
	static AgentUser map(FURMSUser user) {
		return AgentUser.builder()
			.fenixUserId(user.id.map(persistentId -> persistentId.id).orElse(null))
			.email(user.email)
			.firstName(user.firstName.orElse(null))
			.lastName(user.lastName.orElse(null))
			.build();
	}
}
