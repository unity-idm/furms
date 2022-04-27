/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.applications;

import io.imunity.furms.domain.applications.ProjectApplication;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.List;
import java.util.Set;

public interface ApplicationRepository {
	Set<FenixUserId> findAllApplyingUsers(ProjectId projectId);
	Set<ProjectApplication> findAllApplyingUsers(List<ProjectId> projectIds);
	Set<ProjectId> findAllAppliedProjectsIds(FenixUserId userId);
	void create(ProjectId projectId, FenixUserId userId);
	void remove(ProjectId projectId, FenixUserId id);
	boolean existsBy(ProjectId projectId, FenixUserId id);
}
