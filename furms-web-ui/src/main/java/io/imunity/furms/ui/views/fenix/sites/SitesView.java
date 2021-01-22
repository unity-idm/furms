/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.sites;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.grid.ColumnTextAlign.END;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.GROUP;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.domain.constant.RoutesConst.FENIX_ADMIN_LANDING_PAGE;
import static io.imunity.furms.ui.utils.MenuComponentFactory.createActionButton;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;
import io.imunity.furms.ui.views.fenix.sites.data.SiteGridItem;

@Route(value = FENIX_ADMIN_LANDING_PAGE, layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.sites.page.title")
public class SitesView extends FurmsViewComponent {

	private final SiteService siteService;

	SitesView(SiteService siteService) {
		this.siteService = siteService;

		addHeader();
		addTable();
	}

	private void addHeader() {
		Button addButton = new Button(getTranslation("view.sites.main.add.button"), new Icon(PLUS_CIRCLE));
		addButton.addClickListener(this::actionOpenSiteFormAdd);

		getContent().add(new ViewHeaderLayout(getTranslation("view.sites.main.title"), addButton));
	}

	private void addTable() {
		FlexLayout tableLayout = new FlexLayout();
		tableLayout.setWidthFull();

		List<SiteGridItem> sites = fetchSites();

		SparseGrid<SiteGridItem> siteGrid = new SparseGrid<>(SiteGridItem.class);
		siteGrid.setItems(sites);

		Binder<SiteGridItem> siteBinder = new Binder<>(SiteGridItem.class);
		Editor<SiteGridItem> siteEditor = siteGrid.getEditor();
		siteEditor.setBinder(siteBinder);
		siteEditor.setBuffered(true);

		siteGrid.addComponentColumn(site -> new RouterLink(site.getName(), SitesDetailsView.class, site.getId()))
				.setHeader(getTranslation("view.sites.main.grid.column.name"))
				.setKey("name")
				.setEditorComponent(addEditForm(siteBinder));

		siteGrid.addComponentColumn(site -> addMenu(site, siteGrid))
				.setHeader(getTranslation("view.sites.main.grid.column.actions"))
				.setKey("actions")
				.setEditorComponent(addEditButtons(siteEditor))
				.setTextAlign(END);

		tableLayout.add(siteGrid);

		getContent().add(tableLayout);
	}

	private Component addMenu(SiteGridItem site, Grid<SiteGridItem> siteGrid) {
		GridActionMenu contextMenu = new GridActionMenu();
		contextMenu.setId(site.getId());
		contextMenu.addItem(createActionButton(getTranslation("view.sites.main.grid.item.menu.edit"), EDIT),
				e -> actionEditSite(site, siteGrid));
		contextMenu.addItem(createActionButton(getTranslation("view.sites.main.grid.item.menu.delete"), TRASH),
				e -> actionDeleteSite(site, siteGrid));
		contextMenu.addItem(createActionButton(getTranslation("view.sites.main.grid.item.menu.administrators"), GROUP),
				e -> actionOpenAdministrators(site));

		getContent().add(contextMenu);

		return contextMenu.getTarget();
	}

	private Component addEditForm(Binder<SiteGridItem> siteBinder) {
		TextField siteNameField = new TextField();
		siteNameField.setValueChangeMode(EAGER);
		siteBinder.forField(siteNameField)
				.withValidator(getNotEmptyStringValidator(), getTranslation("view.sites.form.error.validation.field.name.required"))
				.withValidator(siteService::isNameUnique, getTranslation("view.sites.form.error.validation.field.name.unique"))
				.bind(SiteGridItem::getName, SiteGridItem::setName);

		return new Div(siteNameField);
	}

	private Component addEditButtons(Editor<SiteGridItem> siteEditor) {
		Button save = new Button(getTranslation("view.sites.main.grid.editor.button.save"), e -> actionUpdate(siteEditor));
		save.addThemeVariants(LUMO_TERTIARY);
		save.addClassName("save");
		save.addClickShortcut(Key.ENTER);

		Button cancel = new Button(getTranslation("view.sites.main.grid.editor.button.cancel"), e -> siteEditor.cancel());
		cancel.addThemeVariants(LUMO_TERTIARY);
		cancel.addClassName("cancel");

		siteEditor.getBinder().addStatusChangeListener(status -> save.setEnabled(!status.hasValidationErrors()));
		siteEditor.addOpenListener(e -> save.setEnabled(false));

		return new Div(save, cancel);
	}

	private void actionOpenSiteFormAdd(ClickEvent<Button> buttonClickEvent) {
		UI.getCurrent().navigate(SitesAddView.class);
	}

	private void actionOpenAdministrators(SiteGridItem site) {
		UI.getCurrent().navigate(SitesDetailsView.class, site.getId());
	}

	private void actionUpdate(Editor<SiteGridItem> siteEditor) {
		if (siteEditor.getBinder().isValid()) {
			Optional<Component> component = siteEditor.getGrid().getColumnByKey("name")
					.getEditorComponent().getChildren()
					.filter(c -> c instanceof TextField)
					.findFirst();
			if (component.isPresent()) {
				TextField name = component.map(c -> (TextField) c).get();
				try {
					if (siteEditor.getItem().getName().equals(name.getValue())) {
						throw new IllegalArgumentException(getTranslation("view.sites.form.error.validation.field.name.different"));
					}
					siteService.update(Site.builder()
							.id(siteEditor.getItem().getId())
							.name(name.getValue())
							.build());
					siteEditor.cancel();
					siteEditor.getGrid().setItems(fetchSites());
				} catch (IllegalArgumentException e) {
					name.setErrorMessage(e.getMessage());
					name.setInvalid(true);
				} catch (RuntimeException e) {
					showErrorNotification(getTranslation("view.sites.form.error.unexpected", "update"));
				}
			}
		}
	}

	private void actionEditSite(SiteGridItem site, Grid<SiteGridItem> siteGrid) {
		siteGrid.getEditor().editItem(site);
	}

	private void actionDeleteSite(SiteGridItem site, Grid<SiteGridItem> siteGrid) {
		try {
			siteService.delete(site.getId());
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("view.sites.form.error.unexpected", "delete"));
		} finally {
			siteGrid.setItems(fetchSites());
		}
	}

	private List<SiteGridItem> fetchSites() {
		return siteService.findAll().stream()
				.map(SiteGridItem::of)
				.collect(toList());
	}
}
