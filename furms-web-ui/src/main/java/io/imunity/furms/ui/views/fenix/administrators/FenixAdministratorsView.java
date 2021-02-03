/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.administrators;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;
import io.imunity.furms.ui.user_context.FurmsViewUserModelMapper;
import io.imunity.furms.ui.user_context.UserViewModel;
import io.imunity.furms.ui.user_context.UserViewModelMapper;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.stream.Collectors.toList;

@Route(value = "fenix/admin/administrators", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.administrators.page.title")
public class FenixAdministratorsView extends FurmsViewComponent {
	private final UserService userService;

	FenixAdministratorsView(UserService userService) {
		this.userService = userService;

		Grid<UserViewModel> grid = createGrid(loadUsers(userService::getFenixAdmins));
		FurmsUserComboBox furmsUserComboBox = new FurmsUserComboBox(FurmsViewUserModelMapper.mapList(userService.getAllUsers()));
		Button inviteButton = createInviteButton(grid, furmsUserComboBox.comboBox);
		HorizontalLayout inviteUserLayout = createInviteUserLayout(furmsUserComboBox, inviteButton);
		HorizontalLayout searchLayout = createSearchFilterLayout(grid, inviteButton);

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

	private HorizontalLayout createInviteUserLayout(Component emailTextField, Button inviteButton) {
		HorizontalLayout horizontalLayout = new HorizontalLayout(emailTextField, inviteButton);
		horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		horizontalLayout.setSpacing(true);
		return horizontalLayout;
	}

	private Button createInviteButton(Grid<UserViewModel> grid, ComboBox<FurmsViewUserModel> userComboBox) {
		String inviteLabel = getTranslation("view.fenix-admin.administrators.button.invite");
		Button inviteButton = new Button(inviteLabel, PAPERPLANE.create());
		inviteButton.addClickListener(event -> {
			String value = userComboBox.getValue().email;
			loadUsers(userService::getAllUsers).stream()
				.filter(u -> u.email.equals(value))
				.findAny()
				.ifPresentOrElse(
					user -> {
						handleExceptions(() -> userService.addFenixAdminRole(user.id));
						grid.setItems(loadUsers(userService::getFenixAdmins));
						userComboBox.clear();
						},
					() -> showErrorNotification(getTranslation("view.fenix-admin.administrators.error.validation.field.invite"))
				);
		});
		return inviteButton;
	}

	private HorizontalLayout createSearchFilterLayout(Grid<UserViewModel> grid, Button inviteButton) {
		TextField textField = new TextField();
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.setPlaceholder(getTranslation("view.fenix-admin.administrators.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.addValueChangeListener(event -> {
			String value = event.getValue().toLowerCase();
			List<UserViewModel> filteredUsers = loadUsers(userService::getFenixAdmins).stream()
				.filter(user ->
					filterColumn(user.firstName, value)
						|| filterColumn(user.lastName, value)
						|| filterColumn(user.email, value)
				)
				.collect(toList());
			grid.setItems(filteredUsers);
			//TODO This is a work around to fix disappearing text cursor
			inviteButton.focus();
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

	private Grid<UserViewModel> createGrid(List<UserViewModel> users) {
		Grid<UserViewModel> grid = new SparseGrid<>(UserViewModel.class);
		grid.getStyle().set("word-wrap", "break-word");
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		grid.addComponentColumn(c -> new Div(new Span(c.firstName + " " + c.lastName)))
			.setHeader(getTranslation("view.fenix-admin.administrators.grid.column.1"))
			.setFlexGrow(30);
		grid.addColumn(c -> c.email)
			.setHeader(getTranslation("view.fenix-admin.administrators.grid.column.2"))
			.setFlexGrow(60);
		grid.addColumn(c -> "Active")
			.setHeader(getTranslation("view.fenix-admin.administrators.grid.column.3"))
			.setFlexGrow(1);
		grid.addComponentColumn(c -> {
				HorizontalLayout horizontalLayout = new HorizontalLayout(addMenu(grid, c.id));
				horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
				return horizontalLayout;
			})
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
		grid.setItems(users);
		return grid;
	}

	private Component addMenu(Grid<UserViewModel> grid, String id) {
		GridActionMenu contextMenu = new GridActionMenu();
		
		String deleteLabel = getTranslation("view.fenix-admin.administrators.context.menu.remove");
		contextMenu.addItem(addMenuButton(deleteLabel, MINUS_CIRCLE), event -> {
			if(grid.getDataProvider().size(new Query<>()) > 1) {
				handleExceptions(() -> userService.removeFenixAdminRole(id));
				grid.setItems(loadUsers(userService::getFenixAdmins));
			}
			else
				showErrorNotification(getTranslation("view.fenix-admin.administrators.error.validation.remove"));
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
