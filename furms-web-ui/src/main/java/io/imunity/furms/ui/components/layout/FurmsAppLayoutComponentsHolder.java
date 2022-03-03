/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.dom.Element;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.LogoutIconFactory;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.components.TabComponent;
import io.imunity.furms.ui.components.branding.logo.FurmsLogo;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.view_picker.FurmsRolePicker;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static io.imunity.furms.ui.components.layout.FurmsAppLayoutUtils.getPageTitle;
import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

class FurmsAppLayoutComponentsHolder {
	private final BreadCrumbComponent breadCrumbComponent;
	private final FurmsRolePicker furmsSelect;
	private final HorizontalLayout navbar;

	private final VerticalLayout logoMenuContainer;
	private final Supplier<FurmsLogo> logoLoader;
	private final Tabs menuTabs;

	private final Div viewContainer;
	private Component viewContent;

	FurmsAppLayoutComponentsHolder(List<MenuComponent> menuContent,
	                                      FurmsRolePicker furmsSelect,
	                                      Component notificationBar,
	                                      Supplier<FurmsLogo> logoLoader) {
		this.breadCrumbComponent = new BreadCrumbComponent(menuContent);
		this.furmsSelect = furmsSelect;
		this.navbar = createNavbar(breadCrumbComponent, notificationBar, furmsSelect);

		this.logoLoader = logoLoader;
		this.menuTabs = createMenuTabs(menuContent);
		this.logoMenuContainer = createLogoMenuContainer(menuTabs);
		this.viewContainer = new Div();
	}

	VerticalLayout getLogoMenuContainer() {
		return logoMenuContainer;
	}

	HorizontalLayout getNavbar() {
		return navbar;
	}

	Div getViewContainer() {
		return viewContainer;
	}

	void setViewContent(HasElement content) {
		if (content != null) {
			final Element contentElement = content.getElement();
			viewContent = contentElement.getComponent()
					.orElseThrow(() -> new IllegalArgumentException(
							"AppLayout content must be a Component"));
			viewContainer.getElement().appendChild(contentElement);
		}
	}

	void reloadUserPicker() {
		furmsSelect.reloadComponent();
	}

	FurmsViewUserContext getCurrent() {
		return furmsSelect.getCurrent();
	}

	void reloadMenuAndBreadCrumb() {
		findTabForComponent(viewContent).ifPresent(menuTabs::setSelectedTab);
		breadCrumbComponent.update((FurmsViewComponent) viewContent);
		loadLogo();
	}

	private Optional<TabComponent> findTabForComponent(Component component) {
		return menuTabs.getChildren()
				.map(TabComponent.class::cast)
				.filter(tab -> tab.componentClass.contains(component.getClass()))
				.findFirst();
	}

	private void loadLogo() {
		final FurmsLogo currentFurmsLogo = logoLoader.get();
		logoMenuContainer.getChildren()
				.filter(component -> component instanceof FurmsLogo)
				.findFirst()
				.ifPresentOrElse(
						logo -> replaceLogo(logo, currentFurmsLogo),
						() -> logoMenuContainer.addComponentAsFirst(currentFurmsLogo));
	}

	private void replaceLogo(Component logo, FurmsLogo currentFurmsLogo) {
		if (!((FurmsLogo) logo).equalsLogo(currentFurmsLogo)) {
			logoMenuContainer.replace(logo, currentFurmsLogo);
		}
	}

	private HorizontalLayout createNavbar(BreadCrumbComponent breadCrumb,
	                                      Component notificationBar,
	                                      FurmsRolePicker furmsRolePicker) {
		final HorizontalLayout navbarComponent = new HorizontalLayout(
				breadCrumb,
				createRightNavbarSite(notificationBar, furmsRolePicker));
		navbarComponent.setId("header");
		return navbarComponent;
	}

	private HorizontalLayout createRightNavbarSite(Component notificationBar, FurmsRolePicker furmsRolePicker) {
		final Icon logout = LogoutIconFactory.create();

		final HorizontalLayout rightNavbarSite = new HorizontalLayout();
		rightNavbarSite.setAlignItems(FlexComponent.Alignment.CENTER);
		rightNavbarSite.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		rightNavbarSite.setSizeFull();

		final Text navbarTitle = new Text(getTranslation("navbar.text"));

		rightNavbarSite.add(navbarTitle, furmsRolePicker, notificationBar, logout);
		return rightNavbarSite;
	}

	private VerticalLayout createLogoMenuContainer(Tabs menuTabsComponent) {
		final VerticalLayout logoMenuLayout = new VerticalLayout();
		logoMenuLayout.setPadding(false);
		logoMenuLayout.setSpacing(false);
		logoMenuLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
		logoMenuLayout.add(menuTabsComponent);
		return logoMenuLayout;
	}

	private Tabs createMenuTabs(List<MenuComponent> menuContent) {
		final Tabs tabs = new Tabs();
		tabs.setOrientation(Tabs.Orientation.VERTICAL);
		tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
		tabs.setId("tabs");
		final Component[] items = menuContent.stream()
				.map(c -> new TabComponent(getPageTitle(c.component), c))
				.toArray(Tab[]::new);
		tabs.add(items);
		return tabs;
	}

}
