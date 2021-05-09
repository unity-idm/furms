/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public class PoliciesAcceptance {
	public final String policyIdentifier;
	public final int currentVersion;
	public final int processedVersion;
	public final String serviceIdentifier;
	public final Acceptance acceptance;

	@JsonCreator
	PoliciesAcceptance(String policyIdentifier, int currentVersion, String serviceIdentifier, Acceptance acceptance, int processedVersion) {
		this.policyIdentifier = policyIdentifier;
		this.currentVersion = currentVersion;
		this.processedVersion = processedVersion;
		this.serviceIdentifier = serviceIdentifier;
		this.acceptance = acceptance;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PoliciesAcceptance that = (PoliciesAcceptance) o;
		return currentVersion == that.currentVersion &&
			processedVersion == that.processedVersion &&
			Objects.equals(policyIdentifier, that.policyIdentifier) &&
			Objects.equals(serviceIdentifier, that.serviceIdentifier) &&
			acceptance == that.acceptance;
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyIdentifier, currentVersion, processedVersion, serviceIdentifier, acceptance);
	}

	@Override
	public String toString() {
		return "PoliciesAcceptance{" +
			"policyIdentifier='" + policyIdentifier + '\'' +
			", currentVersion=" + currentVersion +
			", processedVersion=" + processedVersion +
			", serviceIdentifier='" + serviceIdentifier + '\'' +
			", acceptance=" + acceptance +
			'}';
	}
}
