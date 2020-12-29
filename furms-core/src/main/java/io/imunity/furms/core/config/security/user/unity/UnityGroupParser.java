/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user.unity;

import io.imunity.furms.core.config.security.user.resource.ResourceId;
import io.imunity.furms.core.config.security.user.resource.ResourceType;
import io.imunity.furms.core.config.security.user.unity.exception.WrongGroupException;
import org.springframework.util.AntPathMatcher;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class UnityGroupParser {
	public final static Predicate<Attribute> usersGroupPredicate = a -> a.groupPath.endsWith("users");

	private final static Map<String, ResourceType> resourcesPatterns = Map.of(
		"/fenix/users", ResourceType.APP_LEVEL,
		"/fenix/sites/*/users", ResourceType.SITE,
		"/fenix/communities/*/users", ResourceType.COMMUNITY,
		"/fenix/communities/*/projects/*/users", ResourceType.PROJECT
	);
	private final static AntPathMatcher matcher = new AntPathMatcher();

	public static ResourceId getResourceId(String group){
		if(group == null)
			throw new WrongGroupException("Group cannot be a null");

		UUID id = null;
		String[] groupElements = group.replaceFirst("^/", "").split("/");

		if(groupElements.length < 2)
			throw new WrongGroupException("Group should contain at least two elements");
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
