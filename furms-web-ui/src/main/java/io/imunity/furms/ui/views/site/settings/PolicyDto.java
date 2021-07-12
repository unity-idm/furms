/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.settings;

import io.imunity.furms.domain.policy_documents.PolicyId;

import java.util.Objects;

class PolicyDto {
	public final PolicyId id;
	public final String name;

	PolicyDto(PolicyId id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyDto policyDto = (PolicyDto) o;
		return Objects.equals(id, policyDto.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "PolicyDto{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			'}';
	}
}
