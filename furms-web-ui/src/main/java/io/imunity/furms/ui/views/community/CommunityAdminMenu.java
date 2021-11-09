/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.FurmsLayoutFactory;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.components.FurmsAppLayoutExtended;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.community.adminstrators.CommunityAdminsView;
import io.imunity.furms.ui.views.community.groups.GroupsView;
import io.imunity.furms.ui.views.community.projects.ProjectView;
import io.imunity.furms.ui.views.community.projects.ProjectsView;
import io.imunity.furms.ui.views.community.settings.SettingsView;

import java.util.List;

public class CommunityAdminMenu extends FurmsAppLayoutExtended {
	CommunityAdminMenu(FurmsLayoutFactory furmsLayoutFactory, VaadinBroadcaster vaadinBroadcaster, AuthzService authzService, RoleTranslator roleTranslator) {
		super(roleTranslator, vaadinBroadcaster, authzService, ViewMode.COMMUNITY, furmsLayoutFactory, List.of(
			MenuComponent.builder(DashboardView.class).build(),
			MenuComponent.builder(ProjectsView.class).subViews(ProjectView.class).build(),
			MenuComponent.builder(GroupsView.class).build(),
			MenuComponent.builder(CommunityAdminsView.class).build(),
			MenuComponent.builder(SettingsView.class).build()
			)
		);
	}
}
