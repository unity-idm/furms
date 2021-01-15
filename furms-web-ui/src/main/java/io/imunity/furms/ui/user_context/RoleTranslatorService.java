/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
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
	private final CommunityService communityService;
	private final AuthzService authzService;

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public RoleTranslatorService(SiteService siteService, CommunityService communityService, AuthzService authzService) {
		this.siteService = siteService;
		this.communityService = communityService;
		this.authzService = authzService;
	}

	public Map<ViewMode, List<FurmsViewUserContext>> translateRolesToUserViewContexts(){
		if(authzService.getRoles().isEmpty()){
			return Map.of(USER, List.of(new FurmsViewUserContext("User settings", USER)));
		}
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
				return Stream.of(new FurmsViewUserContext("Fenix Admin", FENIX), userSettings);
			case SITE_ADMIN:
				return siteService.findById(resourceId.id.toString())
					.map(site -> Stream.of(new FurmsViewUserContext(site.getId(), site.getName(), SITE), userSettings))
					.orElseGet(() -> {
						LOG.warn("Wrong resource id. Data are not synchronized");
						return Stream.empty();
					});
			case SITE_SUPPORT:
				return siteService.findById(resourceId.id.toString())
					.map(site ->
						Stream.of(
							new FurmsViewUserContext(site.getId(), site.getName(), SITE, SITE_SUPPORT_LANDING_PAGE),
							userSettings)
					)
					.orElseGet(() -> {
						LOG.warn("Wrong resource id. Data are not synchronized");
						return Stream.empty();
					});
			case COMMUNITY_ADMIN:
				return communityService.findById(resourceId.id.toString())
					.map(community ->
						Stream.of(
						new FurmsViewUserContext(community.getId(), community.getName(), COMMUNITY),
						userSettings)
					)
					.orElseGet(() -> {
						LOG.warn("Wrong resource id. Data are not synchronized");
						return Stream.empty();
					});
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
