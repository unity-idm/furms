/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.applications;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.List;
import java.util.Set;

public interface ApplicationService {
	List<FURMSUser> findAllApplyingUsers(String projectId);
	Set<String> findAllAppliedProjectsIdsForCurrentUser();
	void createForCurrentUser(String projectId);
	void removeForCurrentUser(String projectId);
	void accept(String projectId, FenixUserId id);
	void remove(String projectId, FenixUserId id);
}
