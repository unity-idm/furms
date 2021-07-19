/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class ServiceId {
	public final String siteId;
	public final String serviceId;

	ServiceId(String siteId, String serviceId) {
		this.siteId = siteId;
		this.serviceId = serviceId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ServiceId serviceId1 = (ServiceId) o;
		return Objects.equals(siteId, serviceId1.siteId)
				&& Objects.equals(serviceId, serviceId1.serviceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, serviceId);
	}

	@Override
	public String toString() {
		return "ServiceId{" +
				"siteId='" + siteId + '\'' +
				", serviceId='" + serviceId + '\'' +
				'}';
	}
}
