/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.applications;

import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;

public interface ProjectApplicationEvent extends FurmsEvent {
	FenixUserId getId();
	ProjectId getProjectId();
	boolean isTargetedAt(FURMSUser user);
}
