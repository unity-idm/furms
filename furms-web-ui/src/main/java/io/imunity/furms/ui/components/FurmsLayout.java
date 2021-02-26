/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import io.imunity.furms.ui.FurmsSelectFactory;

import java.util.List;
import java.util.Optional;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

public class FurmsLayout {
	private final List<MenuComponent> menuContent;
	private final BreadCrumbComponent breadCrumbComponent;
	private final Tabs menu;
	private final FurmsSelectFactory furmsSelectFactory;

	public FurmsLayout(List<MenuComponent> menuContent, FurmsSelectFactory furmsSelectFactory){
		this.menuContent = menuContent;
		this.breadCrumbComponent = new BreadCrumbComponent(menuContent);
		this.menu = createMenu();
		this.furmsSelectFactory = furmsSelectFactory;
	}

	public Component createDrawerContent() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(false);
		layout.setSpacing(false);
		layout.setAlignItems(FlexComponent.Alignment.STRETCH);
		layout.add(getLogo(), menu);
		return layout;
	}
	
	private Component getLogo() {
		HorizontalLayout logoLayout = new HorizontalLayout();
		logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		logoLayout.setMargin(true);
		Image image = new Image(Images.FENIX_LOGO.path, "");
		image.setWidthFull();
		logoLayout.add(image);
		return logoLayout;
	}

	public Component createNavbar(){
		HorizontalLayout rightNavbarSite = createRightNavbarSite();

		HorizontalLayout navbarLayout = new HorizontalLayout(breadCrumbComponent, rightNavbarSite);
		navbarLayout.setId("header");
		return navbarLayout;
	}

	private HorizontalLayout createRightNavbarSite() {
		Icon logout = LogoutIconFactory.create();

		HorizontalLayout rightNavbarSite = new HorizontalLayout();
		rightNavbarSite.setAlignItems(FlexComponent.Alignment.CENTER);
		rightNavbarSite.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		rightNavbarSite.setSizeFull();

		rightNavbarSite.add(new Text(getTranslation("navbar.text")), furmsSelectFactory.create(), logout);
		return rightNavbarSite;
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
			.map(c -> new TabComponent(getPageTitle(c.component), c))
			.toArray(Tab[]::new);
		tabs.add(components);
		return tabs;
	}

	private Optional<TabComponent> getTabForComponent(Component component) {
		return menu.getChildren()
			.map(TabComponent.class::cast)
			.filter(tab -> tab.componentClass.contains(component.getClass()))
			.findFirst();
	}

	static String getPageTitle(Class<? extends Component> componentClass) {
		String key = componentClass.getAnnotation(PageTitle.class).key();
		return getTranslation(key);
	}
}
