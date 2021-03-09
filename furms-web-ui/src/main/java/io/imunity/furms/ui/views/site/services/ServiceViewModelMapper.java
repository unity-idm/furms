/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.services;

import io.imunity.furms.domain.services.Service;

class ServiceViewModelMapper {
	static ServiceViewModel map(Service service) {
		return ServiceViewModel.builder()
			.id(service.id)
			.siteId(service.siteId)
			.name(service.name)
			.description(service.description)
			.build();
	}

	public static Service map(ServiceViewModel serviceViewModel){
		return Service.builder()
			.id(serviceViewModel.id)
			.siteId(serviceViewModel.siteId)
			.name(serviceViewModel.name)
			.description(serviceViewModel.description)
			.build();
	}
}
