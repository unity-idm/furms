/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import java.time.LocalDateTime;
import java.util.Objects;

public class ProjectInstallationEntity {
	public final String id;
	public final String siteExternalId;
	public final String name;
	public final String description;
	public final String communityId;
	public final String communityName;
	public final String acronym;
	public final String researchField;
	public final LocalDateTime validityStart;
	public final LocalDateTime validityEnd;
	public final String leaderId;

	ProjectInstallationEntity(String id, String siteExternalId, String name, String description, String communityId, String communityName, String acronym, String researchField, LocalDateTime validityStart, LocalDateTime validityEnd, String leaderId) {
		this.id = id;
		this.siteExternalId = siteExternalId;
		this.name = name;
		this.description = description;
		this.communityId = communityId;
		this.communityName = communityName;
		this.acronym = acronym;
		this.researchField = researchField;
		this.validityStart = validityStart;
		this.validityEnd = validityEnd;
		this.leaderId = leaderId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallationEntity that = (ProjectInstallationEntity) o;
		return Objects.equals(id, that.id) && Objects.equals(siteExternalId, that.siteExternalId) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(communityId, that.communityId) && Objects.equals(communityName, that.communityName) && Objects.equals(acronym, that.acronym) && Objects.equals(researchField, that.researchField) && Objects.equals(validityStart, that.validityStart) && Objects.equals(validityEnd, that.validityEnd) && Objects.equals(leaderId, that.leaderId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteExternalId, name, description, communityId, communityName, acronym, researchField, validityStart, validityEnd, leaderId);
	}

	@Override
	public String toString() {
		return "ProjectInstallationEntity{" +
			"id='" + id + '\'' +
			", siteExternalId='" + siteExternalId + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", communityId='" + communityId + '\'' +
			", communityName='" + communityName + '\'' +
			", acronym='" + acronym + '\'' +
			", researchField='" + researchField + '\'' +
			", validityStart=" + validityStart +
			", validityEnd=" + validityEnd +
			", leaderId='" + leaderId + '\'' +
			'}';
	}
}
