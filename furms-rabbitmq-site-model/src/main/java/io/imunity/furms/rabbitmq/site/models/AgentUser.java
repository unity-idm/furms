/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public class AgentUser {
	public final String fenixUserId;
	public final String firstName;
	public final String lastName;
	public final String email;

	@JsonCreator
	AgentUser(String fenixUserId, String firstName, String lastName, String email) {
		this.fenixUserId = fenixUserId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentUser that = (AgentUser) o;
		return Objects.equals(fenixUserId, that.fenixUserId) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(email, that.email);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixUserId, firstName, lastName, email);
	}

	@Override
	public String toString() {
		return "AgentUser{" +
			"fenixUserId='" + fenixUserId + '\'' +
			", firstName='" + firstName + '\'' +
			", lastName='" + lastName + '\'' +
			", email='" + email + '\'' +
			'}';
	}

	public static ProjectLeaderBuilder builder() {
		return new ProjectLeaderBuilder();
	}

	public static final class ProjectLeaderBuilder {
		public String fenixUserId;
		public String firstName;
		public String lastName;
		public String email;

		private ProjectLeaderBuilder() {
		}

		public ProjectLeaderBuilder fenixUserId(String fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public ProjectLeaderBuilder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public ProjectLeaderBuilder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public ProjectLeaderBuilder email(String email) {
			this.email = email;
			return this;
		}

		public AgentUser build() {
			return new AgentUser(fenixUserId, firstName, lastName, email);
		}
	}
}
