/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.roles.RoleLoader;
import io.imunity.furms.spi.roles.RoleLoadingException;
import io.imunity.rest.api.types.basic.RestAttribute;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static io.imunity.furms.domain.authz.roles.Role.translateRole;
import static io.imunity.furms.unity.client.UnityGroupParser.getResourceId;
import static io.imunity.furms.unity.client.UnityGroupParser.usersGroupPredicate4Attr;
import static io.imunity.furms.unity.common.UnityConst.ID;
import static io.imunity.furms.unity.common.UnityPaths.DIRECT_GROUP_ATTRIBUTES;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
public class UnityRoleLoader implements RoleLoader {

	private final UnityClient unityClient;

	public UnityRoleLoader(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public Map<ResourceId, Set<Role>> loadUserRoles(PersistentId sub) {
		Map<String, List<RestAttribute>> attributes = loadUserAttributes(sub);
		return loadUserRoles(attributes);
	}

	private Map<ResourceId, Set<Role>> loadUserRoles(Map<String, List<RestAttribute>> attributes) {
		return attributes.values().stream()
			.flatMap(Collection::stream)
			.filter(usersGroupPredicate4Attr)
			.collect(collectingAndThen(getAttributeMapCollector(), m -> m.entrySet().stream()))
			.filter(x -> !x.getValue().isEmpty())
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Collector<RestAttribute, ?, Map<ResourceId, Set<Role>>> getAttributeMapCollector() {
		return groupingBy(
			this::attr2Resource,
			flatMapping(
				this::attr2Role, 
				mapping(identity(), toSet())
			)
		);
	}

	private ResourceId attr2Resource(RestAttribute attribute) {
		return getResourceId(attribute.groupPath);
	}

	private Stream<Role> attr2Role(RestAttribute attribute) {
		return attribute.values.stream()
			.map(value -> translateRole(attribute.name, value))
			.filter(Optional::isPresent)
			.map(Optional::get);
	}

	private Map<String, List<RestAttribute>> loadUserAttributes(PersistentId persistentId) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(DIRECT_GROUP_ATTRIBUTES)
			.uriVariables(Map.of(ID, persistentId.id))
			.build()
			.toUriString();

		try {
			return unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of());
		} catch (WebClientResponseException e) {
			throw new RoleLoadingException(e.getStatusCode().value(), e);
		}
	}
}
