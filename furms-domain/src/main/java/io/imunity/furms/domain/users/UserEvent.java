/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import io.imunity.furms.domain.FurmsEvent;

public interface UserEvent extends FurmsEvent {
	PersistentId getId();
}
