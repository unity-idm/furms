/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.FurmsLayoutFactory;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.project.administrators.ProjectAdministratorsView;
import io.imunity.furms.ui.views.project.allocations.ResourceAllocationsView;
import io.imunity.furms.ui.views.project.resource_access.ResourceAccessView;
import io.imunity.furms.ui.views.project.settings.SettingsView;
import io.imunity.furms.ui.views.project.sites.SitesView;
import io.imunity.furms.ui.views.project.users.UsersView;

import java.util.List;

public class ProjectAdminMenu extends FurmsAppLayout {
	ProjectAdminMenu(FurmsLayoutFactory furmsLayoutFactory, VaadinBroadcaster vaadinBroadcaster, AuthzService authzService, RoleTranslator roleTranslator) {
		super(roleTranslator, vaadinBroadcaster, authzService, ViewMode.PROJECT, furmsLayoutFactory, List.of(
			MenuComponent.builder(UsersView.class).build(),
			MenuComponent.builder(SitesView.class).build(),
			MenuComponent.builder(ResourceAccessView.class).build(),
			MenuComponent.builder(ResourceAllocationsView.class).build(),
			MenuComponent.builder(AlarmsView.class).build(),
			MenuComponent.builder(ProjectAdministratorsView.class).build(),
			MenuComponent.builder(SettingsView.class).build()
		));
	}
}
