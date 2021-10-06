/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.applications;

import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;

public interface ApplicationEvent extends FurmsEvent {
	FenixUserId getId();
	String getProjectId();
	boolean concern(FURMSUser user);
}
