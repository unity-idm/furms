/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public class PolicyAcceptance {
	public final String policyIdentifier;
	public final Integer currentVersion;
	public final String serviceIdentifier;
	public final Integer processedVersion;
	public final Acceptance acceptance;

	@JsonCreator
	public PolicyAcceptance(String policyIdentifier, Integer currentVersion, String serviceIdentifier, Integer processedVersion, Acceptance acceptance) {
		this.policyIdentifier = policyIdentifier;
		this.currentVersion = currentVersion;
		this.serviceIdentifier = serviceIdentifier;
		this.processedVersion = processedVersion;
		this.acceptance = acceptance;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyAcceptance that = (PolicyAcceptance) o;
		return Objects.equals(policyIdentifier, that.policyIdentifier) &&
			Objects.equals(currentVersion, that.currentVersion) &&
			Objects.equals(serviceIdentifier, that.serviceIdentifier) &&
			Objects.equals(processedVersion, that.processedVersion) &&
			acceptance == that.acceptance;
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyIdentifier, currentVersion, serviceIdentifier, processedVersion, acceptance);
	}

	@Override
	public String toString() {
		return "PolicyAcceptance{" +
			"policyIdentifier='" + policyIdentifier + '\'' +
			", currentVersion=" + currentVersion +
			", serviceIdentifier='" + serviceIdentifier + '\'' +
			", processedVersion=" + processedVersion +
			", acceptance=" + acceptance +
			'}';
	}

	public static PolicyAcceptanceBuilder builder() {
		return new PolicyAcceptanceBuilder();
	}

	public static final class PolicyAcceptanceBuilder {
		private String policyIdentifier;
		private Integer currentVersion;
		private String serviceIdentifier;
		private Integer processedVersion;
		private Acceptance acceptance;

		private PolicyAcceptanceBuilder() {
		}

		public PolicyAcceptanceBuilder policyIdentifier(String policyIdentifier) {
			this.policyIdentifier = policyIdentifier;
			return this;
		}

		public PolicyAcceptanceBuilder currentVersion(int currentVersion) {
			this.currentVersion = currentVersion;
			return this;
		}

		public PolicyAcceptanceBuilder serviceIdentifier(String serviceIdentifier) {
			this.serviceIdentifier = serviceIdentifier;
			return this;
		}

		public PolicyAcceptanceBuilder processedVersion(int processedVersion) {
			this.processedVersion = processedVersion;
			return this;
		}

		public PolicyAcceptanceBuilder acceptanceStatus(Acceptance acceptance) {
			this.acceptance = acceptance;
			return this;
		}

		public PolicyAcceptance build() {
			return new PolicyAcceptance(policyIdentifier, currentVersion, serviceIdentifier, processedVersion, acceptance);
		}
	}
}
