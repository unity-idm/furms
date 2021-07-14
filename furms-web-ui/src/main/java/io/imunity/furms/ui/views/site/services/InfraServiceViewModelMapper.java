/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.services;

import io.imunity.furms.domain.services.InfraService;

class InfraServiceViewModelMapper {
	static InfraServiceViewModel map(InfraService infraService) {
		return InfraServiceViewModel.builder()
			.id(infraService.id)
			.siteId(infraService.siteId)
			.name(infraService.name)
			.description(infraService.description)
			.policyId(infraService.policyId)
			.build();
	}

	public static InfraService map(InfraServiceViewModel serviceViewModel){
		return InfraService.builder()
			.id(serviceViewModel.getId())
			.siteId(serviceViewModel.getSiteId())
			.name(serviceViewModel.getName())
			.description(serviceViewModel.getDescription())
			.policyId(serviceViewModel.getPolicyId())
			.build();
	}
}
