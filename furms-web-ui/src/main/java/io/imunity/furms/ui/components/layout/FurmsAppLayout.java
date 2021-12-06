/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.layout;

import com.vaadin.flow.component.AttachEvent;
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
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserEvent;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.VaadinListener;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.components.branding.layout.ExtraLayoutPanel;
import io.imunity.furms.ui.config.FurmsLayoutExtraPanelsConfig;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.ViewMode;

import java.util.List;

@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
@CssImport("./styles/custom-lumo-theme.css")
@Theme(value = Lumo.class)
@PreserveOnRefresh
@Push
public class FurmsAppLayout
		extends FlexLayout
		implements RouterLayout, AfterNavigationObserver, BeforeEnterObserver {

	private final UserViewContextHandler userViewContextHandler;
	private final VaadinBroadcaster vaadinBroadcaster;
	private final AuthzService authzService;

	private final ViewMode viewMode;
	private final PersistentId currentUserId;
	private final FurmsAppLayoutComponentsHolder appLayoutComponents;

	private Registration broadcasterRegistration;

	public FurmsAppLayout(UserViewContextHandler userViewContextHandler,
	                      VaadinBroadcaster vaadinBroadcaster,
	                      AuthzService authzService,
	                      FurmsAppLayoutComponentsFactory appLayoutComponentsFactory,
	                      FurmsLayoutExtraPanelsConfig furmsLayoutExtraPanelsConfig,
	                      ViewMode viewMode,
	                      List<MenuComponent> menuComponents) {
		this.userViewContextHandler = userViewContextHandler;
		this.vaadinBroadcaster = vaadinBroadcaster;
		this.authzService = authzService;
		this.viewMode = viewMode;
		this.currentUserId = authzService.getCurrentAuthNUser().id.get();
		this.appLayoutComponents = appLayoutComponentsFactory.create(menuComponents);

		initView(furmsLayoutExtraPanelsConfig);
	}

	@Override
	public Element getElement() {
		return super.getElement();
	}

	@Override
	public void showRouterLayoutContent(HasElement content) {
		appLayoutComponents.setViewContent(content);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
		final FurmsViewUserContext currentViewUserContext = FurmsViewUserContext.getCurrent();
		if(currentViewUserContext == null || currentViewUserContext.viewMode != viewMode) {
			userViewContextHandler.setUserViewContext(beforeEnterEvent, viewMode);
			appLayoutComponents.reloadUserPicker();
		}
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		appLayoutComponents.reloadMenuAndBreadCrumb();
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

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		broadcasterRegistration.remove();
		broadcasterRegistration = null;
	}

	private void initView(FurmsLayoutExtraPanelsConfig furmsLayoutExtraPanelsConfig) {
		setId("furms-layout");

		final Div top = new ExtraLayoutPanel("furms-layout-top", furmsLayoutExtraPanelsConfig.getTop());
		final Div left = new ExtraLayoutPanel("furms-layout-left", furmsLayoutExtraPanelsConfig.getLeft());
		final Div right = new ExtraLayoutPanel("furms-layout-right", furmsLayoutExtraPanelsConfig.getRight());
		final Div bottom = new ExtraLayoutPanel("furms-layout-bottom", furmsLayoutExtraPanelsConfig.getBottom());

		final VerticalLayout menuContent = appLayoutComponents.getLogoMenuContainer();
		menuContent.setId("furms-layout-menu");

		final VerticalLayout viewContent = new VerticalLayout();
		viewContent.setId("furms-layout-view-content");
		viewContent.setAlignItems(FlexComponent.Alignment.STRETCH);
		viewContent.add(appLayoutComponents.getNavbar(), appLayoutComponents.getViewContainer());

		final HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setId("furms-layout-main");
		mainLayout.add(left, menuContent, viewContent, right);

		add(top, mainLayout, bottom);
	}

	private boolean isCurrentUserRoleListChanged(FurmsEvent furmsEvent) {
		if(!(furmsEvent instanceof UserEvent))
			return false;
		UserEvent event = (UserEvent) furmsEvent;
		return currentUserId.equals(event.getId());
	}

}
