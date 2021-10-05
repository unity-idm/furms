/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.applications;

import io.imunity.furms.domain.applications.ProjectApplication;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ApplicationRepository {
	Set<FenixUserId> findAllApplyingUsers(String projectId);
	Set<ProjectApplication> findAllApplyingUsers(List<UUID> projectIds);
	Set<String> findAllAppliedProjectsIds(FenixUserId userId);
	void create(String projectId, FenixUserId userId);
	void remove(String projectId, FenixUserId id);
	boolean existsBy(String projectId, FenixUserId id);
}
