/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */


package io.imunity.furms.domain.users;

import io.imunity.furms.domain.authz.roles.ResourceId;

public class RemoveUserProjectMembershipEvent extends RemoveUserRoleEvent {

	public RemoveUserProjectMembershipEvent(PersistentId id, ResourceId resourceId) {
		super(id, resourceId);
	}

}
