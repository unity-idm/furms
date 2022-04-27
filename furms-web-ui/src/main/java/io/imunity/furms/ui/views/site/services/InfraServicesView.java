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
import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

import java.util.Collections;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Route(value = "site/admin/services", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.services.page.title")
public class InfraServicesView extends FurmsViewComponent {

	private final InfraServiceService infraServiceService;
	private final Grid<InfraServiceViewModel> grid;

	InfraServicesView(InfraServiceService infraServiceService) {
		this.infraServiceService = infraServiceService;
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
		addButton.addClickListener(x -> UI.getCurrent().navigate(InfraServiceFormView.class));
		return addButton;
	}

	private Grid<InfraServiceViewModel> createCommunityGrid() {
		Grid<InfraServiceViewModel> grid = new DenseGrid<>(InfraServiceViewModel.class);

		grid.addComponentColumn(item -> new RouterLink(item.getName(), InfraServiceFormView.class, item.getId().id.toString()))
			.setHeader(getTranslation("view.site-admin.service.grid.column.name"))
			.setSortable(true)
			.setComparator(item -> item.getName().toLowerCase());
		grid.addColumn(InfraServiceViewModel::getId)
			.setHeader(getTranslation("view.site-admin.service.grid.column.id"))
			.setSortable(true);
		grid.addColumn(InfraServiceViewModel::getDescription)
			.setHeader(getTranslation("view.site-admin.service.grid.column.description"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.site-admin.service.grid.column.actions"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(InfraServiceViewModel serviceViewModel) {
		return new GridActionsButtonLayout(
			createContextMenu(serviceViewModel.getId(), serviceViewModel.getName())
		);
	}

	private Component createContextMenu(InfraServiceId serviceId, String serviceName) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.service.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(InfraServiceFormView.class, serviceId.id.toString())
		);

		Dialog confirmDialog = createConfirmDialog(serviceId, serviceName);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.service.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(InfraServiceId serviceId, String serviceName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.site-admin.service.dialog.text", serviceName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			getResultOrException(() -> infraServiceService.delete(serviceId, new SiteId(getCurrentResourceId())))
				.getException()
				.ifPresent(throwable -> showErrorNotification(getTranslation(throwable.getMessage(), serviceName)));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadServicesViewsModels());
	}

	private List<InfraServiceViewModel> loadServicesViewsModels() {
		return handleExceptions(() -> infraServiceService.findAll(new SiteId(getCurrentResourceId())))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(InfraServiceViewModelMapper::map)
			.sorted(comparing(serviceViewModel -> serviceViewModel.getName().toLowerCase()))
			.collect(toList());
	}
}
