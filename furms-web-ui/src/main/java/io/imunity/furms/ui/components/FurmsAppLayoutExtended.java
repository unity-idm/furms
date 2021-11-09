/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.RouterLayout;
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
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;

@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
@CssImport("./styles/custom-lumo-theme.css")
@Theme(value = Lumo.class)
@PreserveOnRefresh
@Push
public class FurmsAppLayoutExtended
		extends FlexLayout
		implements RouterLayout, AfterNavigationObserver, BeforeEnterObserver {

	private final RoleTranslator roleTranslator;
	private final VaadinBroadcaster vaadinBroadcaster;
	private final AuthzService authzService;
	private final ViewMode viewMode;
	private final PersistentId currentUserId;
	private final FurmsAppLayoutUtils furmsAppLayoutUtils;
	private Registration broadcasterRegistration;

	private final Div viewContainer;
	private Component content;

	public FurmsAppLayoutExtended(RoleTranslator roleTranslator,
	                              VaadinBroadcaster vaadinBroadcaster,
	                              AuthzService authzService,
	                              ViewMode viewMode,
	                              FurmsLayoutFactory furmsLayoutFactory,
	                              List<MenuComponent> menuComponents) {
		this.roleTranslator = roleTranslator;
		this.vaadinBroadcaster = vaadinBroadcaster;
		this.authzService = authzService;
		this.viewMode = viewMode;
		this.currentUserId = authzService.getCurrentAuthNUser().id.get();
		this.furmsAppLayoutUtils = furmsLayoutFactory.create(menuComponents);
		this. viewContainer = new Div();

		setId("furms-layout");

		final Div top = div("furms-layout-top");
		final Div left = div("furms-layout-left");
		final Div right = div("furms-layout-right");
		final Div bottom = div("furms-layout-bottom");

		final VerticalLayout menuContent = furmsAppLayoutUtils.createDrawerContent();
		menuContent.setId("furms-layout-menu");

		final VerticalLayout viewContent = new VerticalLayout();
		viewContent.setId("furms-layout-view-content");
		viewContent.setAlignItems(FlexComponent.Alignment.STRETCH);
		viewContent.add(furmsAppLayoutUtils.createNavbar(), viewContainer);

		final HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setId("furms-layout-main");
		mainLayout.add(left, menuContent, viewContent, right);

		add(top, mainLayout, bottom);
	}

	@Override
	public Element getElement() {
		return super.getElement();
	}

	@Override
	public void showRouterLayoutContent(HasElement content) {
		if (content != null) {
			final Element contentElement = content.getElement();
			this.content = contentElement.getComponent()
					.orElseThrow(() -> new IllegalArgumentException(
							"AppLayout content must be a Component"));
			viewContainer.getElement().appendChild(contentElement);
		}
	}

	@Override
	public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
		if(FurmsViewUserContext.getCurrent() == null) {
			setCurrentRole(beforeEnterEvent);
			furmsAppLayoutUtils.reloadUserPicker();
		}
		else if(!Objects.equals(FurmsViewUserContext.getCurrent(), furmsAppLayoutUtils.getUserPickerValue()))
			furmsAppLayoutUtils.reloadUserPicker();
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		furmsAppLayoutUtils.afterNavigation(content);
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

	private Div div(String id) {
		final Div div = new Div();
		div.setId(id);
		return div;
	}

}
