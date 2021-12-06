/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.components.layout.FurmsAppLayoutComponentsFactory;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.components.layout.FurmsAppLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.components.layout.UserViewContextHandler;
import io.imunity.furms.ui.config.FurmsLayoutExtraPanelsConfig;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.community.adminstrators.CommunityAdminsView;
import io.imunity.furms.ui.views.community.groups.GroupsView;
import io.imunity.furms.ui.views.community.projects.ProjectView;
import io.imunity.furms.ui.views.community.projects.ProjectsView;
import io.imunity.furms.ui.views.community.settings.SettingsView;

import java.util.List;

public class CommunityAdminMenu extends FurmsAppLayout {

	CommunityAdminMenu(UserViewContextHandler userViewContextHandler,
	                   VaadinBroadcaster vaadinBroadcaster,
	                   AuthzService authzService,
	                   FurmsAppLayoutComponentsFactory componentsFactory,
	                   FurmsLayoutExtraPanelsConfig extraPanelsConfig) {
		super(userViewContextHandler, vaadinBroadcaster, authzService, componentsFactory, extraPanelsConfig,
				ViewMode.COMMUNITY,
				List.of(
						MenuComponent.builder(DashboardView.class).build(),
						MenuComponent.builder(ProjectsView.class).subViews(ProjectView.class).build(),
						MenuComponent.builder(GroupsView.class).build(),
						MenuComponent.builder(CommunityAdminsView.class).build(),
						MenuComponent.builder(SettingsView.class).build()));
	}
}
