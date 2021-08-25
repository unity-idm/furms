/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("PolicyUpdate")
public class AgentPolicyUpdate implements Body {
	public final String policyIdentifier;
	public final String policyName;
	public final Integer currentVersion;
	public final String serviceIdentifier;

	@JsonCreator
	public AgentPolicyUpdate(String policyIdentifier, String policyName, Integer currentVersion, String serviceIdentifier) {
		this.policyIdentifier = policyIdentifier;
		this.policyName = policyName;
		this.currentVersion = currentVersion;
		this.serviceIdentifier = serviceIdentifier;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentPolicyUpdate that = (AgentPolicyUpdate) o;
		return Objects.equals(policyIdentifier, that.policyIdentifier) &&
			Objects.equals(policyName, that.policyName) &&
			Objects.equals(currentVersion, that.currentVersion) &&
			Objects.equals(serviceIdentifier, that.serviceIdentifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyIdentifier, policyName, currentVersion, serviceIdentifier);
	}

	@Override
	public String toString() {
		return "AgentPolicyUpdate{" +
			"policyIdentifier='" + policyIdentifier + '\'' +
			", policyName='" + policyName + '\'' +
			", currentVersion=" + currentVersion +
			", serviceIdentifier='" + serviceIdentifier + '\'' +
			'}';
	}

	public static AgentPolicyUpdateBuilder builder() {
		return new AgentPolicyUpdateBuilder();
	}

	public static final class AgentPolicyUpdateBuilder {
		private String policyIdentifier;
		private String policyName;
		private Integer currentVersion;
		private String serviceIdentifier;

		private AgentPolicyUpdateBuilder() {
		}

		public AgentPolicyUpdateBuilder policyIdentifier(String policyIdentifier) {
			this.policyIdentifier = policyIdentifier;
			return this;
		}

		public AgentPolicyUpdateBuilder policyName(String policyName) {
			this.policyName = policyName;
			return this;
		}

		public AgentPolicyUpdateBuilder currentVersion(Integer currentVersion) {
			this.currentVersion = currentVersion;
			return this;
		}

		public AgentPolicyUpdateBuilder serviceIdentifier(String serviceIdentifier) {
			this.serviceIdentifier = serviceIdentifier;
			return this;
		}

		public AgentPolicyUpdate build() {
			return new AgentPolicyUpdate(policyIdentifier, policyName, currentVersion, serviceIdentifier);
		}
	}
}
