/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.projects;

import io.imunity.furms.domain.projects.Project;

import java.util.Optional;
import java.util.Set;

public interface ProjectRepository {
	Optional<Project> findById(String id);

	Set<Project> findAll();

	String create(Project community);

	String update(Project community);

	boolean exists(String id);

	boolean isUniqueName(String name);

	void delete(String id);
}
