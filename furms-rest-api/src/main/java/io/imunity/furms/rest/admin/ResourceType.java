/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

class ResourceType {
	final ResourceTypeId id;

	final String name;

	final ServiceId serviceId;

	ResourceType(ResourceTypeId id, String name, ServiceId serviceId) {
		this.id = id;
		this.name = name;
		this.serviceId = serviceId;
	}
}
