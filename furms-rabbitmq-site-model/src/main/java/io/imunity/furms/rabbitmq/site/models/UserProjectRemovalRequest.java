/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("UserProjectRemovalRequest")
public class UserProjectRemovalRequest implements Body {
	public final String fenixUserId;
	public final String projectIdentifier;

	@JsonCreator
	public UserProjectRemovalRequest(String fenixUserId, String projectIdentifier) {
		this.fenixUserId = fenixUserId;
		this.projectIdentifier = projectIdentifier;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserProjectRemovalRequest that = (UserProjectRemovalRequest) o;
		return Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(projectIdentifier, that.projectIdentifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixUserId, projectIdentifier);
	}

	@Override
	public String toString() {
		return "UserProjectRemovalRequest{" +
			"fenixUserId='" + fenixUserId + '\'' +
			", projectIdentifier='" + projectIdentifier + '\'' +
			'}';
	}
}
