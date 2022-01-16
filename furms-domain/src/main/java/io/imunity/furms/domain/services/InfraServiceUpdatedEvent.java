/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.services;

import java.util.Objects;

public class InfraServiceUpdatedEvent implements InfraServiceEvent {
	public final InfraService oldInfraService;
	public final InfraService newInfraService;

	public InfraServiceUpdatedEvent(InfraService oldInfraService, InfraService newInfraService) {
		this.oldInfraService = oldInfraService;
		this.newInfraService = newInfraService;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InfraServiceUpdatedEvent that = (InfraServiceUpdatedEvent) o;
		return Objects.equals(oldInfraService, that.oldInfraService) &&
			Objects.equals(newInfraService, that.newInfraService);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldInfraService, newInfraService);
	}

	@Override
	public String toString() {
		return "UpdateServiceEvent{" +
			"oldInfraService='" + oldInfraService + '\'' +
			",newInfraService='" + newInfraService + '\'' +
			'}';
	}
}
