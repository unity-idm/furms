/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class ProjectCreateRequest {

	public final String communityId;
	public final String acronym;
	public final String gid;
	public final String name;
	public final String description;
	public final Validity validity;
	public final String researchField;
	public final String projectLeaderId;

	ProjectCreateRequest(String communityId, String acronym, String gid, String name, String description,
	                            Validity validity, String researchField, String projectLeaderId) {
		this.communityId = communityId;
		this.acronym = acronym;
		this.gid = gid;
		this.name = name;
		this.description = description;
		this.validity = validity;
		this.researchField = researchField;
		this.projectLeaderId = projectLeaderId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectCreateRequest that = (ProjectCreateRequest) o;
		return Objects.equals(communityId, that.communityId)
				&& Objects.equals(acronym, that.acronym)
				&& Objects.equals(gid, that.gid)
				&& Objects.equals(name, that.name)
				&& Objects.equals(description, that.description)
				&& Objects.equals(validity, that.validity)
				&& Objects.equals(researchField, that.researchField)
				&& Objects.equals(projectLeaderId, that.projectLeaderId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(communityId, acronym, gid, name, description, validity, researchField, projectLeaderId);
	}

	@Override
	public String toString() {
		return "ProjectCreateRequest{" +
				"communityId='" + communityId + '\'' +
				", acronym='" + acronym + '\'' +
				", gid='" + gid + '\'' +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", validity=" + validity +
				", researchField='" + researchField + '\'' +
				", projectLeaderId='" + projectLeaderId + '\'' +
				'}';
	}
}
