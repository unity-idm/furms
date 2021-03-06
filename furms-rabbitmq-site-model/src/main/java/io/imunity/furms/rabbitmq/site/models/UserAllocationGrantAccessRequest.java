/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("UserAllocationGrantAccessRequest")
public class UserAllocationGrantAccessRequest implements Body {
	public final String allocationIdentifier;
	public final String fenixUserId;
	public final String projectIdentifier;

	@JsonCreator
	public UserAllocationGrantAccessRequest(String allocationIdentifier, String fenixUserId, String projectIdentifier) {
		this.allocationIdentifier = allocationIdentifier;
		this.fenixUserId = fenixUserId;
		this.projectIdentifier = projectIdentifier;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAllocationGrantAccessRequest that = (UserAllocationGrantAccessRequest) o;
		return Objects.equals(allocationIdentifier, that.allocationIdentifier) &&
			Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(projectIdentifier, that.projectIdentifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(allocationIdentifier, fenixUserId, projectIdentifier);
	}

	@Override
	public String toString() {
		return "UserAllocationGrantAccessRequest{" +
			"allocationIdentifier='" + allocationIdentifier + '\'' +
			", fenixUserId='" + fenixUserId + '\'' +
			", projectIdentifier='" + projectIdentifier + '\'' +
			'}';
	}
}
