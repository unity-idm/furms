/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.unity;

import static io.imunity.furms.unity.client.common.UnityConst.FENIX_PATTERN;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import org.springframework.util.AntPathMatcher;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import pl.edu.icm.unity.types.basic.Attribute;

public class UnityGroupParser {
	public final static Predicate<String> usersGroupPredicate = group -> group.endsWith("users");
	public final static Predicate<Attribute> usersGroupPredicate4Attr = a -> a.getGroupPath().endsWith("users");

	private static final Map<String, ResourceType> resourcesPatterns = Map.of(
		FENIX_PATTERN, ResourceType.APP_LEVEL,
		"/fenix/sites/*/users", ResourceType.SITE,
		"/fenix/communities/*/users", ResourceType.COMMUNITY,
		"/fenix/communities/*/projects/*/users", ResourceType.PROJECT
	);
	private static final AntPathMatcher matcher = new AntPathMatcher();

	public static ResourceId attr2Resource(Attribute attribute) {
		return getResourceId(attribute.getGroupPath());
	}
	
	public static ResourceId getResourceId(String group){
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
