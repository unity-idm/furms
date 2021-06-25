/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.FurmsSelectFactory;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.views.user_settings.projects.ProjectsView;
import io.imunity.furms.ui.views.user_settings.sites.SitesView;
import io.imunity.furms.ui.views.user_settings.ssh_keys.SSHKeysView;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static io.imunity.furms.domain.authz.roles.Role.hasAdminRole;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class UserSettingsMenu extends FurmsAppLayout implements AfterNavigationObserver {
	private final FurmsLayout furmsLayout;

	UserSettingsMenu(FurmsSelectFactory furmsSelectFactory, AuthzService authzService) {
		setPrimarySection(Section.DRAWER);
		final List<MenuComponent> menuComponents = Stream.of(
					MenuComponent.builder(ProfileView.class).build(),
					MenuComponent.builder(SitesView.class).build(),
					MenuComponent.builder(ProjectsView.class).build(),
					MenuComponent.builder(PolicyDocumentsView.class).build(),
					MenuComponent.builder(SSHKeysView.class).build(),
					createApiKeyElement(authzService))
				.filter(Objects::nonNull)
				.collect(toList());
		this.furmsLayout = new FurmsLayout(menuComponents, furmsSelectFactory);
		addToNavbar(false, this.furmsLayout.createNavbar());
		addToDrawer(this.furmsLayout.createDrawerContent());
	}

	private MenuComponent createApiKeyElement(AuthzService authzService) {
		final FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		if (currentAuthNUser == null || CollectionUtils.isEmpty(currentAuthNUser.roles)) {
			return null;
		}
		final Set<Role> roles = currentAuthNUser.roles.values().stream()
				.flatMap(Collection::parallelStream)
				.collect(toSet());

		return hasAdminRole(roles)
				? MenuComponent.builder(APIKeyView.class).build()
				: null;
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		furmsLayout.afterNavigation(getContent());
	}
}
