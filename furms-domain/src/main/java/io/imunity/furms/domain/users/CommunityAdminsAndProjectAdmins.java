/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import java.util.List;
import java.util.Objects;

public class CommunityAdminsAndProjectAdmins {
	public final List<FURMSUser> communityAdmins;
	public final List<FURMSUser> projectAdmins;

	public CommunityAdminsAndProjectAdmins(List<FURMSUser> communityAdmins, List<FURMSUser> projectAdmins) {
		this.communityAdmins = communityAdmins;
		this.projectAdmins = projectAdmins;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAdminsAndProjectAdmins that = (CommunityAdminsAndProjectAdmins) o;
		return Objects.equals(projectAdmins, that.projectAdmins) && Objects.equals(communityAdmins, that.communityAdmins);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectAdmins, communityAdmins);
	}

	@Override
	public String toString() {
		return "CommunityUsersAndCommunityAdmins{" +
			"projectAdmins=" + projectAdmins +
			", communityAdmins=" + communityAdmins +
			'}';
	}
}
