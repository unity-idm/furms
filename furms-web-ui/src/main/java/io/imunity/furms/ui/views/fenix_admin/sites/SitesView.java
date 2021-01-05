/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.sites;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.menu.FenixAdminMenu;

import java.util.List;
import java.util.UUID;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.grid.ColumnTextAlign.END;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.GROUP;
import static com.vaadin.flow.component.icon.VaadinIcon.MENU;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE_O;
import static com.vaadin.flow.component.icon.VaadinIcon.TRAIN;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN;

@Route(value = "fenix/admin/sites", layout = FenixAdminMenu.class)
@PageTitle(key = "view.sites.page.title")
@CssImport("./styles/components/dropdown-menu.css")
public class SitesView extends FurmsViewComponent {

	private final static List<Site> sites = List.of(
			Site.builder().id(UUID.randomUUID().toString()).name("Name 1").build(),
			Site.builder().id(UUID.randomUUID().toString()).name("Name 2").build(),
			Site.builder().id(UUID.randomUUID().toString()).name("Name 3").build(),
			Site.builder().id(UUID.randomUUID().toString()).name("Name 4").build()
	);

	private final VerticalLayout layout;

	SitesView() {
		layout = new VerticalLayout();
		layout.setPadding(true);
		layout.setSpacing(true);

		addHeader();
		addTable();

		getContent().add(layout);
	}

	private void addHeader() {
		FlexLayout headerLayout = new FlexLayout();
		headerLayout.setWidthFull();

		H4 title = new H4("Sites");

		Button addButton = new Button("Add", new Icon(PLUS_CIRCLE_O));

		headerLayout.add(title, addButton);
		headerLayout.setJustifyContentMode(BETWEEN);

		layout.add(headerLayout);
	}

	private void addTable() {
		FlexLayout tableLayout = new FlexLayout();
		tableLayout.setWidthFull();

		Grid<Site> siteGrid = new Grid<>();
		siteGrid.setHeightByRows(true);
		siteGrid.addComponentColumn(site -> new RouterLink(site.getName(), SiteView.class, site.getId()))
				.setHeader("Name")
				.setKey("name");
		siteGrid.addComponentColumn(site -> addMenu(site, siteGrid))
				.setHeader("Actions")
				.setKey("actions")
				.setEditorComponent(new VerticalLayout(new Label("editable")))
				.setTextAlign(END);
		siteGrid.setItemDetailsRenderer(new ComponentRenderer<>(site -> {
			VerticalLayout layout = new VerticalLayout();
			layout.add(new Label(site.getId()));
			return layout;
		}));
		siteGrid.setDetailsVisibleOnClick(false);

		siteGrid.setItems(sites);

		tableLayout.add(siteGrid);

		layout.add(tableLayout);
	}

	private Component addMenu(Site site, Grid<Site> siteGrid) {
		Button button = new Button(MENU.create());
		button.addThemeVariants(LUMO_TERTIARY);
		button.setClassName("dropdown-menu");

		ContextMenu contextMenu = new ContextMenu();
		contextMenu.setId(site.getId());
		contextMenu.setOpenOnClick(true);
		contextMenu.setTarget(button);
		contextMenu.addItem(addMenuButton("Edit", EDIT), e -> testClick(e, siteGrid));
		contextMenu.addItem(addMenuButton("Delete", TRAIN), e -> {});
		contextMenu.addItem(addMenuButton("Administrators", GROUP), e -> {});

		layout.add(contextMenu);

		return button;
	}

	private void testClick(ClickEvent<MenuItem> e, Grid<Site> siteGrid) {
//		siteGrid.getEditor().getItem();
	}

	private Component addMenuButton(String edit, VaadinIcon icon) {
		Button button = new Button(edit, icon.create());
		button.addThemeVariants(LUMO_TERTIARY);
		return button;
	}
}
