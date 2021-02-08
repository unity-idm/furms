/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import com.google.common.collect.ImmutableList;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.SparseGrid;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.domain.constant.RoutesConst.FRONT_LOGOUT_URL;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class AdministratorsGridComponent extends VerticalLayout {

	private final Grid<AdministratorsGridItem> grid;

	private final Supplier<List<User>> fetchUsersAction;
	private final Consumer<String> removeUserAction;
	private final String currentUserId;

	public AdministratorsGridComponent(Supplier<List<User>> fetchUsersAction, Consumer<String> removeUserAction, String currentUserId) {
		this.fetchUsersAction = fetchUsersAction;
		this.removeUserAction = removeUserAction;
		this.grid = new SparseGrid<>(AdministratorsGridItem.class);
		this.currentUserId = currentUserId;
		addSearchForm();
		addGrid();
	}

	public void reloadGrid() {
		grid.setItems(loadUsers());
	}

	private void addSearchForm() {
		TextField textField = new TextField();
		textField.setPlaceholder(getTranslation("component.administrators.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.addValueChangeListener(event -> {
			textField.blur();
			String value = event.getValue().toLowerCase();
			List<AdministratorsGridItem> filteredUsers = loadUsers().stream()
					.filter(user -> rowContains(user, value))
					.collect(toList());
			UI.getCurrent().accessSynchronously(() -> loadGrid(filteredUsers));
			textField.focus();
		});

		HorizontalLayout search = new HorizontalLayout(textField);
		search.setWidthFull();
		search.setAlignItems(Alignment.END);
		search.setJustifyContentMode(JustifyContentMode.END);

		add(search);
	}

	private void addGrid() {
		grid.getStyle().set("word-wrap", "break-word");
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

		Column<AdministratorsGridItem> fullNameCol = grid.addComponentColumn(FullNameColumn::new)
				.setHeader(getTranslation("component.administrators.grid.column.1"))
				.setSortable(true)
				.setComparator(FullNameColumn::compareTo)
				.setFlexGrow(60);
		grid.addColumn(AdministratorsGridItem::getEmail)
				.setHeader(getTranslation("component.administrators.grid.column.2"))
				.setSortable(true)
				.setFlexGrow(60);
		grid.addColumn(c -> "Active")
				.setHeader(getTranslation("component.administrators.grid.column.3"))
				.setFlexGrow(1);
		grid.addComponentColumn(c -> addMenu(c.getId()))
				.setHeader(getTranslation("component.administrators.grid.column.4"))
				.setTextAlign(ColumnTextAlign.END);
		grid.addItemClickListener(event -> {
			event.getItem().setIcon(grid.isDetailsVisible(event.getItem()) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create());
			grid.getDataProvider().refreshItem(event.getItem());
		});
		grid.sort(ImmutableList.of(new GridSortOrder<>(fullNameCol, SortDirection.ASCENDING)));
		reloadGrid();

		add(grid);
	}

	private Component addMenu(String id) {
		GridActionMenu contextMenu = new GridActionMenu();

		Button button = new Button(getTranslation("component.administrators.context.menu.remove"),
				MINUS_CIRCLE.create());
		button.addThemeVariants(LUMO_TERTIARY);

		contextMenu.addItem(button, event -> {
			if(id.equals(currentUserId))
				doRemoveYourself();
			else
				doRemoveItemAction(id);
		});
		add(contextMenu);
		return contextMenu.getTarget();
	}

	private void doRemoveYourself(){
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("component.administrators.remove.yourself.confirm"));
		furmsDialog.addConfirmButtonClickListener(event -> {
			if (loadUsers().size() > 1) {
				UI.getCurrent().getPage().setLocation(FRONT_LOGOUT_URL);
				handleExceptions(() -> removeUserAction.accept(currentUserId));
				reloadGrid();
			} else {
				showErrorNotification(getTranslation("component.administrators.error.validation.remove"));
			}
		});
		furmsDialog.open();
	}

	private void doRemoveItemAction(String id) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("component.administrators.remove.confirm"));
		furmsDialog.addConfirmButtonClickListener(event -> {
			if (loadUsers().size() > 1) {
				handleExceptions(() -> removeUserAction.accept(id));
				reloadGrid();
			} else {
				showErrorNotification(getTranslation("component.administrators.error.validation.remove"));
			}
		});
	}

	private void loadGrid(List<AdministratorsGridItem> users) {
		grid.setItems(users);
	}

	private List<AdministratorsGridItem> loadUsers() {
		return handleExceptions(fetchUsersAction)
				.orElseGet(Collections::emptyList)
				.stream()
				.map(AdministratorsGridItem::new)
				.sorted(Comparator.comparing(AdministratorsGridItem::getEmail))
				.collect(toList());
	}

	private boolean rowContains(AdministratorsGridItem row, String value) {
		return columnContains(row.getFirstName(), value)
				|| columnContains(row.getLastName(), value)
				|| columnContains(row.getEmail(), value);
	}

	private boolean columnContains(String column, String value) {
		return ofNullable(column)
				.map(String::toLowerCase)
				.map(c -> c.contains(value))
				.orElse(false);
	}
	
	private static class FullNameColumn extends Div {
		private FullNameColumn(AdministratorsGridItem c) {
			super(/*c.getIcon(),*/ new Span(c.getFirstName() + " " + c.getLastName()));
		}
		
		private static int compareTo(AdministratorsGridItem c1, AdministratorsGridItem c2) {
			String c1FullName = c1.getFirstName() + " " + c1.getLastName();
			String c2FullName = c2.getFirstName() + " " + c2.getLastName();
			return c1FullName.compareTo(c2FullName);
		}
	}
}
