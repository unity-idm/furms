/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.sites;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.grid.editor.EditorOpenEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;
import io.imunity.furms.ui.views.fenix.sites.add.SitesAddView;
import io.imunity.furms.ui.views.fenix.sites.admins.SitesAdminsView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.grid.ColumnTextAlign.END;
import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.domain.constant.RoutesConst.FENIX_ADMIN_LANDING_PAGE;
import static io.imunity.furms.ui.utils.FormSettings.NAME_MAX_LENGTH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;
import static java.util.stream.Collectors.toList;

@Route(value = FENIX_ADMIN_LANDING_PAGE, layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.sites.page.title")
public class SitesView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteService siteService;

	private SiteGridItem bufferedSiteGridItem;

	SitesView(SiteService siteService) {
		this.siteService = siteService;

		addHeader();
		addTable();

		addPreventionForMultiEnterClick();
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

		siteEditor.addOpenListener(event -> onEditorOpen(event, siteBinder));
		siteEditor.addCloseListener(event -> onEditorClose(siteBinder));

		siteGrid.addComponentColumn(site -> new RouterLink(site.getName(), SitesAdminsView.class, site.getId()))
				.setHeader(getTranslation("view.sites.main.grid.column.name"))
				.setKey("name")
				.setSortable(true)
				.setComparator(SiteGridItem::getName)
				.setEditorComponent(addEditForm(siteEditor));

		siteGrid.addComponentColumn(site -> createLastColumnContent(site, siteGrid))
				.setHeader(getTranslation("view.sites.main.grid.column.actions"))
				.setKey("actions")
				.setEditorComponent(addEditButtons(siteEditor))
				.setTextAlign(END);

		tableLayout.add(siteGrid);

		getContent().add(tableLayout);
	}
	
	private Component createLastColumnContent(SiteGridItem site, Grid<SiteGridItem> siteGrid) {
		return new GridActionsButtonLayout(
				new RouterGridLink(USERS, site.getId(), SitesAdminsView.class),
				createContextMenu(site, siteGrid)
			);
	}

	private Component createContextMenu(SiteGridItem site, Grid<SiteGridItem> siteGrid) {
		GridActionMenu contextMenu = new GridActionMenu();
		contextMenu.setId(site.getId());
		contextMenu.addItem(new MenuButton(getTranslation("view.sites.main.grid.item.menu.edit"), EDIT),
				e -> actionEditSite(site, siteGrid));
		contextMenu.addItem(new MenuButton(getTranslation("view.sites.main.grid.item.menu.delete"), TRASH),
				e -> actionDeleteSite(site, siteGrid));
		contextMenu.addItem(new MenuButton(getTranslation("view.sites.main.grid.item.menu.administrators"), USERS),
				e -> actionOpenAdministrators(site));

		getContent().add(contextMenu);

		return contextMenu.getTarget();
	}

	private Component addEditForm(Editor<SiteGridItem> siteEditor) {
		TextField siteNameField = new TextField();
		siteNameField.setMaxLength(NAME_MAX_LENGTH);
		siteNameField.setWidthFull();
		siteNameField.setValueChangeMode(EAGER);
		siteEditor.getBinder().forField(siteNameField)
				.withValidator(getNotEmptyStringValidator(), getTranslation("view.sites.form.error.validation.field.name.required"))
				.withValidator(siteName -> !siteService.isNamePresentIgnoringRecord(siteName, siteEditor.getItem().getId()),
							getTranslation("view.sites.form.error.validation.field.name.unique"))
				.bind(SiteGridItem::getName, SiteGridItem::setName);

		return new Div(siteNameField);
	}

	private Component addEditButtons(Editor<SiteGridItem> siteEditor) {
		Button save = new Button(getTranslation("view.sites.main.grid.editor.button.save"),
				e -> updateAction(siteEditor));
		save.addThemeVariants(LUMO_TERTIARY);
		save.addClassName("save");
		save.addClickShortcut(Key.ENTER);

		Button cancel = new Button(getTranslation("view.sites.main.grid.editor.button.cancel"),
				e -> cancelAction(siteEditor));
		cancel.addThemeVariants(LUMO_TERTIARY);
		cancel.addClassName("cancel");

		siteEditor.getBinder().addStatusChangeListener(status -> save.setEnabled(!status.hasValidationErrors() && isNameChanged(siteEditor)));
		siteEditor.addOpenListener(e -> save.setEnabled(false));

		return new Div(save, cancel);
	}
	
	private void cancelAction(Editor<SiteGridItem> siteEditor) {
		siteEditor.cancel();
		refreshGrid(siteEditor);
	}

	private void actionOpenSiteFormAdd(ClickEvent<Button> buttonClickEvent) {
		UI.getCurrent().navigate(SitesAddView.class);
	}

	private void actionOpenAdministrators(SiteGridItem site) {
		UI.getCurrent().navigate(SitesAdminsView.class, site.getId());
	}
	
	private void refreshGrid(Editor<SiteGridItem> siteEditor) {
		siteEditor.getGrid().setItems(fetchSites());
	}

	private void updateAction(Editor<SiteGridItem> siteEditor) {
		if (siteEditor.getBinder().isValid()) {
			Optional<Component> component = siteEditor.getGrid().getColumnByKey("name")
					.getEditorComponent().getChildren()
					.filter(c -> c instanceof TextField)
					.findFirst();
			if (component.isPresent()) {
				TextField name = component.map(c -> (TextField) c).get();
				try {
					siteService.update(Site.builder()
							.id(siteEditor.getItem().getId())
							.name(name.getValue())
							.build());
					siteEditor.cancel();
					refreshGrid(siteEditor);
					showSuccessNotification(getTranslation("view.sites.form.save.success"));
					reloadRolePicker();
				} catch (DuplicatedNameValidationError e) {
					name.setErrorMessage(getTranslation("view.sites.form.error.validation.field.name.unique"));
					name.setInvalid(true);
				} catch (RuntimeException e) {
					LOG.error("Could not update Site.", e);
					showErrorNotification(getTranslation("view.sites.form.error.unexpected", "update"));
				}
			}
		}
	}

	private void actionEditSite(SiteGridItem site, Grid<SiteGridItem> siteGrid) {
		siteGrid.getEditor().editItem(site);
	}

	private void actionDeleteSite(SiteGridItem site, Grid<SiteGridItem> siteGrid) {
		FurmsDialog cancelDialog = new FurmsDialog(getTranslation("view.sites.main.confirmation.dialog.delete", site.getName()));
		cancelDialog.addConfirmButtonClickListener(event -> {
			try {
				siteService.delete(site.getId());
				showSuccessNotification(getTranslation("view.sites.main.grid.item.menu.delete.success", site.getName()));
			} catch (RuntimeException e) {
				LOG.error("Could not create Site. ", e);
				showErrorNotification(getTranslation("view.sites.form.error.unexpected", "delete"));
			} finally {
				siteGrid.setItems(fetchSites());
				UI.getCurrent().getSession().getAttribute(FurmsSelectReloader.class).reload();
			}
		});
		cancelDialog.open();
	}

	private List<SiteGridItem> fetchSites() {
		return siteService.findAll().stream()
				.map(SiteGridItem::of)
				.sorted(Comparator.comparing(SiteGridItem::getName))
				.collect(toList());
	}

	private void onEditorOpen(EditorOpenEvent<SiteGridItem> event, Binder<SiteGridItem> siteBinder) {
		bufferedSiteGridItem = event.getItem().clone();
		siteBinder.setBean(event.getItem());
	}


	private void onEditorClose(Binder<SiteGridItem> siteBinder) {
		bufferedSiteGridItem = null;
		siteBinder.setBean(null);
	}

	private boolean isNameChanged(Editor<SiteGridItem> siteEditor) {
		return bufferedSiteGridItem != null && !Objects.equals(bufferedSiteGridItem.getName(), siteEditor.getItem().getName());
	}

}
