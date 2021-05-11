/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.MINUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
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

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.views.landing.LandingPageView;

import static io.imunity.furms.domain.users.UserStatus.ENABLED;

public class UsersGridComponent extends VerticalLayout {

	private final Grid<AdministratorsGridItem> grid;

	private final Supplier<List<FURMSUser>> fetchUsersAction;
	private final Consumer<PersistentId> removeUserAction;
	private final PersistentId currentUserId;
	private final boolean redirectOnCurrentUserRemoval;
	private final boolean allowRemovalOfLastUser;
	
	private final String confirmRemovalMessageKey;
	private final String confirmSelfRemovalMessageKey;
	private final String removalNotAllowedMessageKey;
	
	private String searchText = "";

	private UsersGridComponent(Supplier<List<FURMSUser>> fetchUsersAction,
			Consumer<PersistentId> removeUserAction,
			PersistentId currentUserId,
			boolean redirectOnCurrentUserRemoval,
			boolean allowRemovalOfLastUser,
			String confirmRemovalMessageKey,
			String confirmSelfRemovalMessageKey,
			String removalNotAllowedMessageKey) {
		this.fetchUsersAction = fetchUsersAction;
		this.removeUserAction = removeUserAction;
		this.grid = new SparseGrid<>(AdministratorsGridItem.class);
		this.currentUserId = currentUserId;
		this.redirectOnCurrentUserRemoval = redirectOnCurrentUserRemoval;
		this.allowRemovalOfLastUser = allowRemovalOfLastUser;
		this.confirmRemovalMessageKey = confirmRemovalMessageKey == null 
				? "component.administrators.remove.confirm"
				: confirmRemovalMessageKey;
		this.confirmSelfRemovalMessageKey = confirmSelfRemovalMessageKey == null
				? "component.administrators.remove.yourself.confirm"
				: confirmSelfRemovalMessageKey;
		this.removalNotAllowedMessageKey = removalNotAllowedMessageKey == null
				? "component.administrators.error.validation.remove"
				: removalNotAllowedMessageKey;
		addSearchForm();
		addGrid();
		setPadding(false);
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
			searchText = event.getValue().toLowerCase();
			UI.getCurrent().accessSynchronously(() -> loadGrid(loadUsers()));
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
				.setFlexGrow(35);
		grid.addColumn(AdministratorsGridItem::getEmail)
				.setHeader(getTranslation("component.administrators.grid.column.2"))
				.setSortable(true)
				.setFlexGrow(35);
		grid.addColumn(this::addStatusLabel)
				.setHeader(getTranslation("component.administrators.grid.column.3"))
				.setSortable(true)
				.setFlexGrow(5);
		grid.addComponentColumn(this::addMenu)
				.setHeader(getTranslation("component.administrators.grid.column.4"))
				.setWidth("6em")
				.setTextAlign(ColumnTextAlign.END);
		grid.addItemClickListener(event -> {
			event.getItem().setIcon(grid.isDetailsVisible(event.getItem()) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create());
			grid.getDataProvider().refreshItem(event.getItem());
		});
		grid.sort(ImmutableList.of(new GridSortOrder<>(fullNameCol, SortDirection.ASCENDING)));
		reloadGrid();

		add(grid);
	}

	private String addStatusLabel(final AdministratorsGridItem administratorsGridItem) {
		return administratorsGridItem.getStatus() != null && administratorsGridItem.getStatus().equals(ENABLED)
				? getTranslation("component.administrators.user.status.active")
				: getTranslation("component.administrators.user.status.inactive");
	}

	private Component addMenu(AdministratorsGridItem gridItem) {
		GridActionMenu contextMenu = new GridActionMenu();

		Button button = new Button(getTranslation("component.administrators.context.menu.remove"),
				MINUS_CIRCLE.create());
		button.addThemeVariants(LUMO_TERTIARY);

		contextMenu.addItem(button, event -> {
			if(gridItem.getId().isPresent()
					&& gridItem.getId().get().equals(currentUserId))
				doRemoveYourself();
			else
				doRemoveItemAction(gridItem);
		});
		add(contextMenu);
		return contextMenu.getTarget();
	}

	private void doRemoveYourself(){
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation(confirmSelfRemovalMessageKey));
		furmsDialog.addConfirmButtonClickListener(event -> {
			if (allowRemoval()) {
				getResultOrException(() -> removeUserAction.accept(currentUserId))
					.getThrowable().ifPresentOrElse(
						e -> showErrorNotification(getTranslation(e.getMessage())),
					this::refreshUserRoles
				);
			} else {
				showErrorNotification(getTranslation(removalNotAllowedMessageKey));
			}
		});
		furmsDialog.open();
	}

	private void refreshUserRoles() {
		if(redirectOnCurrentUserRemoval)
			UI.getCurrent().navigate(LandingPageView.class);
		else
			reloadGrid();
	}

	private void doRemoveItemAction(AdministratorsGridItem removedItem) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation(confirmRemovalMessageKey, 
				FullNameColumn.getFullName(removedItem)));
		furmsDialog.addConfirmButtonClickListener(event -> {
			if (allowRemoval()) {
				handleExceptions(() -> removeUserAction.accept(removedItem.getId().orElse(null)));
				reloadGrid();
			} else {
				showErrorNotification(getTranslation(removalNotAllowedMessageKey));
			}
		});
		furmsDialog.open();
	}
	
	private boolean allowRemoval() {
		return allowRemovalOfLastUser || loadUsers().size() > 1;
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
				.filter(user -> rowContains(user, searchText))
				.collect(toList());
	}

	private boolean rowContains(AdministratorsGridItem row, String value) {
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
	
	private static class FullNameColumn extends Div {
		private FullNameColumn(AdministratorsGridItem c) {
			super(/*c.getIcon(),*/ new Span(getFullName(c)));
		}

		private static String getFullName(AdministratorsGridItem c) {
			return c.getFirstName()
				.map(value -> value + " ").orElse("")
				+ c.getLastName().orElse("");
		}

		private static int compareTo(AdministratorsGridItem c1, AdministratorsGridItem c2) {
			String c1FullName = c1.getFirstName() + " " + c1.getLastName();
			String c2FullName = c2.getFirstName() + " " + c2.getLastName();
			return c1FullName.compareTo(c2FullName);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Supplier<List<FURMSUser>> fetchUsersAction;
		private Consumer<PersistentId> removeUserAction;
		private PersistentId currentUserId;
		private boolean redirectOnCurrentUserRemoval = false;
		private boolean allowRemovalOfLastUser = false;
		private String confirmRemovalMessageKey;
		private String confirmSelfRemovalMessageKey;
		private String removalNotAllowedMessageKey;

		private Builder() {
		}

		public Builder withFetchUsersAction(Supplier<List<FURMSUser>> fetchUsersAction) {
			this.fetchUsersAction = fetchUsersAction;
			return this;
		}

		public Builder withRemoveUserAction(Consumer<PersistentId> removeUserAction) {
			this.removeUserAction = removeUserAction;
			return this;
		}

		public Builder withCurrentUserId(PersistentId currentUserId) {
			this.currentUserId = currentUserId;
			return this;
		}

		public Builder redirectOnCurrentUserRemoval() {
			this.redirectOnCurrentUserRemoval = true;
			return this;
		}

		public Builder allowRemovalOfLastUser() {
			this.allowRemovalOfLastUser = true;
			return this;
		}
		
		public Builder withConfirmRemovalMessageKey(String key) {
			this.confirmRemovalMessageKey = key;
			return this;
		}
		
		public Builder withConfirmSelfRemovalMessageKey(String key) {
			this.confirmSelfRemovalMessageKey = key;
			return this;
		}

		public Builder withRemovalNotAllowedMessageKey(String key) {
			this.removalNotAllowedMessageKey = key;
			return this;
		}
		
		public UsersGridComponent build() {
			Preconditions.checkNotNull(fetchUsersAction);
			Preconditions.checkNotNull(removeUserAction);
			Preconditions.checkNotNull(currentUserId);
			return new UsersGridComponent(fetchUsersAction, removeUserAction, currentUserId,
					redirectOnCurrentUserRemoval, allowRemovalOfLastUser, confirmRemovalMessageKey,
					confirmSelfRemovalMessageKey, removalNotAllowedMessageKey);
		}
	}
}
