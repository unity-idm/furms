/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.imunity.furms.domain.constant.RoutesConst.SITE_SUPPORT_LANDING_PAGE;
import static io.imunity.furms.ui.user_context.ViewMode.COMMUNITY;
import static io.imunity.furms.ui.user_context.ViewMode.FENIX;
import static io.imunity.furms.ui.user_context.ViewMode.PROJECT;
import static io.imunity.furms.ui.user_context.ViewMode.SITE;
import static io.imunity.furms.ui.user_context.ViewMode.USER;
import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;
import static java.util.Comparator.comparingInt;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
class RoleTranslatorService implements RoleTranslator {
	private final SiteService siteService;
	private final CommunityService communityService;
	private final ProjectService projectService;
	private final AuthzService authzService;

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String FENIX_ADMIN_CONTEXT_ID = "__fenix_admin__";
	private static final String USER_PROPERTIES_CONTEXT_ID = "__user_settings__";

	public RoleTranslatorService(SiteService siteService, CommunityService communityService,
	                             ProjectService projectService, AuthzService authzService) {
		this.siteService = siteService;
		this.communityService = communityService;
		this.projectService = projectService;
		this.authzService = authzService;
	}

	@Override
	public Map<ViewMode, List<FurmsViewUserContext>> refreshAuthzRolesAndGetRolesToUserViewContexts(){
		authzService.reloadRoles();
		return translateRolesToUserViewContexts(authzService.getRoles());
	}

	private Map<ViewMode, List<FurmsViewUserContext>> translateRolesToUserViewContexts(Map<ResourceId, Set<Role>> roles){
		if(roles.isEmpty()){
			return Map.of(USER, List.of(new FurmsViewUserContext(USER_PROPERTIES_CONTEXT_ID, "User settings", USER)));
		}
		return roles.values().stream()
			.flatMap(Collection::stream)
			.distinct()
			.flatMap(role -> {
				Set<ResourceId> ids = roles.entrySet().stream()
					.filter(y -> y.getValue().contains(role))
					.map(Map.Entry::getKey)
					.collect(Collectors.toSet());
				return getFurmsUserContexts(ids, role);
			}).distinct()
			.sorted(comparingInt(user -> user.viewMode.order))
			.collect(
				groupingBy(
					user -> user.viewMode,
					LinkedHashMap::new,
					mapping(identity(), toList()))
			);
	}

	private Stream<FurmsViewUserContext> getFurmsUserContexts(Set<ResourceId> resourceIds, Role role) {
		FurmsViewUserContext userSettings = new FurmsViewUserContext(USER_PROPERTIES_CONTEXT_ID, "User settings", USER);
		Stream<FurmsViewUserContext> furmsViewUserContextStream = getFurmsViewUserContextStream(resourceIds, role);
		return Stream.concat(furmsViewUserContextStream, Stream.of(userSettings));
	}

	private Stream<FurmsViewUserContext> getFurmsViewUserContextStream(Set<ResourceId> resourceIds, Role role) {
		switch (role) {
			case FENIX_ADMIN:
				return Stream.of(new FurmsViewUserContext(FENIX_ADMIN_CONTEXT_ID, "Fenix admin", FENIX));
			case SITE_ADMIN:
				return siteService.findAll(resourceIds.stream().map(ResourceId::asSiteId).collect(toSet())).stream()
					.map(site -> new FurmsViewUserContext(site.getId().id.toString(), getAdminName(site.getName()), SITE));
			case SITE_SUPPORT:
				return siteService.findAll(resourceIds.stream().map(ResourceId::asSiteId).collect(toSet())).stream()
					.map(site -> new FurmsViewUserContext(site.getId().id.toString(), getSupportName(site.getName()), SITE,
						SITE_SUPPORT_LANDING_PAGE));
			case COMMUNITY_ADMIN:
				return communityService.findAll(resourceIds.stream().map(ResourceId::asCommunityId).collect(toSet())).stream()
					.map(community -> new FurmsViewUserContext(community.getId().id.toString(), getAdminName(community.getName()),
						COMMUNITY));
			case PROJECT_ADMIN:
				return projectService.findAll(resourceIds.stream().map(ResourceId::asProjectId).collect(toSet())).stream()
					.map(project -> new FurmsViewUserContext(project.getId().id.toString(), getAdminName(project.getName()),
						PROJECT));
			case PROJECT_USER:
				return Stream.empty();
			default:
				throw new IllegalArgumentException("This shouldn't happen, viewMode level should be always declared");
		}
	}

	private String getAdminName(String name) {
		return name + " " + getTranslation("admin");
	}

	private String getSupportName(String name) {
		return name + " " + getTranslation("support");
	}
}
