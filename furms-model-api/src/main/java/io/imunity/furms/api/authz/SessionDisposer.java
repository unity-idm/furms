/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.api.authz;

import javax.servlet.http.HttpSession;

public interface SessionDisposer {
	void invalidateSession(HttpSession request);
}
