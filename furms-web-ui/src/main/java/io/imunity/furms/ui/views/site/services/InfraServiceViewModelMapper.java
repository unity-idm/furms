/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.services;

import io.imunity.furms.domain.services.InfraService;

class InfraServiceViewModelMapper {
	static ServiceViewModel map(InfraService infraService) {
		return ServiceViewModel.builder()
			.id(infraService.id)
			.siteId(infraService.siteId)
			.name(infraService.name)
			.description(infraService.description)
			.build();
	}

	public static InfraService map(ServiceViewModel serviceViewModel){
		return InfraService.builder()
			.id(serviceViewModel.id)
			.siteId(serviceViewModel.siteId)
			.name(serviceViewModel.name)
			.description(serviceViewModel.description)
			.build();
	}
}
