/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.services;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.services.ServiceService;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

import java.util.Collections;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Route(value = "site/admin/services", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.services.page.title")
public class ServicesView extends FurmsViewComponent {

	private final ServiceService serviceService;
	private final Grid<ServiceViewModel> grid;

	public ServicesView(ServiceService serviceService) {
		this.serviceService = serviceService;
		this.grid = createCommunityGrid();

		Button addButton = createAddButton();
		loadGridContent();

		getContent().add(createHeaderLayout(addButton), new HorizontalLayout(grid));
	}

	private HorizontalLayout createHeaderLayout(Button addButton) {
		return new ViewHeaderLayout(getTranslation("view.site-admin.service.header"), addButton);
	}

	private Button createAddButton() {
		Button addButton = new Button(getTranslation("view.site-admin.service.button.add"), PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(ServiceFormView.class));
		return addButton;
	}

	private Grid<ServiceViewModel> createCommunityGrid() {
		Grid<ServiceViewModel> grid = new SparseGrid<>(ServiceViewModel.class);

		grid.addComponentColumn(c -> new RouterLink(c.name, ServiceFormView.class, c.id))
			.setHeader(getTranslation("view.site-admin.service.grid.column.1"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(c -> c.description)
			.setHeader(getTranslation("view.site-admin.service.grid.column.2"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.site-admin.service.grid.column.3"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(ServiceViewModel serviceViewModel) {
		return new GridActionsButtonLayout(
			createContextMenu(serviceViewModel.id, serviceViewModel.name)
		);
	}

	private Component createContextMenu(String serviceId, String serviceName) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.service.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(ServiceFormView.class, serviceId)
		);

		Dialog confirmDialog = createConfirmDialog(serviceId, serviceName);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.service.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(String serviceId, String serviceName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.site-admin.service.dialog.text", serviceName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			handleExceptions(() -> serviceService.delete(serviceId));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadServicesViewsModels());
	}

	private List<ServiceViewModel> loadServicesViewsModels() {
		return handleExceptions(() -> serviceService.findAll(getCurrentResourceId()))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(ServiceViewModelMapper::map)
			.sorted(comparing(serviceViewModel -> serviceViewModel.name.toLowerCase()))
			.collect(toList());
	}
}
