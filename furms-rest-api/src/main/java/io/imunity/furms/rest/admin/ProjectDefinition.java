/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class ProjectDefinition extends ProjectMutableDefinition {
	public final String communityId;
	public final String acronym;
	public final String gid;

	ProjectDefinition(String name, String description, Validity validity,
			String researchField, User projectLeader,
			String communityId, String acronym, String gid) {
		super(name, description, validity, researchField, projectLeader);
		this.communityId = communityId;
		this.acronym = acronym;
		this.gid = gid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		ProjectDefinition that = (ProjectDefinition) o;
		return Objects.equals(communityId, that.communityId)
				&& Objects.equals(acronym, that.acronym)
				&& Objects.equals(gid, that.gid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), communityId, acronym, gid);
	}

	@Override
	public String toString() {
		return "ProjectDefinition{" +
				"communityId='" + communityId + '\'' +
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
