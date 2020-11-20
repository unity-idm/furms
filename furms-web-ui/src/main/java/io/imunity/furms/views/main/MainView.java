/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.views.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import io.imunity.furms.views.about.AboutView;
import io.imunity.furms.views.helloworld.HelloWorldView;
import org.springframework.core.env.Environment;

import java.util.Optional;

import static io.imunity.furms.constant.LoginFlowConst.LOGOUT_URL;

/**
 * The main view is a top-level placeholder for other views.
 */
@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
@PWA(name = "Furms", shortName = "Furms", enableInstallPrompt = false)
public class MainView extends AppLayout
{
	private final Tabs menu;
	private final Environment env;
	private H1 viewTitle;

	MainView(Environment env)
	{
		this.env = env;
		setPrimarySection(Section.DRAWER);
		addToNavbar(true, createHeaderContent());
		menu = createMenu();
		addToDrawer(createDrawerContent(menu));
	}

	private Component createHeaderContent()
	{
		HorizontalLayout layout = new HorizontalLayout();
		layout.setId("header");
		layout.getThemeList().set("dark", true);
		layout.setWidthFull();
		layout.setSpacing(false);
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		layout.add(new DrawerToggle());
		viewTitle = new H1();
		layout.add(viewTitle);
		HorizontalLayout logoutLayout = new HorizontalLayout();
		logoutLayout.setSizeFull();
		logoutLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		logoutLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		logoutLayout.add(new Anchor(LOGOUT_URL, env.getProperty("${placeholder.logout}")));
		layout.add(logoutLayout);
		return layout;
	}

	private Component createDrawerContent(Tabs menu)
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(false);
		layout.setSpacing(false);
		layout.getThemeList().set("spacing-s", true);
		layout.setAlignItems(FlexComponent.Alignment.STRETCH);
		HorizontalLayout logoLayout = new HorizontalLayout();
		logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		logoLayout.add(new H1(env.getProperty("${placeholder.app-name}")));
		layout.add(logoLayout, menu);
		return layout;
	}

	private Tabs createMenu()
	{
		Tabs tabs = new Tabs();
		tabs.setOrientation(Tabs.Orientation.VERTICAL);
		tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
		tabs.setId("tabs");
		tabs.add(createMenuItems());
		return tabs;
	}

	private Component[] createMenuItems()
	{
		return new Tab[]{
				createTab(env.getProperty("${placeholder.tab.1}"), HelloWorldView.class),
				createTab(env.getProperty("${placeholder.tab.2}"), AboutView.class)
		};
	}

	private static Tab createTab(String text, Class<? extends Component> navigationTarget)
	{
		final Tab tab = new Tab();
		tab.add(new RouterLink(text, navigationTarget));
		ComponentUtil.setData(tab, Class.class, navigationTarget);
		return tab;
	}

	@Override
	protected void afterNavigation()
	{
		super.afterNavigation();
		getTabForComponent(getContent()).ifPresent(menu::setSelectedTab);
		viewTitle.setText(getCurrentPageTitle());
	}

	private Optional<Tab> getTabForComponent(Component component)
	{
		return menu.getChildren()
				.filter(tab -> ComponentUtil.getData(tab, Class.class)
						.equals(component.getClass()))
				.findFirst().map(Tab.class::cast);
	}

	private String getCurrentPageTitle()
	{
		return getContent().getClass().getAnnotation(PageTitle.class).value();
	}
}
