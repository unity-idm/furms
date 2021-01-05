/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.unity;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.spi.roles.RoleLoader;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.basic.Attribute;

import java.util.*;
import java.util.stream.Collector;

import static io.imunity.furms.domain.authz.roles.Role.translateRole;
import static io.imunity.furms.unity.client.unity.UnityGroupParser.getResourceId;
import static io.imunity.furms.unity.client.unity.UnityGroupParser.usersGroupPredicate;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.*;

@Service
public class UnityRoleLoader implements RoleLoader {

	private final UnityClient unityClient;
	private final String URI = "entity/{entityId}/groups/attributes";

	public UnityRoleLoader(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public Map<ResourceId, Set<Role>> loadUserRoles(String sub){
		Map<String, List<Attribute>> attributes = loadUserAttributes(sub);
		Map<ResourceId, Set<Role>> resourceIdSetMap = loadUserRoles(attributes);
		return resourceIdSetMap;
	}

	private Map<ResourceId, Set<Role>> loadUserRoles(Map<String, List<Attribute>> attributes) {
		return attributes.values().stream()
			.flatMap(Collection::stream)
			.filter(usersGroupPredicate)
			.collect(collectingAndThen(getAttributeMapCollector(), m -> m.entrySet().stream()))
			.filter(x -> !x.getValue().isEmpty())
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Collector<Attribute, ?, Map<ResourceId, Set<Role>>> getAttributeMapCollector() {
		return groupingBy(
			attribute -> getResourceId(attribute.getGroupPath()),
			mapping(
				attribute -> translateRole(attribute.getName(), attribute.getValues().iterator().next()),
				filtering(Optional::isPresent, mapping(Optional::get, toSet()))
			)
		);
	}

	private Map<String, List<Attribute>> loadUserAttributes(String persistentId) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(URI)
			.uriVariables(Map.of("entityId", persistentId))
			.build()
			.toUriString();

		try {
			return unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of("groupsPatterns", "/fenix/**/users"));
		} catch (WebClientResponseException e) {
			return emptyMap();
		}
	}
}
