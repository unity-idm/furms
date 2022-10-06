/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import io.imunity.furms.api.validation.exceptions.ResourceCreditHasAllocationException;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsProgressBar;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.IdRenderer;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.user_context.UIContext;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

@Route(value = "site/admin/resource/credits", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.resource-credits.page.title")
public class ResourceCreditsView extends FurmsViewComponent {

	private final ResourceCreditService resourceCreditService;
	private final Grid<ResourceCreditViewModel> grid;
	private final ZoneId zoneId;

	public ResourceCreditsView(ResourceCreditService resourceCreditService) {
		this.resourceCreditService = resourceCreditService;
		this.grid = createResourceCreditGrid();
		zoneId = UIContext.getCurrent().getZone();

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
		Grid<ResourceCreditViewModel> grid = new DenseGrid<>(ResourceCreditViewModel.class);

		grid.addComponentColumn(c -> new RouterLink(c.getName(), ResourceCreditFormView.class, c.getId().id.toString()))
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.name"))
			.setSortable(true)
			.setFlexGrow(20)
			.setComparator(x -> x.getName().toLowerCase());
		grid.addColumn(new IdRenderer<>(ResourceCreditViewModel::getId))
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.id"))
			.setSortable(true)
			.setFlexGrow(20)
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
			.setComparator(comparing(ResourceCreditViewModel::getConsumed));
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.site-admin.resource-credits.grid.column.actions"))
			.setWidth("4em")
			.setTextAlign(ColumnTextAlign.END);
		return grid;
	}

	private HorizontalLayout createLastColumnContent(ResourceCreditViewModel resourceCreditViewModel) {
		return new GridActionsButtonLayout(
			createContextMenu(resourceCreditViewModel.getId(), resourceCreditViewModel.getName())
		);
	}

	private Component createContextMenu(ResourceCreditId resourceCreditId, String resourceCreditName) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.resource-credits.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(ResourceCreditFormView.class, resourceCreditId.id.toString())
		);

		Dialog confirmDialog = createConfirmDialog(resourceCreditId, resourceCreditName);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.resource-credits.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(ResourceCreditId resourceTypeId, String resourceCreditName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.site-admin.resource-credits.dialog.text", resourceCreditName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			handleExceptions(() -> resourceCreditService.delete(resourceTypeId, new SiteId(getCurrentResourceId())),
					Map.of(ResourceCreditHasAllocationException.class, "view.site-admin.resource-credits.form.error.resourceCreditHasAllocations"));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadServicesViewsModels());
	}

	private List<ResourceCreditViewModel> loadServicesViewsModels() {
		return handleExceptions(() -> resourceCreditService.findAllWithAllocations(new SiteId(getCurrentResourceId())))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(credit -> ResourceCreditViewModelMapper.map(credit, zoneId))
			.sorted(comparing(resourceTypeViewModel -> resourceTypeViewModel.getName().toLowerCase()))
			.collect(toList());
	}
}
