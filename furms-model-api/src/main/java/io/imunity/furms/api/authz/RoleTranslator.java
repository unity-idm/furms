/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.authz;

import io.imunity.furms.domain.authz.UserScopeContent;
import io.imunity.furms.domain.authz.roles.RoleLevel;

import java.util.List;
import java.util.Map;

public interface RoleTranslator {
	Map<RoleLevel, List<UserScopeContent>> translateRolesToUserScopes();
}
