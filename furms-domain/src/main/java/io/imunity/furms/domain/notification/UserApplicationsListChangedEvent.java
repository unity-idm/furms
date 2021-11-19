/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.notification;

import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.users.FURMSUser;

@FunctionalInterface
public interface UserApplicationsListChangedEvent extends FurmsEvent {
	boolean isTargetedAt(FURMSUser user);
}
