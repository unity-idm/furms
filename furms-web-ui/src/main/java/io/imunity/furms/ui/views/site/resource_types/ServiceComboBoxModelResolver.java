/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_types;

import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

class ServiceComboBoxModelResolver {
	private final Map<InfraServiceId, ServiceComboBoxModel> map;

	public ServiceComboBoxModelResolver(Set<InfraService> services) {
		map = services.stream()
			.map(service -> new ServiceComboBoxModel(service.id, service.name))
			.collect(toMap(x -> x.id, x -> x));
	}

	public List<ServiceComboBoxModel> getServices(){
		return new ArrayList<>(map.values());
	}

	public String getName(InfraServiceId id){
		return map.get(id).name;
	}

	public ServiceComboBoxModel getService(InfraServiceId id){
		return map.get(id);
	}
}
