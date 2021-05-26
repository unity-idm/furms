/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_types;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.components.support.GridUtils.getsLeadingPartOfUUID;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

@Route(value = "site/admin/resource/types", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.resource-types.page.title")
public class ResourceTypesView extends FurmsViewComponent {

	private final ResourceTypeService resourceTypeService;
	private final Grid<ResourceTypeViewModel> grid;
	private final ServiceComboBoxModelResolver resolver;

	public ResourceTypesView(ResourceTypeService resourceTypeService, InfraServiceService serviceService) {
		this.resourceTypeService = resourceTypeService;
		this.grid = createResourceTypesGrid();
		this.resolver = new ServiceComboBoxModelResolver(serviceService.findAll(getCurrentResourceId()));

		Button addButton = createAddButton();
		loadGridContent();

		getContent().add(createHeaderLayout(addButton), new HorizontalLayout(grid));
	}

	private HorizontalLayout createHeaderLayout(Button addButton) {
		return new ViewHeaderLayout(getTranslation("view.site-admin.resource-types.header"), addButton);
	}

	private Button createAddButton() {
		Button addButton = new Button(getTranslation("view.site-admin.resource-types.button.add"), PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(ResourceTypeFormView.class));
		return addButton;
	}

	private Grid<ResourceTypeViewModel> createResourceTypesGrid() {
		Grid<ResourceTypeViewModel> grid = new SparseGrid<>(ResourceTypeViewModel.class);

		grid.addComponentColumn(c -> new RouterLink(c.getName(), ResourceTypeFormView.class, c.getId()))
			.setHeader(getTranslation("view.site-admin.resource-types.grid.column.name"))
			.setSortable(true)
			.setComparator(x -> x.getName().toLowerCase());
		grid.addColumn(c -> getsLeadingPartOfUUID(c.getId()))
			.setHeader(getTranslation("view.site-admin.resource-types.grid.column.id"))
			.setSortable(true);
		grid.addColumn(c -> resolver.getName(c.getServiceId()))
			.setHeader(getTranslation("view.site-admin.resource-types.grid.column.service"))
			.setSortable(true);
		grid.addColumn(c -> getTranslation("enum.ResourceMeasureType." + c.getType().name()))
			.setHeader(getTranslation("view.site-admin.resource-types.grid.column.type"))
			.setSortable(true);
		grid.addColumn(ResourceTypeViewModel::getUnit)
			.setHeader(getTranslation("view.site-admin.resource-types.grid.column.unit"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.site-admin.resource-types.grid.column.actions"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(ResourceTypeViewModel serviceViewModel) {
		return new GridActionsButtonLayout(
			createContextMenu(serviceViewModel.getId(), serviceViewModel.getName())
		);
	}

	private Component createContextMenu(String serviceId, String resourceTypeName) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.resource-types.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(ResourceTypeFormView.class, serviceId)
		);

		Dialog confirmDialog = createConfirmDialog(serviceId, resourceTypeName);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.resource-types.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(String serviceId, String resourceTypeName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.site-admin.resource-types.dialog.text", resourceTypeName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			getResultOrException(() -> resourceTypeService.delete(serviceId, getCurrentResourceId()))
				.getException()
				.ifPresent(throwable -> showErrorNotification(getTranslation(throwable.getMessage(), resourceTypeName)));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadServicesViewsModels());
	}

	private List<ResourceTypeViewModel> loadServicesViewsModels() {
		return handleExceptions(() -> resourceTypeService.findAll(getCurrentResourceId()))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(ResourceTypeViewModelMapper::map)
			.sorted(comparing(serviceViewModel -> serviceViewModel.getName().toLowerCase()))
			.collect(toList());
	}
}
