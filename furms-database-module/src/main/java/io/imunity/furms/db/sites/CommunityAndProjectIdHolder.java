/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import java.util.Objects;
import java.util.UUID;

class CommunityAndProjectIdHolder {
	public final UUID communityId;
	public final UUID projectId;

	CommunityAndProjectIdHolder(UUID communityId, UUID projectId) {
		this.communityId = communityId;
		this.projectId = projectId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAndProjectIdHolder that = (CommunityAndProjectIdHolder) o;
		return Objects.equals(communityId, that.communityId) && Objects.equals(projectId, that.projectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(communityId, projectId);
	}

	@Override
	public String toString() {
		return "CommunityAndProjectIdHolder{" +
			"communityId=" + communityId +
			", projectId=" + projectId +
			'}';
	}
}
