/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.applications;

import io.imunity.furms.domain.applications.ProjectApplicationWithUser;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.List;
import java.util.Set;

public interface ProjectApplicationsService {
	List<ProjectApplicationWithUser> findAllApplicationsUsersForCurrentProjectAdmins();
	List<FURMSUser> findAllApplyingUsers(ProjectId projectId);
	Set<ProjectId> findAllAppliedProjectsIdsForCurrentUser();
	void createForCurrentUser(ProjectId projectId);
	void removeForCurrentUser(ProjectId projectId);
	void accept(ProjectId projectId, FenixUserId id);
	void remove(ProjectId projectId, FenixUserId id);
}
