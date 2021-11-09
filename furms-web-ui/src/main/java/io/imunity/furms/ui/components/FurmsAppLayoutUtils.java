/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;

import io.imunity.furms.ui.components.branding.logo.FurmsLogo;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.view_picker.FurmsRolePicker;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

public class FurmsAppLayoutUtils {
	private final List<MenuComponent> menuContent;
	private final BreadCrumbComponent breadCrumbComponent;
	private final Tabs menu;
	private final FurmsRolePicker furmsSelect;
	private final Component notificationBar;

	private final VerticalLayout drawer;

	private final Supplier<FurmsLogo> furmsLogoLoader;

	public FurmsAppLayoutUtils(List<MenuComponent> menuContent,
	                           FurmsRolePicker furmsSelect,
	                           Component notificationBar,
	                           Supplier<FurmsLogo> furmsLogoLoader){
		this.menuContent = menuContent;
		this.breadCrumbComponent = new BreadCrumbComponent(menuContent);
		this.menu = createMenu();
		this.furmsSelect = furmsSelect;
		this.notificationBar = notificationBar;
		this.furmsLogoLoader = furmsLogoLoader;
		this.drawer = new VerticalLayout();
	}

	void reloadUserPicker() {
		furmsSelect.reloadComponent();
	}

	FurmsViewUserContext getUserPickerValue() {
		return furmsSelect.getCurrentValue();
	}

	public VerticalLayout createDrawerContent() {
		drawer.setPadding(false);
		drawer.setSpacing(false);
		drawer.setAlignItems(FlexComponent.Alignment.STRETCH);
		drawer.add(menu);
		return drawer;
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

		rightNavbarSite.add(new Text(getTranslation("navbar.text")), furmsSelect, notificationBar, logout);
		return rightNavbarSite;
	}

	public void afterNavigation(Component content){
		getTabForComponent(content).ifPresent(menu::setSelectedTab);
		breadCrumbComponent.update((FurmsViewComponent) content);
		loadLogo();
	}

	public static void callReloadLogo(final Class source) {
		UI.getCurrent().navigate(source);
	}

	private void loadLogo() {
		final FurmsLogo currentFurmsLogo = furmsLogoLoader.get();
		drawer.getChildren()
				.filter(component -> component instanceof FurmsLogo)
				.findFirst()
				.ifPresentOrElse(
						logo -> replaceLogoInDrawer(logo, currentFurmsLogo),
						() -> drawer.addComponentAsFirst(currentFurmsLogo));
	}

	private void replaceLogoInDrawer(Component logo, FurmsLogo currentFurmsLogo) {
		if (!((FurmsLogo)logo).equalsLogo(currentFurmsLogo)) {
			drawer.replace(logo, currentFurmsLogo);
		}
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
