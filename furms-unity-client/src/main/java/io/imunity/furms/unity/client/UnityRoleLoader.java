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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.basic.Attribute;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static io.imunity.furms.domain.authz.roles.Role.translateRole;
import static io.imunity.furms.unity.client.UnityGroupParser.getResourceId;
import static io.imunity.furms.unity.client.UnityGroupParser.usersGroupPredicate4Attr;
import static io.imunity.furms.unity.common.UnityConst.*;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_ATTRIBUTES;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Service
public class UnityRoleLoader implements RoleLoader {

	private final UnityClient unityClient;

	public UnityRoleLoader(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public Map<ResourceId, Set<Role>> loadUserRoles(PersistentId sub) {
		Map<String, List<Attribute>> attributes = loadUserAttributes(sub);
		Map<ResourceId, Set<Role>> resourceIdSetMap = loadUserRoles(attributes);
		return resourceIdSetMap;
	}

	private Map<ResourceId, Set<Role>> loadUserRoles(Map<String, List<Attribute>> attributes) {
		return attributes.values().stream()
			.flatMap(Collection::stream)
			.filter(usersGroupPredicate4Attr)
			.collect(collectingAndThen(getAttributeMapCollector(), m -> m.entrySet().stream()))
			.filter(x -> !x.getValue().isEmpty())
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Collector<Attribute, ?, Map<ResourceId, Set<Role>>> getAttributeMapCollector() {
		return groupingBy(
			this::attr2Resource,
			flatMapping(
				this::attr2Role, 
				mapping(identity(), toSet())
			)
		);
	}

	private ResourceId attr2Resource(Attribute attribute) {
		return getResourceId(attribute.getGroupPath());
	}

	private Stream<Role> attr2Role(Attribute attribute) {
		return attribute.getValues().stream()
			.map(value -> translateRole(attribute.getName(), value))
			.filter(Optional::isPresent)
			.map(Optional::get);
	}

	private Map<String, List<Attribute>> loadUserAttributes(PersistentId persistentId) {
		String path = UriComponentsBuilder.newInstance()
			.pathSegment(GROUP_ATTRIBUTES)
			.uriVariables(Map.of(ID, persistentId.id))
			.build()
			.toUriString();

		try {
			return unityClient.get(path, new ParameterizedTypeReference<>() {}, Map.of(GROUPS_PATTERNS, ALL_GROUPS_PATTERNS));
		} catch (WebClientResponseException e) {
			throw new RoleLoadingException(e.getStatusCode().value(), e);
		}
	}
}
