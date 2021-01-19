/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.projects;

import io.imunity.furms.domain.projects.Project;

import java.util.Optional;
import java.util.Set;

public interface ProjectService {
	Optional<Project> findById(String id);

	Set<Project> findAll();

	void create(Project project);

	void update(Project project);

	void delete(String projectId, String communityId);
}
