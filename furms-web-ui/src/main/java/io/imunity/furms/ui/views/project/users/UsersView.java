/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.users;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.components.administrators.AdministratorsGridItem;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.domain.constant.RoutesConst.PROJECT_BASE_LANDING_PAGE;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.stream.Collectors.toList;

@Route(value = PROJECT_BASE_LANDING_PAGE, layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.users.page.title")
public class UsersView extends FurmsLandingViewComponent {
	private final ProjectService projectService;
	private final AuthzService authzService;
	private final UserService userService;
	private Project project;
	private String currentUserId;
	private MembershipChangerComponent membershipLayout;

	UsersView(ProjectService projectService, AuthzService authzService, UserService userService) {
		this.projectService = projectService;
		this.authzService = authzService;
		this.userService = userService;
		loadPageContent();
	}

	private void loadPageContent() {
		project = projectService.findById(getCurrentResourceId()).get();
		Grid<AdministratorsGridItem> grid = createGrid(loadUsers());
		HorizontalLayout searchLayout = createSearchFilterLayout(grid);

		currentUserId = authzService.getCurrentUserId();
		membershipLayout = new MembershipChangerComponent(
			getTranslation("view.project-admin.users.button.join"),
			getTranslation("view.project-admin.users.button.demit"),
			() -> projectService.isUser(project.getCommunityId(), project.getId(), currentUserId)
		);
		membershipLayout.addJoinButtonListener(event -> {
			projectService.addUser(project.getCommunityId(), project.getId(), currentUserId);
			grid.setItems(loadUsers());
		});
		membershipLayout.addDemitButtonListener(event -> {
			projectService.removeUser(project.getCommunityId(), project.getId(), currentUserId);
			grid.setItems(loadUsers());
		});
		InviteUserComponent inviteUser = new InviteUserComponent(userService.getAllUsers());
		inviteUser.addInviteAction(event -> {
			projectService.inviteUser(project.getCommunityId(), project.getId(), inviteUser.getEmail());
			grid.setItems(loadUsers());
			membershipLayout.loadAppropriateButton();
			inviteUser.clear();
		});
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(getTranslation("view.project-admin.users.header", project.getName()), membershipLayout);
		getContent().add(headerLayout, inviteUser, searchLayout, grid);
	}

	private List<AdministratorsGridItem> loadUsers() {
		return handleExceptions(() -> projectService.findUsers(project.getCommunityId(), project.getId()))
			.orElseGet(Collections::emptyList)
			.stream()
			.map(AdministratorsGridItem::new)
			.sorted(Comparator.comparing(AdministratorsGridItem::getEmail))
			.collect(toList());
	}

	private Grid<AdministratorsGridItem> createGrid(List<AdministratorsGridItem> users) {
		Grid<AdministratorsGridItem> grid = new SparseGrid<>(AdministratorsGridItem.class);
		grid.getStyle().set("word-wrap", "break-word");
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		grid.addComponentColumn(c -> new Div(new Span(c.getFirstName() + " " + c.getLastName())))
			.setHeader(getTranslation("view.project-admin.users.grid.column.1"))
			.setFlexGrow(30)
			.setSortable(true);
		grid.addColumn(AdministratorsGridItem::getEmail)
			.setHeader(getTranslation("view.project-admin.users.grid.column.2"))
			.setFlexGrow(60)
			.setSortable(true);
		grid.addColumn(c -> "Active")
			.setHeader(getTranslation("view.project-admin.users.grid.column.3"))
			.setFlexGrow(1);
		grid.addComponentColumn(c -> {
			HorizontalLayout horizontalLayout = new HorizontalLayout(addMenu(grid, c.getId()));
			horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
			return horizontalLayout;
		})
			.setHeader(getTranslation("view.project-admin.users.grid.column.4"))
			.setTextAlign(ColumnTextAlign.END);
		grid.addItemClickListener(event -> {
			event.getItem().setIcon(grid.isDetailsVisible(event.getItem()) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create());
			grid.getDataProvider().refreshItem(event.getItem());
		});
		grid.setItems(users);
		return grid;
	}

	private Component addMenu(Grid<AdministratorsGridItem> grid, String id) {
		GridActionMenu contextMenu = new GridActionMenu();

		String deleteLabel = getTranslation("view.project-admin.users.context.menu.remove");
		contextMenu.addItem(addMenuButton(deleteLabel, MINUS_CIRCLE), event -> {
			handleExceptions(() -> projectService.removeUser(project.getCommunityId(), project.getId(), id));
			grid.setItems(loadUsers());
			membershipLayout.loadAppropriateButton();
		});
		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Button addMenuButton(String label, VaadinIcon icon) {
		Button button = new Button(label, icon.create());
		button.addThemeVariants(LUMO_TERTIARY);
		return button;
	}

	private HorizontalLayout createSearchFilterLayout(Grid<AdministratorsGridItem> grid) {
		TextField textField = new TextField();
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.setPlaceholder(getTranslation("view.project-admin.users.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.addValueChangeListener(event -> {
			String value = event.getValue().toLowerCase();
			List<AdministratorsGridItem> filteredUsers = loadUsers().stream()
				.filter(user ->
					filterColumn(user.getFirstName(), value)
						|| filterColumn(user.getLastName(), value)
						|| filterColumn(user.getEmail(), value)
				)
				.collect(toList());
			textField.blur();
			grid.setItems(filteredUsers);
			textField.focus();
		});

		HorizontalLayout search = new HorizontalLayout(textField);
		search.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		return search;
	}

	private boolean filterColumn(String column, String value) {
		return Optional.ofNullable(column)
			.map(String::toLowerCase)
			.map(c -> c.contains(value))
			.orElse(false);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
		getContent().removeAll();
		loadPageContent();
	}
}
