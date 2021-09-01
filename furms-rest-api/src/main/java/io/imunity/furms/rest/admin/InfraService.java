/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

class InfraService {
	public final ServiceId id;
	public final String name;
	public final String policyId;

	InfraService(ServiceId id, String name, String policyId) {
		this.id = id;
		this.name = name;
		this.policyId = policyId;
	}

	InfraService(io.imunity.furms.domain.services.InfraService infraService) {
		this(
			new ServiceId(infraService.siteId, infraService.id),
			infraService.name,
			Optional.ofNullable(infraService.policyId)
				.map(policy -> policy.id)
				.map(UUID::toString)
				.orElse(null));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InfraService that = (InfraService) o;
		return Objects.equals(id, that.id)
				&& Objects.equals(name, that.name)
				&& Objects.equals(policyId, that.policyId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, policyId);
	}

	@Override
	public String toString() {
		return "InfraService{" +
				"id=" + id +
				", name='" + name + '\'' +
				", policyId=" + policyId +
				'}';
	}
}
