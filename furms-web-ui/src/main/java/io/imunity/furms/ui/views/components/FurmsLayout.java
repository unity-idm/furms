/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.server.VaadinService;

import java.util.List;
import java.util.Optional;

public class FurmsLayout {
	private final List<Class<? extends Component>> menuContent;
	private final BreadCrumbComponent breadCrumbComponent;
	private final Tabs menu;

	public FurmsLayout(List<Class<? extends Component>> menuContent){
		this.menuContent = menuContent;
		this.breadCrumbComponent = new BreadCrumbComponent(menuContent);
		this.menu = createMenu();
	}

	public Component createDrawerContent() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(false);
		layout.setSpacing(false);
		layout.getThemeList().set("spacing-s", true);
		layout.setAlignItems(FlexComponent.Alignment.STRETCH);
		HorizontalLayout logoLayout = new HorizontalLayout();
		logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		logoLayout.add(new H1("Furms"));
		layout.add(logoLayout, menu);
		return layout;
	}

	public Component createNavbar(){
		breadCrumbComponent.getContent().setSizeFull();
		Anchor logout = new Anchor("/logout", getTranslation("view.main-page.logout"));
		HorizontalLayout logoutLayout = new HorizontalLayout();
		logoutLayout.setAlignItems(FlexComponent.Alignment.END);
		logoutLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		logoutLayout.setSizeFull();
		logoutLayout.add(logout);
		HorizontalLayout navbarLayout = new HorizontalLayout(breadCrumbComponent, logoutLayout);
		navbarLayout.setSizeFull();
		return navbarLayout;
	}

	public void afterNavigation(Component content){
		getTabForComponent(content).ifPresent(menu::setSelectedTab);
		breadCrumbComponent.update((FurmsViewComponent) content);
	}

	private Tabs createMenu() {
		final Tabs tabs = new Tabs();
		tabs.setOrientation(Tabs.Orientation.VERTICAL);
		tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
		tabs.setId("tabs");
		Component[] components = menuContent.stream()
			.map(c -> new TabComponent(getPageTitle(c), c))
			.toArray(Tab[]::new);
		tabs.add(components);
		return tabs;
	}

	private Optional<TabComponent> getTabForComponent(Component component) {
		return menu.getChildren()
			.map(TabComponent.class::cast)
			.filter(tab -> tab.componentClass.equals(component.getClass()))
			.findFirst();
	}

	static String getPageTitle(Class<? extends Component> componentClass) {
		String key = componentClass.getAnnotation(PageTitle.class).key();
		return getTranslation(key);
	}

	private static String getTranslation(String key) {
		return VaadinService.getCurrent()
			.getInstantiator()
			.getI18NProvider()
			.getTranslation(key, UI.getCurrent().getLocale());
	}
}
