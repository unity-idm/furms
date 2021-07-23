/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.sites.Gid;

import java.util.Objects;

class Project {
	public final String id;
	public final String acronym;
	public final String name;
	public final String communityId;
	public final String researchField;
	public final String gid;
	public final String description;
	public final Validity validity;
	public final User projectLeader;

	Project(String id, String acronym, String name, String communityId, String researchField, String gid,
	               String description, Validity validity, User projectLeader) {
		this.id = id;
		this.acronym = acronym;
		this.name = name;
		this.communityId = communityId;
		this.researchField = researchField;
		this.gid = gid;
		this.description = description;
		this.validity = validity;
		this.projectLeader = projectLeader;
	}

	Project(io.imunity.furms.domain.projects.Project project, User user) {
		this(project.getId(),
				project.getAcronym(),
				project.getName(),
				project.getCommunityId(),
				project.getResearchField(),
				null,
				project.getDescription(),
				new Validity(project.getUtcStartTime(), project.getUtcEndTime()),
				user);
	}

	public Project(io.imunity.furms.domain.projects.Project project, User user, Gid gid) {
		this(project.getId(),
				project.getAcronym(),
				project.getName(),
				project.getCommunityId(),
				project.getResearchField(),
				gid.id,
				project.getDescription(),
				new Validity(project.getUtcStartTime(), project.getUtcEndTime()),
				user);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Project project = (Project) o;
		return Objects.equals(id, project.id)
				&& Objects.equals(acronym, project.acronym)
				&& Objects.equals(name, project.name)
				&& Objects.equals(communityId, project.communityId)
				&& Objects.equals(researchField, project.researchField)
				&& Objects.equals(gid, project.gid)
				&& Objects.equals(description, project.description)
				&& Objects.equals(validity, project.validity)
				&& Objects.equals(projectLeader, project.projectLeader);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, acronym, name, communityId, researchField, gid, description, validity, projectLeader);
	}

	@Override
	public String toString() {
		return "Project{" +
				"id='" + id + '\'' +
				", acronym='" + acronym + '\'' +
				", name='" + name + '\'' +
				", communityId='" + communityId + '\'' +
				", researchField='" + researchField + '\'' +
				", gid='" + gid + '\'' +
				", description='" + description + '\'' +
				", validity=" + validity +
				", projectLeader=" + projectLeader +
				'}';
	}
}
