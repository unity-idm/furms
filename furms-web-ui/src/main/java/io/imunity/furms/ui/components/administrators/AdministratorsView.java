/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.MINUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.PAPERPLANE;
import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.stream.Collectors.toList;

public abstract class AdministratorsView extends FurmsViewComponent {

	protected abstract Supplier<List<User>> fetchUsers();
	protected abstract void addUser(String id);
	protected abstract void removeUser(String id);

	private final Grid<UserViewModel> grid;

	protected final UsersDAO usersDAO;

	public AdministratorsView(UsersDAO usersDAO) {
		this.usersDAO = usersDAO;
		this.grid = new SparseGrid<>(UserViewModel.class);

		addHeader();
		addSearchForm();
		addGrid();
	}

	private void addHeader() {
		TextField email = new TextField();
		email.setPlaceholder(getTranslation("view.fenix-admin.administrators.field.invite"));

		Button inviteButton = new Button(getTranslation("view.fenix-admin.administrators.button.invite"), PAPERPLANE.create(),
				e -> doInviteAction(email));

		HorizontalLayout inviteUserLayout = new HorizontalLayout(email, inviteButton);
		inviteUserLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		inviteUserLayout.setSpacing(true);

		getContent().add(new ViewHeaderLayout(getTranslation("view.fenix-admin.administrators.header"), inviteUserLayout));
	}

	private void addSearchForm() {
		TextField textField = new TextField();
		textField.setPlaceholder(getTranslation("view.fenix-admin.administrators.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.addValueChangeListener(event -> {
			textField.blur();
			String value = event.getValue().toLowerCase();
			List<UserViewModel> filteredUsers = loadUsers().stream()
					.filter(user -> rowContains(user, value))
					.collect(toList());
			UI.getCurrent().accessSynchronously(() -> loadGrid(filteredUsers));
			textField.focus();
		});

		HorizontalLayout search = new HorizontalLayout(textField);
		search.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

		getContent().add(search);
	}

	private void addGrid() {
		grid.getStyle().set("word-wrap", "break-word");
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

		grid.addComponentColumn(c -> new Div(c.icon, new Span(c.firstName + " " + c.lastName)))
				.setHeader(getTranslation("view.fenix-admin.administrators.grid.column.1"))
				.setFlexGrow(30);
		grid.addColumn(c -> c.email)
				.setHeader(getTranslation("view.fenix-admin.administrators.grid.column.2"))
				.setFlexGrow(60);
		grid.addColumn(c -> "Active")
				.setHeader(getTranslation("view.fenix-admin.administrators.grid.column.3"))
				.setFlexGrow(1);
		grid.addComponentColumn(c -> addMenu(c.id))
				.setHeader(getTranslation("view.fenix-admin.administrators.grid.column.4"))
				.setTextAlign(ColumnTextAlign.END);
		//TODO for now we do not have user data since to show
//		grid.setItemDetailsRenderer(new ComponentRenderer<>(
//			data -> {
//				VerticalLayout layout = new VerticalLayout();
//				layout.add(new Span("Example Data"), new Span("Additional Data"));
//				return layout;
//			})
//		);
		grid.addItemClickListener(event -> {
			event.getItem().icon = grid.isDetailsVisible(event.getItem()) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
			grid.getDataProvider().refreshItem(event.getItem());
		});
		loadGrid();

		getContent().add(grid);
	}

	private Component addMenu(String id) {
		GridActionMenu contextMenu = new GridActionMenu();

		Button button = new Button(getTranslation("view.fenix-admin.administrators.context.menu.remove"),
				MINUS_CIRCLE.create());
		button.addThemeVariants(LUMO_TERTIARY);

		contextMenu.addItem(button, event -> doRemoveItemAction(id));
		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private void doInviteAction(TextField email) {
		usersDAO.findByEmail(email.getValue()).ifPresentOrElse(
				user -> {
					handleExceptions(() -> addUser(user.id));
					loadGrid();
					email.clear();
				},
				() -> showErrorNotification(getTranslation("view.fenix-admin.administrators.error.validation.field.invite")));
	}

	private void doRemoveItemAction(String id) {
		if (grid.getDataProvider().size(new Query<>()) > 1) {
			handleExceptions(() -> removeUser(id));
			loadGrid();
		} else {
			showErrorNotification(getTranslation("view.fenix-admin.administrators.error.validation.remove"));
		}
	}

	private void loadGrid() {
		grid.setItems(loadUsers());
	}

	private void loadGrid(List<UserViewModel> users) {
		grid.setItems(users);
	}

	private List<UserViewModel> loadUsers() {
		return handleExceptions(fetchUsers())
				.orElseGet(Collections::emptyList)
				.stream()
				.map(UserViewModel::new)
				.collect(toList());
	}

	private boolean rowContains(UserViewModel row, String value) {
		return columnContains(row.firstName, value)
				|| columnContains(row.lastName, value)
				|| columnContains(row.email, value);
	}

	private boolean columnContains(String column, String value) {
		return Optional.ofNullable(column)
				.map(String::toLowerCase)
				.map(c -> c.contains(value))
				.orElse(false);
	}

}
