/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.sites.Site;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static io.imunity.furms.domain.constant.RoutesConst.SITE_SUPPORT_LANDING_PAGE;
import static io.imunity.furms.ui.user_context.ViewMode.*;
import static java.util.Comparator.comparingInt;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Service
class RoleTranslatorService implements RoleTranslator {
	private final SiteService siteService;
	private final AuthzService authzService;

	public RoleTranslatorService(SiteService siteService, AuthzService authzService) {
		this.siteService = siteService;
		this.authzService = authzService;
	}

	public Map<ViewMode, List<FurmsViewUserContext>> translateRolesToUserViewContexts(){
		return authzService.getRoles().entrySet().stream()
			.flatMap(this::getFurmsUserContextStream)
			.distinct()
			.sorted(comparingInt(user -> user.viewMode.order))
			.collect(
				groupingBy(
					user -> user.viewMode,
					LinkedHashMap::new,
					mapping(identity(), toList()))
			);
	}

	private Stream<FurmsViewUserContext> getFurmsUserContextStream(Map.Entry<ResourceId, Set<Role>> roles) {
		return roles.getValue().stream()
			.flatMap(role -> getFurmsUserContexts(roles.getKey(), role));
	}

	private Stream<FurmsViewUserContext> getFurmsUserContexts(ResourceId resourceId, Role role) {
		FurmsViewUserContext userSettings = new FurmsViewUserContext("User settings", USER);
		switch (role) {
			case FENIX_ADMIN:
				return Stream.of(new FurmsViewUserContext("FENIX ADMIN", FENIX), userSettings);
			case SITE_ADMIN:
				Site site = siteService.findById(resourceId.id.toString())
					.orElseThrow(() -> new IllegalArgumentException("This shouldn't happen, wrong resource id"));
				return Stream.of(new FurmsViewUserContext(site.getId(), site.getName(), SITE), userSettings);
			case SITE_SUPPORT:
				site = siteService.findById(resourceId.id.toString())
					.orElseThrow(() -> new IllegalArgumentException("This shouldn't happen, wrong resource id"));
				return Stream.of(
					new FurmsViewUserContext(site.getId(), site.getName(), SITE, SITE_SUPPORT_LANDING_PAGE),
					userSettings
				);
			case COMMUNITY_ADMIN:
				//TODO it will be change when communityService will be available
				return Stream.of(
					new FurmsViewUserContext(resourceId.id.toString(), resourceId.id.toString(), COMMUNITY),
					userSettings
				);
			case PROJECT_ADMIN:
				//TODO it will be change when projectService will be available
				return Stream.of(
					new FurmsViewUserContext(resourceId.id.toString(), resourceId.id.toString(), PROJECT),
					userSettings
				);
			case PROJECT_MEMBER:
				return Stream.of(userSettings);
			default:
				throw new IllegalArgumentException("This shouldn't happen, viewMode level should be always declared");
		}
	}
}
