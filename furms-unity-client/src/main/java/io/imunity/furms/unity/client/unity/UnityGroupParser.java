/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.unity;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import org.springframework.util.AntPathMatcher;
import pl.edu.icm.unity.types.basic.Attribute;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static io.imunity.furms.unity.client.common.UnityConst.FENIX_PATTERN;

class UnityGroupParser {
	final static Predicate<Attribute> usersGroupPredicate = a -> a.getGroupPath().endsWith("users");

	private final static Map<String, ResourceType> resourcesPatterns = Map.of(
		FENIX_PATTERN, ResourceType.APP_LEVEL,
		"/fenix/sites/*/users", ResourceType.SITE,
		"/fenix/communities/*/users", ResourceType.COMMUNITY,
		"/fenix/communities/*/projects/*/users", ResourceType.PROJECT
	);
	private final static AntPathMatcher matcher = new AntPathMatcher();

	static ResourceId getResourceId(String group){
		if(group == null)
			throw new IllegalArgumentException("Group cannot be a null");

		UUID id = null;
		String[] groupElements = group.replaceFirst("^/", "").split("/");

		if(groupElements.length < 2)
			throw new IllegalArgumentException("Group should contain at least two elements");
		if(groupElements.length > 2){
			id = UUID.fromString(groupElements[groupElements.length - 2]);
		}
		ResourceType type = getResourceType(group);
		return new ResourceId(id, type);
	}

	private static ResourceType getResourceType(String group) {
		return resourcesPatterns.entrySet().stream()
			.filter(e -> matcher.match(e.getKey(), group))
			.findAny()
			.map(Map.Entry::getValue)
			.orElseThrow();
	}
}
