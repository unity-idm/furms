/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import com.google.common.collect.ImmutableList;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.SortDirection;
import io.imunity.furms.ui.components.SparseGrid;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static io.imunity.furms.domain.users.UserStatus.ENABLED;
import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

public class UserGrid {
	private final Grid<UserGridItem> grid;
	private final Supplier<List<UserGridItem>> fetchUserGridItemsAction;

	private UserGrid(Grid<UserGridItem> grid, Supplier<List<UserGridItem>> fetchUserGridItemsAction) {
		this.grid = grid;
		this.fetchUserGridItemsAction = fetchUserGridItemsAction;
	}

	public Component getGrid(){
		return grid;
	}

	public void reloadGrid() {
		grid.setItems(fetchUserGridItemsAction.get());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static UserGrid.Builder defaultInit(UserContextMenuFactory factory){
		return new Builder()
			.withFullNameColumn()
			.withEmailColumn()
			.withStatusColumn()
			.withContextMenuColumn(factory);
	}

	public static final class Builder {
		private final Grid<UserGridItem> grid = new SparseGrid<>(UserGridItem.class);
		private Supplier<List<UserGridItem>> fetchUsersAction;

		private Builder() {
			grid.getStyle().set("word-wrap", "break-word");
			grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
			grid.addItemClickListener(event -> {
				event.getItem().setIcon(grid.isDetailsVisible(event.getItem()) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create());
				grid.getDataProvider().refreshItem(event.getItem());
			});
		}

		public Builder withFullNameColumn() {
			Grid.Column<UserGridItem> fullNameCol = grid.addComponentColumn(FullNameColumn::new)
				.setHeader(getTranslation("component.administrators.grid.column.1"))
				.setSortable(true)
				.setComparator(FullNameColumn::compareTo)
				.setFlexGrow(35);
			grid.sort(ImmutableList.of(new GridSortOrder<>(fullNameCol, SortDirection.ASCENDING)));
			return this;
		}

		public <T> Builder withCustomColumn(Function<T, String> valueProvider, String header) {
			grid.addColumn(t -> valueProvider.apply((T) t))
				.setHeader(header)
				.setSortable(true)
				.setFlexGrow(35);
			return this;
		}

		public Builder withEmailColumn() {
			grid.addColumn(UserGridItem::getEmail)
				.setHeader(getTranslation("component.administrators.grid.column.2"))
				.setSortable(true)
				.setFlexGrow(35);
			return this;
		}

		public Builder withStatusColumn() {
			grid.addColumn(this::addStatusLabel)
				.setHeader(getTranslation("component.administrators.grid.column.3"))
				.setSortable(true)
				.setFlexGrow(5);
			return this;
		}

		private String addStatusLabel(final UserGridItem userGridItem) {
			return userGridItem.getStatus() != null && userGridItem.getStatus().equals(ENABLED)
				? getTranslation("component.administrators.user.status.active")
				: getTranslation("component.administrators.user.status.inactive");
		}

		public Builder withContextMenuColumn(UserContextMenuFactory factory) {
			grid.addComponentColumn(x -> factory.get(x, () -> grid.setItems(fetchUsersAction.get()), () -> fetchUsersAction.get().size()))
				.setHeader(getTranslation("component.administrators.grid.column.4"))
				.setWidth("6em")
				.setTextAlign(ColumnTextAlign.END);
			return this;
		}

		public UserGrid build(Supplier<List<UserGridItem>> fetchUsersAction) {
			this.fetchUsersAction = fetchUsersAction;
			return new UserGrid(grid, fetchUsersAction);
		}

		private static class FullNameColumn extends Div {
			private FullNameColumn(UserGridItem c) {
				super(new Span(getFullName(c)));
			}

			private static String getFullName(UserGridItem c) {
				return c.getFirstName()
					.map(value -> value + " ").orElse("")
					+ c.getLastName().orElse("");
			}

			private static int compareTo(UserGridItem c1, UserGridItem c2) {
				String c1FullName = c1.getFirstName() + " " + c1.getLastName();
				String c2FullName = c2.getFirstName() + " " + c2.getLastName();
				return c1FullName.compareTo(c2FullName);
			}
		}
	}
}
