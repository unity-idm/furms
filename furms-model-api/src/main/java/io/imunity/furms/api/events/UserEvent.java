/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.events;

import io.imunity.furms.domain.authz.roles.Role;

public class UserEvent {
	public final Role role;
	public final String id;

	public UserEvent(Role role, String id) {
		this.role = role;
		this.id = id;
	}
}
