/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.projects;

import io.imunity.furms.domain.projects.ProjectGroup;

import java.util.Optional;

public interface ProjectGroupsDAO {
	Optional<ProjectGroup> get(String communityId, String projectId);

	void create(ProjectGroup projectGroup);

	void update(ProjectGroup projectGroup);

	void delete(String communityId, String projectId);
}