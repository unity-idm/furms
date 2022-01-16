/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.services;

import java.util.Objects;

public class InfraServiceCreatedEvent implements InfraServiceEvent {
	public final InfraService infraService;

	public InfraServiceCreatedEvent(InfraService infraService) {
		this.infraService = infraService;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InfraServiceCreatedEvent that = (InfraServiceCreatedEvent) o;
		return Objects.equals(infraService, that.infraService);
	}

	@Override
	public int hashCode() {
		return Objects.hash(infraService);
	}

	@Override
	public String toString() {
		return "InfraServiceCreatedEvent{" +
			"infraService='" + infraService + '\'' +
			'}';
	}
}
