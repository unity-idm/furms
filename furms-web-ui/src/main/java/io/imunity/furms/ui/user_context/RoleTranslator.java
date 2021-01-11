/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import java.util.List;
import java.util.Map;

public interface RoleTranslator {
	Map<ViewMode, List<FurmsViewUserContext>> translateRolesToUserViewContexts();
}
