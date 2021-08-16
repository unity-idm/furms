/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.imunity.furms.domain.users.FURMSUser;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.stream.Collectors.toList;

public class UsersGridComponent extends VerticalLayout {

	private final UserGrid userGrid;
	private final SearchLayout searchLayout;

	private UsersGridComponent(UserGrid userGrid, SearchLayout searchLayout) {
		this.userGrid = userGrid;
		this.searchLayout = searchLayout;
		this.searchLayout.addValueChangeGridReloader(this::reloadGrid);

		add(searchLayout);
		add(userGrid.getGrid());
		userGrid.reloadGrid();
		setPadding(false);
	}

	public void reloadGrid(){
		userGrid.reloadGrid();
	}

	public static UsersGridComponent defaultInit(Supplier<List<FURMSUser>> fetchUsersAction, UserGrid.Builder userGridBuilder){
		SearchLayout searchLayout = new SearchLayout();
		return new UsersGridComponent(userGridBuilder.build(() -> loadUsers(fetchUsersAction, searchLayout)), searchLayout);
	}

	public static UsersGridComponent init(Supplier<List<UserGridItem>> fetchUsersAction, UserGrid.Builder userGridBuilder){
		SearchLayout searchLayout = new SearchLayout();
		return new UsersGridComponent(userGridBuilder.build(() -> loadPreparedUsers(fetchUsersAction, searchLayout)), searchLayout);
	}

	private static List<UserGridItem> loadUsers(Supplier<List<FURMSUser>> fetchUsersAction, SearchLayout searchLayout) {
		return handleExceptions(fetchUsersAction)
				.orElseGet(Collections::emptyList)
				.stream()
				.map(UserGridItem::new)
				.sorted(Comparator.comparing(UserGridItem::getEmail))
				.filter(user -> rowContains(user, searchLayout.getSearchText(), searchLayout))
				.collect(toList());
	}

	private static List<UserGridItem> loadPreparedUsers(Supplier<List<UserGridItem>> fetchUsersAction, SearchLayout searchLayout) {
		return handleExceptions(fetchUsersAction)
			.orElseGet(Collections::emptyList)
			.stream()
			.sorted(Comparator.comparing(UserGridItem::getEmail))
			.filter(user -> rowContains(user, searchLayout.getSearchText(), searchLayout))
			.collect(toList());
	}

	private static boolean rowContains(UserGridItem row, String value, SearchLayout searchLayout) {
		return searchLayout.getSearchText().isEmpty() || columnContains(row.getFirstName(), value)
				|| columnContains(row.getLastName(), value)
				|| columnContains(Optional.ofNullable(row.getEmail()), value);
	}

	private static boolean columnContains(Optional<String> column, String value) {
		return column
				.map(String::toLowerCase)
				.map(c -> c.contains(value))
				.orElse(false);
	}
}
