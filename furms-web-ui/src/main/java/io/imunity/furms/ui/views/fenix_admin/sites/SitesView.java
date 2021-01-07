/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.sites;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.menu.FenixAdminMenu;
import io.imunity.furms.ui.views.fenix_admin.sites.data.SiteDataGrid;

import java.util.List;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.grid.ColumnTextAlign.END;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.GROUP;
import static com.vaadin.flow.component.icon.VaadinIcon.MENU;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE_O;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN;
import static java.util.stream.Collectors.toList;

@Route(value = "fenix/admin/sites", layout = FenixAdminMenu.class)
@PageTitle(key = "view.sites.page.title")
@CssImport("./styles/components/dropdown-menu.css")
public class SitesView extends FurmsViewComponent {

	private final SiteService siteService;

	private final VerticalLayout layout;

	SitesView(SiteService siteService) {
		this.siteService = siteService;

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
		addButton.addClickListener(e -> UI.getCurrent().navigate(SitesAddView.class));

		headerLayout.add(title, addButton);
		headerLayout.setJustifyContentMode(BETWEEN);

		layout.add(headerLayout);
	}

	private void addTable() {
		FlexLayout tableLayout = new FlexLayout();
		tableLayout.setWidthFull();

		List<SiteDataGrid> sites = fetchSites();

		Grid<SiteDataGrid> siteGrid = new Grid<>();
		siteGrid.setHeightByRows(true);
		siteGrid.setItems(sites);

		Binder<SiteDataGrid> siteBinder = new Binder<>(SiteDataGrid.class);
		Editor<SiteDataGrid> siteEditor = siteGrid.getEditor();
		siteEditor.setBinder(siteBinder);
		siteEditor.setBuffered(true);

		TextField siteNameField = new TextField();
		siteBinder.forField(siteNameField)
				.bind(SiteDataGrid::getName, SiteDataGrid::setName);

		siteGrid.addComponentColumn(site -> new RouterLink(site.getName(), SitesDetailsView.class, site.getId()))
				.setHeader("Name")
				.setEditorComponent(siteNameField);

		Button save = new Button("Save", e -> {
			try {
				siteEditor.save();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		});
		save.addThemeVariants(LUMO_TERTIARY);
		save.addClassName("save");

		Button cancel = new Button("Cancel", e -> {
			try {
				siteEditor.cancel();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		});
		cancel.addThemeVariants(LUMO_TERTIARY);
		cancel.addClassName("cancel");

		siteGrid.addComponentColumn(site -> addMenu(site, siteGrid))
				.setHeader("Actions")
				.setEditorComponent(new Div(save, cancel))
				.setTextAlign(END);


		tableLayout.add(siteGrid);

		layout.add(tableLayout);
	}

	private Component addMenu(SiteDataGrid site, Grid<SiteDataGrid> siteGrid) {
		Button button = new Button(MENU.create());
		button.addThemeVariants(LUMO_TERTIARY);
		button.setClassName("dropdown-menu");

		ContextMenu contextMenu = new ContextMenu();
		contextMenu.setId(site.getId());
		contextMenu.setOpenOnClick(true);
		contextMenu.setTarget(button);
		contextMenu.addItem(addMenuButton("Edit", EDIT), e -> actionEditSite(site, siteGrid));
		contextMenu.addItem(addMenuButton("Delete", TRASH), e -> actionDeleteSite(site, siteGrid));
		contextMenu.addItem(addMenuButton("Administrators", GROUP), e -> actionOpenAdministrators(site));

		layout.add(contextMenu);

		return button;
	}

	private Button addMenuButton(String label, VaadinIcon icon) {
		Button button = new Button(label, icon.create());
		button.addThemeVariants(LUMO_TERTIARY);
		return button;
	}

	private void actionOpenAdministrators(SiteDataGrid site) {
		UI.getCurrent().navigate(SitesDetailsView.class, site.getId());
	}

	private void actionEditSite(SiteDataGrid site, Grid<SiteDataGrid> siteGrid) {
		siteGrid.getEditor().editItem(site);
	}

	private void actionDeleteSite(SiteDataGrid site, Grid<SiteDataGrid> siteGrid) {
		siteService.delete(site.getId());
		siteGrid.setItems(fetchSites());
	}

	private List<SiteDataGrid> fetchSites() {
		return siteService.findAll().stream()
				.map(SiteDataGrid::of)
				.collect(toList());
	}
}
