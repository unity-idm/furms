/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.api.validation.exceptions.ResourceCreditHasAllocationException;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsProgressBar;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.user_context.InvocationContext;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.components.support.GridUtils.getsLeadingPartOfUUID;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Route(value = "site/admin/resource/credits", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.resource-credits.page.title")
public class ResourceCreditsView extends FurmsViewComponent {

	private final ResourceCreditService resourceCreditService;
	private final Grid<ResourceCreditViewModel> grid;
	private ZoneId zoneId;

	public ResourceCreditsView(ResourceCreditService resourceCreditService, ResourceTypeService resourceTypeService) {
		this.resourceCreditService = resourceCreditService;
		this.grid = createResourceCreditGrid();
		zoneId = InvocationContext.getCurrent().getZone();

		Button addButton = createAddButton();
		loadGridContent();

		getContent().add(createHeaderLayout(addButton), new HorizontalLayout(grid));
	}

	private HorizontalLayout createHeaderLayout(Button addButton) {
		return new ViewHeaderLayout(getTranslation("view.site-admin.resource-credits.header"), addButton);
	}

	private Button createAddButton() {
		Button addButton = new Button(getTranslation("view.site-admin.resource-credits.button.add"), PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(ResourceCreditFormView.class));
		return addButton;
	}

	private Grid<ResourceCreditViewModel> createResourceCreditGrid() {
		Grid<ResourceCreditViewModel> grid = new SparseGrid<>(ResourceCreditViewModel.class);

		grid.addComponentColumn(c -> new RouterLink(c.getName(), ResourceCreditFormView.class, c.getId()))
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.name"))
			.setSortable(true)
			.setComparator(x -> x.getName().toLowerCase());
		grid.addColumn(c -> getsLeadingPartOfUUID(c.getId()))
				.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.id"))
				.setSortable(true)
				.setComparator(x -> x.getName().toLowerCase());
		grid.addColumn(ResourceCreditViewModel::getResourceTypeName)
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.resourceType"))
			.setSortable(true);
		grid.addColumn(ResourceCreditViewModel::getAmount)
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.credit"))
			.setSortable(true)
			.setComparator(comparing(model -> model.getAmount().amount));
		grid.addColumn(ResourceCreditViewModel::getDistributed)
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.distributed"))
			.setSortable(true)
			.setComparator(comparing(model -> model.getDistributed().amount));
		grid.addColumn(ResourceCreditViewModel::getRemaining)
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.remaining"))
			.setSortable(true)
			.setComparator(comparing(model -> model.getRemaining().amount));
		grid.addColumn(c -> c.getCreateTime().toLocalDate())
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.created"))
			.setSortable(true);
		grid.addColumn(c -> c.getStartTime().toLocalDate())
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.validFrom"))
			.setSortable(true);
		grid.addColumn(c -> c.getEndTime().toLocalDate())
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.validTo"))
			.setSortable(true);
		grid.addComponentColumn(model -> {
			double value = model.getConsumed()
				.divide(model.getAmount().amount, 4, HALF_UP)
				.doubleValue();
			return new FurmsProgressBar(value);
		})
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.consumption"))
			.setTextAlign(ColumnTextAlign.END);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.actions"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(ResourceCreditViewModel resourceTypeViewModel) {
		return new GridActionsButtonLayout(
			createContextMenu(resourceTypeViewModel.getId(), resourceTypeViewModel.getName())
		);
	}

	private Component createContextMenu(String resourceTypeId, String resourceCreditName) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.resource-credits.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(ResourceCreditFormView.class, resourceTypeId)
		);

		Dialog confirmDialog = createConfirmDialog(resourceTypeId, resourceCreditName);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.resource-credits.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(String resourceTypeId, String resourceCreditName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.site-admin.resource-credits.dialog.text", resourceCreditName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			handleExceptions(() -> resourceCreditService.delete(resourceTypeId, getCurrentResourceId()),
					Map.of(ResourceCreditHasAllocationException.class, "view.site-admin.resource-credits.form.error.resourceCreditHasAllocations"));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadServicesViewsModels());
	}

	private List<ResourceCreditViewModel> loadServicesViewsModels() {
		return handleExceptions(() -> resourceCreditService.findAll(getCurrentResourceId()))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(credit -> ResourceCreditViewModelMapper.map(credit, zoneId))
			.sorted(comparing(resourceTypeViewModel -> resourceTypeViewModel.getName().toLowerCase()))
			.collect(toList());
	}
}
