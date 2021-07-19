/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class ResourceTypeId {
	public final String siteId;
	public final String typeId;

	ResourceTypeId(String siteId, String typeId) {
		this.siteId = siteId;
		this.typeId = typeId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceTypeId that = (ResourceTypeId) o;
		return Objects.equals(siteId, that.siteId)
				&& Objects.equals(typeId, that.typeId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, typeId);
	}

	@Override
	public String toString() {
		return "ResourceTypeId{" +
				"siteId='" + siteId + '\'' +
				", typeId='" + typeId + '\'' +
				'}';
	}
}
