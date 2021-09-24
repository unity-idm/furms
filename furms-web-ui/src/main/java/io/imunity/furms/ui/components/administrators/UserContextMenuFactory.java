/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.views.landing.LandingPageView;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.MINUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.PAPERPLANE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

public class UserContextMenuFactory {
	private final Consumer<PersistentId> removeUserAction;
	private final Consumer<PersistentId> postRemoveUserAction;
	private final Consumer<InvitationId> resendInvitationAction;
	private final Consumer<InvitationId> removeInvitationAction;
	private final PersistentId currentUserId;
	private final boolean redirectOnCurrentUserRemoval;
	private final boolean allowRemovalOfLastUser;

	private final String confirmRemovalMessageKey;
	private final String confirmSelfRemovalMessageKey;
	private final String removalNotAllowedMessageKey;

	private final Set<CustomContextMenuItem> customContextMenuItems;

	UserContextMenuFactory(Consumer<PersistentId> removeUserAction,
	                       Consumer<PersistentId> postRemoveUserAction,
	                       PersistentId currentUserId,
	                       boolean redirectOnCurrentUserRemoval,
	                       boolean allowRemovalOfLastUser,
	                       String confirmRemovalMessageKey,
	                       String confirmSelfRemovalMessageKey,
	                       String removalNotAllowedMessageKey,
	                       Set<CustomContextMenuItem> customContextMenuItems,
	                       Consumer<InvitationId> resendInvitationAction,
	                       Consumer<InvitationId> removeInvitationAction) {
		this.removeUserAction = removeUserAction;
		this.postRemoveUserAction = postRemoveUserAction;
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
		this.customContextMenuItems = customContextMenuItems;
		this.resendInvitationAction = resendInvitationAction;
		this.removeInvitationAction = removeInvitationAction;
	}

	private void doRemoveYourself(Runnable gridReloader, Supplier<Integer> gridSizeLoader){
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation(confirmSelfRemovalMessageKey));
		furmsDialog.addConfirmButtonClickListener(event -> {
			if (allowRemoval(gridSizeLoader)) {
				getResultOrException(() -> removeUserAction.accept(currentUserId))
						.getException()
						.ifPresentOrElse(
								e -> showErrorNotification(getTranslation(e.getMessage())),
								() -> refreshUserRoles(gridReloader));
			} else {
				showErrorNotification(getTranslation(removalNotAllowedMessageKey));
			}
		});
		furmsDialog.open();
	}

	private void doRemoveItemAction(UserGridItem removedItem, Runnable gridReloader, Supplier<Integer> gridSizeLoader) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation(confirmRemovalMessageKey,
			getFullName(removedItem)));
		furmsDialog.addConfirmButtonClickListener(event -> {
			if (allowRemoval(gridSizeLoader)) {
				final PersistentId persistentId = removedItem.getId().orElse(null);
				handleExceptions(() -> {
					removeUserAction.accept(persistentId);
					if (postRemoveUserAction != null) {
						postRemoveUserAction.accept(persistentId);
					}
				});
				gridReloader.run();
			} else {
				showErrorNotification(getTranslation(removalNotAllowedMessageKey));
			}
		});
		furmsDialog.open();
	}

	private void refreshUserRoles(Runnable gridReloader) {
		if(redirectOnCurrentUserRemoval)
			UI.getCurrent().navigate(LandingPageView.class);
		else
			gridReloader.run();
	}

	private boolean allowRemoval(Supplier<Integer> gridSizeLoader) {
		return allowRemovalOfLastUser || gridSizeLoader.get() > 1;
	}

	public Component get(UserGridItem gridItem, Runnable gridReloader, Supplier<Integer> gridSizeLoader){
		GridActionMenu contextMenu = new GridActionMenu();

		if(removeUserAction != null && gridItem.getStatus().equals(UserStatus.ENABLED)) {
			Button button = new Button(getTranslation("component.administrators.context.menu.remove"),
				MINUS_CIRCLE.create());
			button.addThemeVariants(LUMO_TERTIARY);
			contextMenu.addItem(button, event -> {
				if (gridItem.getId().isPresent()
					&& gridItem.getId().get().equals(currentUserId))
					doRemoveYourself(gridReloader, gridSizeLoader);
				else
					doRemoveItemAction(gridItem, gridReloader, gridSizeLoader);
			});
		}
		if(resendInvitationAction != null && gridItem.getStatus().equals(UserStatus.DISABLED)){
			Button button = new Button(getTranslation("component.administrators.context.menu.resend.invitation"),
				PAPERPLANE.create());
			button.addThemeVariants(LUMO_TERTIARY);
			contextMenu.addItem(button, event -> {
				resendInvitationAction.accept(gridItem.getInvitationId().get());
			});
		}
		if(removeInvitationAction != null && gridItem.getStatus().equals(UserStatus.DISABLED)){
			Button button = new Button(getTranslation("component.administrators.context.menu.remove.invitation"),
				TRASH.create());
			button.addThemeVariants(LUMO_TERTIARY);
			contextMenu.addItem(button, event -> {
				removeInvitationAction.accept(gridItem.getInvitationId().get());
			});
		}
		customContextMenuItems.stream()
			.filter(item -> item.confirmer.test(gridItem))
			.forEach(item ->
			contextMenu.addItem((Component)item.buttonProvider.apply(gridItem), event -> {
				item.menuButtonHandler.accept(gridItem);
			})
		);
		if(contextMenu.getChildren().count() == 0)
			return new Div();
		return contextMenu.getTarget();
	}

	private static String getFullName(UserGridItem c) {
		return c.getFirstName()
			.map(value -> value + " ").orElse("")
			+ c.getLastName().orElse("");
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Consumer<PersistentId> removeUserAction;
		private Consumer<PersistentId> postRemoveUserAction;
		private PersistentId currentUserId;
		private boolean redirectOnCurrentUserRemoval = false;
		private boolean allowRemovalOfLastUser = false;
		private String confirmRemovalMessageKey;
		private String confirmSelfRemovalMessageKey;
		private String removalNotAllowedMessageKey;
		private final Set<CustomContextMenuItem> customContextMenuItems = new HashSet<>();
		private Consumer<InvitationId> resendInvitationAction;
		private Consumer<InvitationId> removeInvitationAction;

		private Builder() {
		}

		public Builder withRemoveUserAction(Consumer<PersistentId> removeUserAction) {
			this.removeUserAction = removeUserAction;
			return this;
		}

		public Builder withPostRemoveUserAction(Consumer<PersistentId> postRemoveUserAction) {
			if (this.removeUserAction == null) {
				throw new IllegalArgumentException("Post Remove Action required Remove User Action declared");
			}
			this.postRemoveUserAction = postRemoveUserAction;
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

		public <T> Builder addCustomContextMenuItem(Function<T, MenuButton> buttonProvider, Consumer<T> action) {
			this.customContextMenuItems.add(new CustomContextMenuItem(buttonProvider, action, x -> true));
			return this;
		}

		public <T> Builder addCustomContextMenuItem(Function<T, MenuButton> buttonProvider, Consumer<T> action, Predicate<T> predicate) {
			this.customContextMenuItems.add(new CustomContextMenuItem(buttonProvider, action, predicate));
			return this;
		}

		public Builder withRemoveInvitationAction(Consumer<InvitationId> removeInvitationAction) {
			this.removeInvitationAction = removeInvitationAction;
			return this;
		}

		public Builder withResendInvitationAction(Consumer<InvitationId> resendInvitationAction) {
			this.resendInvitationAction = resendInvitationAction;
			return this;
		}

		public Builder withRemovalNotAllowedMessageKey(String key) {
			this.removalNotAllowedMessageKey = key;
			return this;
		}

		public UserContextMenuFactory build() {
			return new UserContextMenuFactory(removeUserAction, postRemoveUserAction, currentUserId,
				redirectOnCurrentUserRemoval, allowRemovalOfLastUser, confirmRemovalMessageKey,
				confirmSelfRemovalMessageKey, removalNotAllowedMessageKey, customContextMenuItems,
				resendInvitationAction, removeInvitationAction);
		}
	}
}
