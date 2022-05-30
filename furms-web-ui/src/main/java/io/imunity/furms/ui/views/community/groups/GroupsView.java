/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.groups;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.generic_groups.GenericGroupService;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.RouterGridLink;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.SearchLayout;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

import java.util.Collections;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.icon.VaadinIcon.USERS;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Route(value = "community/admin/groups", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.groups.page.title")
public class GroupsView extends FurmsViewComponent {
	private final GenericGroupService genericGroupService;

	private Grid<GroupGridModel> grid;
	private final SearchLayout searchLayout;

	GroupsView(GenericGroupService genericGroupService) {
		this.genericGroupService = genericGroupService;
		this.searchLayout = new SearchLayout();

		loadPageContent();
	}

	private void loadPageContent() {
		grid = createPolicyDocumentGrid();
		searchLayout.addValueChangeGridReloader(this::loadGridContent);

		loadGridContent();
		ViewHeaderLayout viewHeaderLayout = new ViewHeaderLayout(
			getTranslation("view.community-admin.groups.page.header"), createAddButton()
		);
		getContent().add(viewHeaderLayout, searchLayout, grid);
	}

	private Button createAddButton() {
		Button addButton = new Button(getTranslation("view.community-admin.groups.button.add"), PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(GroupFormView.class));
		return addButton;
	}

	private void loadGridContent() {
		grid.setItems(loadGenericGroupGridModels());
	}

	private List<GroupGridModel> loadGenericGroupGridModels() {
		CommunityId communityId = new CommunityId(getCurrentResourceId());
		return handleExceptions(() -> genericGroupService.findAllGroupWithAssignmentsAmount(communityId))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(group -> new GroupGridModel(group.group.id, group.group.communityId, group.group.name, group.group.description, group.amount))
			.filter(model -> rowContains(model, searchLayout.getSearchText(), searchLayout))
			.sorted(comparing(projectViewModel -> projectViewModel.name.toLowerCase()))
			.collect(toList());
	}

	private boolean rowContains(GroupGridModel row, String value, SearchLayout searchLayout) {
		String lowerCaseValue = value.toLowerCase();
		return searchLayout.getSearchText().isEmpty()
			|| row.name.toLowerCase().contains(lowerCaseValue)
			|| row.description.toLowerCase().contains(lowerCaseValue)
			|| String.valueOf(row.membersAmount).contains(lowerCaseValue);
	}

	private Grid<GroupGridModel> createPolicyDocumentGrid() {
		Grid<GroupGridModel> grid = new DenseGrid<>(GroupGridModel.class);

		grid.addComponentColumn(model -> new RouterLink(model.name, GroupFormView.class, model.id.id.toString()))
			.setHeader(getTranslation("view.community-admin.groups.grid.1"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(model -> model.description)
			.setHeader(getTranslation("view.community-admin.groups.grid.2"))
			.setSortable(true);
		grid.addColumn(model -> model.membersAmount)
			.setHeader(getTranslation("view.community-admin.groups.grid.3"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.groups.grid.4"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(GroupGridModel model) {
		Component contextMenu = createContextMenu(model.id, model.name, model.communityId);
		return new GridActionsButtonLayout(new RouterGridLink(USERS, model.id.id.toString(), GroupMembersView.class), contextMenu);
	}

	private Component createContextMenu(GenericGroupId groupId, String groupName, CommunityId communityId) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.community-admin.groups.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(GroupFormView.class, groupId.id.toString())
		);

		Dialog confirmDialog = createConfirmDialog(groupId, groupName, communityId);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.community-admin.groups.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.community-admin.groups.menu.members"), USERS),
			event -> UI.getCurrent().navigate(GroupMembersView.class, groupId.id.toString())
		);

		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(GenericGroupId groupId, String groupName, CommunityId communityId) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.community-admin.groups.dialog.text", groupName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			try {
				genericGroupService.delete(communityId, groupId);
				loadGridContent();
			} catch (Exception e) {
				showErrorNotification(getTranslation("base.error.message"));
			}
		});
		return furmsDialog;
	}
}
