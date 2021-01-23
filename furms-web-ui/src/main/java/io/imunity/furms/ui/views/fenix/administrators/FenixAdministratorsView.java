/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.administrators;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.PAPERPLANE;
import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

@Route(value = "fenix/admin/administrators", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.administrators.page.title")
public class FenixAdministratorsView extends FurmsViewComponent {
	private final UsersDAO usersDAO;

	FenixAdministratorsView(UsersDAO usersDAO) {
		this.usersDAO = usersDAO;

		Grid<UserViewModel> grid = createGrid(loadUsers(usersDAO::getAdminUsers));
		HorizontalLayout inviteUserLayout = createInviteUserLayout(grid);
		HorizontalLayout searchLayout = createSearchFilterLayout(grid);

		ViewHeaderLayout headerLayout = new ViewHeaderLayout(getTranslation("view.fenix-admin.administrators.header"), 
				inviteUserLayout);
		getContent().add(headerLayout, searchLayout ,grid);
	}

	private List<UserViewModel> loadUsers(Supplier<List<User>> supplier) {
		return handleExceptions(supplier)
			.orElseGet(Collections::emptyList)
			.stream()
			.map(UserViewModelMapper::map)
			.collect(toList());
	}

	private HorizontalLayout createInviteUserLayout(Grid<UserViewModel> grid) {
		TextField emailTextField = new TextField();
		emailTextField.setPlaceholder(getTranslation("view.fenix-admin.administrators.field.invite"));
		String inviteLabel = getTranslation("view.fenix-admin.administrators.button.invite");
		Button inviteButton = new Button(inviteLabel, PAPERPLANE.create());
		inviteButton.addClickListener(event -> {
			String value = emailTextField.getValue();
			loadUsers(usersDAO::getAllUsers).stream()
				.filter(u -> u.email.equals(value))
				.findAny()
				.ifPresentOrElse(
					user -> {
						handleExceptions(() -> usersDAO.addFenixAdminRole(user.id));
						grid.setItems(loadUsers(usersDAO::getAdminUsers));
						},
					() -> Notification.show(getTranslation("view.fenix-admin.administrators.error.validation.field.invite"))
				);
		});
		HorizontalLayout horizontalLayout = new HorizontalLayout(emailTextField, inviteButton);
		horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		horizontalLayout.setSpacing(true);
		return horizontalLayout;
	}

	private HorizontalLayout createSearchFilterLayout(Grid<UserViewModel> grid) {
		TextField textField = new TextField();
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.setPlaceholder(getTranslation("view.fenix-admin.administrators.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.addValueChangeListener(event -> {
			String value = event.getValue().toLowerCase();
			List<UserViewModel> filteredUsers = loadUsers(usersDAO::getAdminUsers).stream()
				.filter(user ->
					filterColumn(user.firstName, value)
						|| filterColumn(user.lastName, value)
						|| filterColumn(user.email, value)
				)
				.collect(toList());
			grid.setItems(filteredUsers);
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

	private Grid<UserViewModel> createGrid(List<UserViewModel> users) {
		Grid<UserViewModel> grid = new SparseGrid<>(UserViewModel.class);
		grid.getStyle().set("word-wrap", "break-word");
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		grid.addComponentColumn(c -> new Div(c.icon, new Span(c.firstName)))
			.setHeader(getTranslation("view.fenix-admin.administrators.grid.column.1"));
		grid.addColumn(c -> c.lastName).setHeader(getTranslation("view.fenix-admin.administrators.grid.column.2"));
		grid.addColumn(c -> c.email).setHeader(getTranslation("view.fenix-admin.administrators.grid.column.3"));
		grid.addColumn(c -> "Active").setHeader(getTranslation("view.fenix-admin.administrators.grid.column.4"));
		grid.addComponentColumn(c -> {
			HorizontalLayout horizontalLayout = new HorizontalLayout(addMenu(grid, c.id));
			horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
			return horizontalLayout;
		})
			.setHeader(getTranslation("view.fenix-admin.administrators.grid.column.3"))
			.setTextAlign(ColumnTextAlign.END);
		grid.setItemDetailsRenderer(new ComponentRenderer<>(
			data -> {
				VerticalLayout layout = new VerticalLayout();
				layout.add(new Span("Example Data"), new Span("Additional Data"));
				return layout;
			})
		);
		grid.addItemClickListener(event -> {
			event.getItem().icon = grid.isDetailsVisible(event.getItem()) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
			grid.getDataProvider().refreshItem(event.getItem());
		});
		grid.setItems(users);
		return grid;
	}

	private Component addMenu(Grid<UserViewModel> grid, String id) {
		GridActionMenu contextMenu = new GridActionMenu();
		
		String deleteLabel = getTranslation("view.fenix-admin.administrators.context.menu.delete");
		contextMenu.addItem(addMenuButton(deleteLabel, TRASH), event -> {
			handleExceptions(() -> usersDAO.removeFenixAdminRole(id));
			grid.setItems(loadUsers(usersDAO::getAdminUsers));
		});
		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Button addMenuButton(String label, VaadinIcon icon) {
		Button button = new Button(label, icon.create());
		button.addThemeVariants(LUMO_TERTIARY);
		return button;
	}
}
