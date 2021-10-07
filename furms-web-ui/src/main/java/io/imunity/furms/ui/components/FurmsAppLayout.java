/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserEvent;
import io.imunity.furms.ui.FurmsLayoutFactory;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.VaadinListener;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
@CssImport("./styles/custom-lumo-theme.css")
@Theme(value = Lumo.class)
@PreserveOnRefresh
@Push
public class FurmsAppLayout extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver  {
	private final RoleTranslator roleTranslator;
	private final VaadinBroadcaster vaadinBroadcaster;
	private final AuthzService authzService;
	private final ViewMode viewMode;
	private final PersistentId currentUserId;
	private final FurmsLayout furmsLayout;

	private Registration broadcasterRegistration;

	protected FurmsAppLayout(RoleTranslator roleTranslator, VaadinBroadcaster vaadinBroadcaster, AuthzService authzService, ViewMode viewMode, FurmsLayoutFactory furmsLayoutFactory, List<MenuComponent> menuComponents) {
		this.roleTranslator = roleTranslator;
		this.vaadinBroadcaster = vaadinBroadcaster;
		this.authzService = authzService;
		this.viewMode = viewMode;
		this.currentUserId = authzService.getCurrentAuthNUser().id.get();

		setPrimarySection(Section.DRAWER);
		this.furmsLayout = furmsLayoutFactory.create(menuComponents);
		addToNavbar(false, this.furmsLayout.createNavbar());
		addToDrawer(this.furmsLayout.createDrawerContent());
	}

	@Override
	public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
		if(FurmsViewUserContext.getCurrent() == null) {
			setCurrentRole(beforeEnterEvent);
			furmsLayout.reloadUserPicker();
		}
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		furmsLayout.afterNavigation(getContent());
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		UI ui = attachEvent.getUI();
		broadcasterRegistration = vaadinBroadcaster.register(
			VaadinListener.builder()
				.consumer(event -> ui.access(authzService::reloadRoles))
				.predicate(this::isCurrentUserRoleListChanged)
				.build()
		);
	}

	private boolean isCurrentUserRoleListChanged(FurmsEvent furmsEvent) {
		if(!(furmsEvent instanceof UserEvent))
			return false;
		UserEvent event = (UserEvent) furmsEvent;
		return currentUserId.equals(event.getId());
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		broadcasterRegistration.remove();
		broadcasterRegistration = null;
	}

	private void setCurrentRole(BeforeEnterEvent beforeEnterEvent) {
		beforeEnterEvent.getLocation()
			.getSubLocation()
			.map(Location::getQueryParameters)
			.flatMap(x -> Optional.ofNullable(x.getParameters().get("resourceId")))
			.filter(x -> !x.isEmpty())
			.map(x -> x.iterator().next())
			.ifPresentOrElse(this::setCurrentRoleFromQueryParam, this::setAnyCurrentRole);
	}

	private void setAnyCurrentRole() {
		roleTranslator.refreshAuthzRolesAndGetRolesToUserViewContexts()
			.getOrDefault(viewMode, emptyList()).stream().findAny()
			.ifPresent(FurmsViewUserContext::setAsCurrent);
	}

	private void setCurrentRoleFromQueryParam(String id) {
		roleTranslator.refreshAuthzRolesAndGetRolesToUserViewContexts()
			.getOrDefault(viewMode, emptyList()).stream()
			.filter(x -> x.id.equals(id))
			.findAny()
			.ifPresent(FurmsViewUserContext::setAsCurrent);
	}
}
