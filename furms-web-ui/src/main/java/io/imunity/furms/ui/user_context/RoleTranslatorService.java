/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

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

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;

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
	public Map<ViewMode, List<FurmsViewUserContext>> translateRolesToUserViewContexts(){
		authzService.reloadRoles();
		if(authzService.getRoles().isEmpty()){
			return Map.of(USER, List.of(new FurmsViewUserContext(USER_PROPERTIES_CONTEXT_ID, "User settings", USER)));
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
		FurmsViewUserContext userSettings = new FurmsViewUserContext(USER_PROPERTIES_CONTEXT_ID, "User settings", USER);
		switch (role) {
			case FENIX_ADMIN:
				return Stream.of(new FurmsViewUserContext(FENIX_ADMIN_CONTEXT_ID, "Fenix admin", FENIX), userSettings);
			case SITE_ADMIN:
				return siteService.findById(resourceId.id.toString())
					.map(site -> Stream.of(
						new FurmsViewUserContext(site.getId(), getAdminName(site.getName()), SITE),
						userSettings)
					)
					.orElseGet(() -> {
						LOG.warn("Wrong resource id. Data are not synchronized");
						return Stream.empty();
					});
			case SITE_SUPPORT:
				return siteService.findById(resourceId.id.toString())
					.map(site ->
						Stream.of(
							new FurmsViewUserContext(site.getId(), getAdminName(site.getName()), SITE, SITE_SUPPORT_LANDING_PAGE),
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
						new FurmsViewUserContext(community.getId(), getAdminName(community.getName()), COMMUNITY),
						userSettings)
					)
					.orElseGet(() -> {
						LOG.warn("Wrong resource id. Data are not synchronized");
						return Stream.empty();
					});
			case PROJECT_ADMIN:
				return projectService.findById(resourceId.id.toString())
					.map(project ->
						Stream.of(
							new FurmsViewUserContext(project.getId(), getAdminName(project.getName()), PROJECT),
							userSettings)
					)
					.orElseGet(() -> {
						LOG.warn("Wrong resource id. Data are not synchronized");
						return Stream.empty();
					});
			case PROJECT_MEMBER:
				return projectService.findById(resourceId.id.toString())
					.map(project ->
						Stream.of(
							new FurmsViewUserContext(project.getId(), getMemberName(project.getName()), PROJECT),
							userSettings)
					)
					.orElseGet(() -> {
						LOG.warn("Wrong resource id. Data are not synchronized");
						return Stream.empty();
					});
			default:
				throw new IllegalArgumentException("This shouldn't happen, viewMode level should be always declared");
		}
	}

	private String getAdminName(String name) {
		return name + " " + getTranslation("admin");
	}

	private String getMemberName(String name) {
		return name + " " + getTranslation("member");
	}
}
