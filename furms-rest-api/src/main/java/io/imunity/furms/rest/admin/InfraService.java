/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

class InfraService {
	public final String serviceId;
	public final String name;
	public final String policyId;

	InfraService(String serviceId, String name, String policyId) {
		this.serviceId = serviceId;
		this.name = name;
		this.policyId = policyId;
	}

	InfraService(io.imunity.furms.domain.services.InfraService infraService) {
		this(infraService.id.id.toString(),
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
		return Objects.equals(serviceId, that.serviceId)
				&& Objects.equals(name, that.name)
				&& Objects.equals(policyId, that.policyId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(serviceId, name, policyId);
	}

	@Override
	public String toString() {
		return "InfraService{" +
				"serviceId=" + serviceId +
				", name='" + name + '\'' +
				", policyId=" + policyId +
				'}';
	}
}
