/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.projects;

import io.imunity.furms.domain.projects.LimitedProject;
import io.imunity.furms.domain.projects.Project;

import java.util.Optional;
import java.util.Set;

public interface ProjectService {
	Optional<Project> findById(String id);

	Set<Project> findAll(String communityId);

	void create(Project project);

	void update(Project project);

	void limitedUpdate(LimitedProject project);

	void delete(String projectId, String communityId);
}
