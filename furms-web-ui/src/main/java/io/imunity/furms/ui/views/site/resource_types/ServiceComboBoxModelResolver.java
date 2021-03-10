/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_types;

import io.imunity.furms.domain.services.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

class ServiceComboBoxModelResolver {
	private final Map<String, ServiceComboBoxModel> map;

	public ServiceComboBoxModelResolver(Set<Service> services) {
		map = services.stream()
			.map(service -> new ServiceComboBoxModel(service.id, service.name))
			.collect(toMap(x -> x.id, x -> x));
	}

	public List<ServiceComboBoxModel> getServices(){
		return new ArrayList<>(map.values());
	}

	public String getName(String id){
		return map.get(id).name;
	}

	public ServiceComboBoxModel getService(String id){
		return map.get(id);
	}
}
