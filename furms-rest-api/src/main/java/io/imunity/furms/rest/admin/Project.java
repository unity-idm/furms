/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.sites.SiteInstalledProject;
import io.imunity.furms.rest.user.User;

import java.util.Objects;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

class Project {
	public final String id;
	public final String acronym;
	public final String name;
	public final String communityId;
	public final String researchField;
	public final Set<ProjectSiteInstallation> installations;
	public final String description;
	public final Validity validity;
	public final User projectLeader;

	Project(String id, String acronym, String name, String communityId, String researchField,
	        Set<ProjectSiteInstallation> installations, String description, Validity validity, User projectLeader) {
		this.id = id;
		this.acronym = acronym;
		this.name = name;
		this.communityId = communityId;
		this.researchField = researchField;
		this.installations = installations;
		this.description = description;
		this.validity = validity;
		this.projectLeader = projectLeader;
	}

	public Project(io.imunity.furms.domain.projects.Project project,
	               User user,
	               Set<SiteInstalledProject> projectInstallations) {
		this(project.getId(),
				project.getAcronym(),
				project.getName(),
				project.getCommunityId(),
				project.getResearchField(),
				ofNullable(projectInstallations)
					.map(installations -> installations.stream()
							.map(ProjectSiteInstallation::new)
							.collect(toSet()))
					.orElse(Set.of()),
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
				&& Objects.equals(installations, project.installations)
				&& Objects.equals(description, project.description)
				&& Objects.equals(validity, project.validity)
				&& Objects.equals(projectLeader, project.projectLeader);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, acronym, name, communityId, researchField, installations, description, validity, projectLeader);
	}

	@Override
	public String toString() {
		return "Project{" +
				"id='" + id + '\'' +
				", acronym='" + acronym + '\'' +
				", name='" + name + '\'' +
				", communityId='" + communityId + '\'' +
				", researchField='" + researchField + '\'' +
				", installations='" + installations + '\'' +
				", description='" + description + '\'' +
				", validity=" + validity +
				", projectLeader=" + projectLeader +
				'}';
	}
}
