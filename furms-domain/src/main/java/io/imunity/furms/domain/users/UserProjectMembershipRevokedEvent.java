/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */


package io.imunity.furms.domain.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;

public class UserProjectMembershipRevokedEvent extends UserRoleRevokedEvent {

	public UserProjectMembershipRevokedEvent(PersistentId id, ResourceId resourceId, String projectName) {
		super(id, resourceId, projectName, Role.PROJECT_USER);
	}

}
