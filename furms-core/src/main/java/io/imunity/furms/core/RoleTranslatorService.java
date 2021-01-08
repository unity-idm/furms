/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.core.config.security.user.FurmsUserContext;
import io.imunity.furms.domain.authz.UserScopeContent;
import io.imunity.furms.domain.authz.roles.RoleLevel;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.api.authz.RoleTranslator;
import io.imunity.furms.domain.sites.Site;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Service
class RoleTranslatorService implements RoleTranslator {
	private final SiteService siteService;

	public RoleTranslatorService(SiteService siteService) {
		this.siteService = siteService;
	}

	public Map<RoleLevel, List<UserScopeContent>> translateRolesToUserScopes(){
		FurmsUserContext authentication = (FurmsUserContext)getContext().getAuthentication().getPrincipal();
		LinkedHashMap<RoleLevel, List<UserScopeContent>> collect = authentication.roles.entrySet().stream()
			.flatMap(e -> e.getValue().stream().map(r -> Map.entry(e.getKey(), r)))
			.sorted(Comparator.comparingInt(x -> x.getValue().roleLevel.order))
			.collect(
				groupingBy(
					e -> e.getValue().roleLevel,
					LinkedHashMap::new,
					mapping(e -> getFurmsDisplayContener(e.getKey(), e.getValue()), toList()))
			);
		collect.put(RoleLevel.USER, List.of(new UserScopeContent("User settings", RoleLevel.USER.startUrl)));
		return collect;

	}

	private UserScopeContent getFurmsDisplayContener(ResourceId resourceId, Role role) {
		switch (role.roleLevel) {
			case FENIX:
				return new UserScopeContent("FENIX ADMIN", role.roleLevel.startUrl);
			case SITE:
				Site site = siteService.findById(resourceId.id.toString())
					.orElseThrow(() -> new IllegalArgumentException("This shouldn't happen, wrong resource id"));
				return new UserScopeContent(site.getId(), site.getName(), role.roleLevel.startUrl);
			case COMMUNITY:
				//TODO it will be change when communityService will be available
				return new UserScopeContent(resourceId.id.toString(), resourceId.id.toString(), role.roleLevel.startUrl);
			case PROJECT:
				//TODO it will be change when projectService will be available
				return new UserScopeContent(resourceId.id.toString(), resourceId.id.toString(), role.roleLevel.startUrl);
			default:
				throw new IllegalArgumentException("This shouldn't happen, role level should be always declared");
		}
	}
}
