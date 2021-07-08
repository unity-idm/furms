/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.project_installation.ProjectInstallationJobStatus;

import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCZoned;

class Project extends ProjectDefinition {
	
	public final String id;

	public Project(String name, String description, Validity validity, String researchField, User projectLeader,
			String communityId, String acronym, String gid, String id) {
		super(name, description, validity, researchField, projectLeader,
				communityId, acronym, gid);
		this.id = id;
	}

	public Project(io.imunity.furms.domain.projects.Project project, Optional<ProjectInstallationJobStatus> status, User projectLeader) {
		this(project.getName(),
				project.getDescription(),
				new Validity(convertToUTCZoned(project.getUtcStartTime()), convertToUTCZoned(project.getUtcEndTime())),
				project.getResearchField(),
				projectLeader,
				project.getCommunityId(),
				project.getAcronym(),
				status.map(x -> status.get().gid)
					.orElse(null),
				project.getId());
	}

	public Project(io.imunity.furms.domain.projects.Project projectBySiteId, ProjectInstallationJobStatus status, User projectLeader) {
		this(projectBySiteId, Optional.ofNullable(status), projectLeader);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Project project = (Project) o;
		return Objects.equals(id, project.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id);
	}

	@Override
	public String toString() {
		return "Project{" +
				"id='" + id + '\'' +
				", communityId='" + communityId + '\'' +
				", acronym='" + acronym + '\'' +
				", gid='" + gid + '\'' +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", validity=" + validity +
				", researchField='" + researchField + '\'' +
				", projectLeader=" + projectLeader +
				'}';
	}
}
