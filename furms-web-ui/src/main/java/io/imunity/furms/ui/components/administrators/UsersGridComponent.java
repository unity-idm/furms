/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import io.imunity.furms.domain.users.FURMSUser;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.stream.Collectors.toList;

public class UsersGridComponent extends VerticalLayout {

	private final UserGrid userGrid;

	private String searchText = "";

	public UsersGridComponent(Supplier<List<FURMSUser>> fetchUsersAction, UserGrid.Builder userGridBuilder) {
		this.userGrid = userGridBuilder.build(() -> loadUsers(fetchUsersAction));
		init();
	}

	public UsersGridComponent(UserGrid.Builder userGridBuilder, Supplier<List<UserGridItem>> fetchUsersAction) {
		this.userGrid = userGridBuilder.build(() -> loadPreparedUsers(fetchUsersAction));
		init();
	}

	private void init() {
		addSearchForm();
		add(userGrid.getGrid());
		userGrid.reloadGrid();
		setPadding(false);
	}

	public void reloadGrid(){
		userGrid.reloadGrid();
	}

	private void addSearchForm() {
		TextField textField = new TextField();
		textField.setPlaceholder(getTranslation("component.administrators.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.addValueChangeListener(event -> {
			textField.blur();
			searchText = event.getValue().toLowerCase();
			UI.getCurrent().accessSynchronously(userGrid::reloadGrid);
			textField.focus();
		});

		HorizontalLayout search = new HorizontalLayout(textField);
		search.setWidthFull();
		search.setAlignItems(Alignment.END);
		search.setJustifyContentMode(JustifyContentMode.END);

		add(search);
	}

	private List<UserGridItem> loadUsers(Supplier<List<FURMSUser>> fetchUsersAction) {
		return handleExceptions(fetchUsersAction)
				.orElseGet(Collections::emptyList)
				.stream()
				.map(UserGridItem::new)
				.sorted(Comparator.comparing(UserGridItem::getEmail))
				.filter(user -> rowContains(user, searchText))
				.collect(toList());
	}

	private List<UserGridItem> loadPreparedUsers(Supplier<List<UserGridItem>> fetchUsersAction) {
		return handleExceptions(fetchUsersAction)
			.orElseGet(Collections::emptyList)
			.stream()
			.sorted(Comparator.comparing(UserGridItem::getEmail))
			.filter(user -> rowContains(user, searchText))
			.collect(toList());
	}

	private boolean rowContains(UserGridItem row, String value) {
		return searchText.isEmpty() || columnContains(row.getFirstName(), value)
				|| columnContains(row.getLastName(), value)
				|| columnContains(Optional.ofNullable(row.getEmail()), value);
	}

	private boolean columnContains(Optional<String> column, String value) {
		return column
				.map(String::toLowerCase)
				.map(c -> c.contains(value))
				.orElse(false);
	}
}
