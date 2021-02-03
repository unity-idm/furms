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
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.user_context.UserViewModel;
import io.imunity.furms.ui.user_context.UserViewModelMapper;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

import java.util.Collections;
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
public class UsersView extends FurmsViewComponent {
	private final ProjectService projectService;
	private final Project project;

	UsersView(ProjectService projectService, AuthzService authzService) {
		this.projectService = projectService;
		this.project = projectService.findById(getCurrentResourceId()).get();
		Grid<UserViewModel> grid = createGrid(loadUsers());
		HorizontalLayout searchLayout = createSearchFilterLayout(grid);

		String userId = authzService.getCurrentUserId();
		HorizontalLayout membershipLayout = createMembershipLayout(grid, userId);
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(getTranslation("view.project-admin.users.header", project.getName()),
			membershipLayout);
		getContent().add(headerLayout, searchLayout ,grid);
	}

	private HorizontalLayout createMembershipLayout(Grid<UserViewModel> grid, String userId) {
		Button joinButton = new Button(getTranslation("view.project-admin.users.button.join"));
		Button demitButton = new Button(getTranslation("view.project-admin.users.button.demit"));
		if(projectService.isMember(project.getCommunityId(), project.getId(), userId))
			joinButton.setVisible(false);
		else
			demitButton.setVisible(false);

		joinButton.addClickListener(x -> {
			joinButton.setVisible(false);
			demitButton.setVisible(true);
			projectService.addMember(project.getCommunityId(), project.getId(), userId);
			grid.setItems(loadUsers());
		});

		demitButton.addClickListener(x -> {
			joinButton.setVisible(true);
			demitButton.setVisible(false);
			projectService.removeMember(project.getCommunityId(), project.getId(), userId);
			grid.setItems(loadUsers());
		});
		return new HorizontalLayout(joinButton, demitButton);
	}

	private List<UserViewModel> loadUsers() {
		return handleExceptions(() -> projectService.findUsers(project.getCommunityId(), project.getId()))
			.orElseGet(Collections::emptyList)
			.stream()
			.map(UserViewModelMapper::map)
			.collect(toList());
	}

	private Grid<UserViewModel> createGrid(List<UserViewModel> users) {
		Grid<UserViewModel> grid = new SparseGrid<>(UserViewModel.class);
		grid.getStyle().set("word-wrap", "break-word");
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		grid.addComponentColumn(c -> new Div(new Span(c.firstName + " " + c.lastName)))
			.setHeader(getTranslation("view.project-admin.users.grid.column.1"))
			.setFlexGrow(30);
		grid.addColumn(c -> c.email)
			.setHeader(getTranslation("view.project-admin.users.grid.column.2"))
			.setFlexGrow(60);
		grid.addColumn(c -> "Active")
			.setHeader(getTranslation("view.project-admin.users.grid.column.3"))
			.setFlexGrow(1);
		grid.addComponentColumn(c -> {
			HorizontalLayout horizontalLayout = new HorizontalLayout(addMenu(grid, c.id));
			horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
			return horizontalLayout;
		})
			.setHeader(getTranslation("view.project-admin.users.grid.column.4"))
			.setTextAlign(ColumnTextAlign.END);
		grid.addItemClickListener(event -> {
			event.getItem().icon = grid.isDetailsVisible(event.getItem()) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
			grid.getDataProvider().refreshItem(event.getItem());
		});
		grid.setItems(users);
		return grid;
	}

	private Component addMenu(Grid<UserViewModel> grid, String id) {
		GridActionMenu contextMenu = new GridActionMenu();

		String deleteLabel = getTranslation("view.project-admin.users.context.menu.remove");
		contextMenu.addItem(addMenuButton(deleteLabel, MINUS_CIRCLE), event -> {
			handleExceptions(() -> projectService.removeMember(project.getCommunityId(), project.getId(), id));
			grid.setItems(loadUsers());
		});
		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Button addMenuButton(String label, VaadinIcon icon) {
		Button button = new Button(label, icon.create());
		button.addThemeVariants(LUMO_TERTIARY);
		return button;
	}

	private HorizontalLayout createSearchFilterLayout(Grid<UserViewModel> grid) {
		TextField textField = new TextField();
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.setPlaceholder(getTranslation("view.project-admin.users.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.addValueChangeListener(event -> {
			String value = event.getValue().toLowerCase();
			List<UserViewModel> filteredUsers = loadUsers().stream()
				.filter(user ->
					filterColumn(user.firstName, value)
						|| filterColumn(user.lastName, value)
						|| filterColumn(user.email, value)
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

}
