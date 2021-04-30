/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Objects;

@JsonTypeName("UserProjectAddRequest")
public class UserProjectAddRequest implements Body {
	public final AgentUser user;
	public final List<PoliciesAcceptance> policiesAcceptance;
	public final String projectIdentifier;

	@JsonCreator
	public UserProjectAddRequest(AgentUser user, List<PoliciesAcceptance> policiesAcceptance, String projectIdentifier) {
		this.user = user;
		this.policiesAcceptance = policiesAcceptance;
		this.projectIdentifier = projectIdentifier;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserProjectAddRequest that = (UserProjectAddRequest) o;
		return Objects.equals(user, that.user) &&
			Objects.equals(policiesAcceptance, that.policiesAcceptance) &&
			Objects.equals(projectIdentifier, that.projectIdentifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, policiesAcceptance, projectIdentifier);
	}

	@Override
	public String toString() {
		return "UserProjectAddRequest{" +
			"user=" + user +
			", policiesAcceptance=" + policiesAcceptance +
			", projectIdentifier='" + projectIdentifier + '\'' +
			'}';
	}
}
