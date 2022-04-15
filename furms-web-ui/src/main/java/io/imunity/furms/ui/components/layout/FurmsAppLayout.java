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
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.shared.Registration;
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
import java.util.Objects;

import static io.imunity.furms.ui.components.layout.FurmsAppLayoutComponentsFactory.MAIN_VIEW_CONTAINER_ID;
import static io.imunity.furms.ui.components.layout.FurmsAppLayoutComponentsFactory.MENU_CONTAINER_ID;
import static io.imunity.furms.ui.config.FurmsLayoutExtraPanelsConfig.BOTOOM_PANEL_ID;
import static io.imunity.furms.ui.config.FurmsLayoutExtraPanelsConfig.LEFT_PANEL_ID;
import static io.imunity.furms.ui.config.FurmsLayoutExtraPanelsConfig.RIGHT_PANEL_ID;
import static io.imunity.furms.ui.config.FurmsLayoutExtraPanelsConfig.TOP_PANEL_ID;

@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
@CssImport("./styles/custom-lumo-theme.css")
@CssImport(value = "./styles/components/furms-idle-notification.css")
@CssImport(value = "styles/components/furms-toggle-button.css", themeFor = "vaadin-checkbox")
@CssImport(value="./styles/components/menu-button-item.css", themeFor="vaadin-context-menu-item")
@PreserveOnRefresh
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
		else if(!Objects.equals(FurmsViewUserContext.getCurrent(), appLayoutComponents.getCurrent()))
			appLayoutComponents.reloadUserPicker();
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

		final Div top = new ExtraLayoutPanel(TOP_PANEL_ID, furmsLayoutExtraPanelsConfig.getTop());
		final Div left = new ExtraLayoutPanel(LEFT_PANEL_ID, furmsLayoutExtraPanelsConfig.getLeft());
		final Div right = new ExtraLayoutPanel(RIGHT_PANEL_ID, furmsLayoutExtraPanelsConfig.getRight());
		final Div bottom = new ExtraLayoutPanel(BOTOOM_PANEL_ID, furmsLayoutExtraPanelsConfig.getBottom());

		final VerticalLayout menuContent = appLayoutComponents.getLogoMenuContainer();
		menuContent.setId(MENU_CONTAINER_ID);
		menuContent.getStyle().set("width", null);

		final VerticalLayout viewContent = new VerticalLayout();
		viewContent.setId(MAIN_VIEW_CONTAINER_ID);
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
