/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.authz;

import io.imunity.furms.domain.users.FURMSUser;

public interface FURMSUserProvider {
	FURMSUser getFURMSUser();
	void updateFURMSUser(FURMSUser furmsUser);
}
