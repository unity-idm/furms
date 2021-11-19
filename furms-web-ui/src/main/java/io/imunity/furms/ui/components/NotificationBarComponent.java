/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.shared.Registration;
import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.notification.UserApplicationsListChangedEvent;
import io.imunity.furms.domain.notification.UserInvitationsListChangedEvent;
import io.imunity.furms.domain.notification.UserPoliciesListChangedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.VaadinListener;
import io.imunity.furms.ui.notifications.NotificationBarElement;
import io.imunity.furms.ui.notifications.UINotificationService;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;

import java.util.Iterator;
import java.util.Set;

import static com.vaadin.flow.component.icon.VaadinIcon.BELL;

@CssImport("./styles/components/notification-bar.css")
@CssImport(value = "./styles/custom-lumo-theme.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class NotificationBarComponent extends Button {
	private final VaadinBroadcaster vaadinBroadcaster;
	private final UINotificationService notificationService;
	private final FURMSUser currentUser;
	private final Span badge;
	private final ContextMenu contextMenu;
	private final RoleTranslator roleTranslator;
	private Registration broadcasterRegistration;

	public NotificationBarComponent(VaadinBroadcaster vaadinBroadcaster, UINotificationService notificationService,
	                                FURMSUser currentUser, RoleTranslator roleTranslator) {
		this.vaadinBroadcaster = vaadinBroadcaster;
		this.notificationService = notificationService;
		this.roleTranslator = roleTranslator;
		this.currentUser = currentUser;

		badge = new Span();
		badge.getElement().setAttribute("theme","badge error primary small pill");
		contextMenu = new ContextMenu();
		contextMenu.setOpenOnClick(true);
		contextMenu.setTarget(this);

		setIcon(new Div(BELL.create(), badge));
		loadData();
	}

	public Component getContextMenuTarget(){
		return contextMenu.getTarget();
	}

	private void setNumber(int number) {
		badge.removeAll();
		if(number > 0) {
			Label label = new Label(String.valueOf(number));
			label.getStyle().set("margin-top", "-3px");
			badge.add(label);
			badge.setVisible(true);
			setDisabled(false);
		}else {
			badge.setVisible(false);
			setDisabled(true);
		}
	}

	private void loadData() {
		contextMenu.removeAll();
		Set<NotificationBarElement> allCurrentUserNotification = notificationService.findAllCurrentUserNotification();
		Iterator<NotificationBarElement> iterator = allCurrentUserNotification.iterator();
		while (iterator.hasNext()) {
			NotificationBarElement barElement = iterator.next();
			MenuItem menuItem = contextMenu.addItem(createLabel(barElement.text), y -> {
				roleTranslator.refreshAuthzRolesAndGetRolesToUserViewContexts()
					.get(barElement.viewMode).stream()
					.filter(context -> barElement.resourceId.filter(resourceId -> resourceId.equals(context.id)).isPresent())
					.findAny()
					.ifPresent(FurmsViewUserContext::setAsCurrent);
				UI.getCurrent().navigate(barElement.redirect);
			});
			menuItem.setId("context-menu-item-size");
			if (iterator.hasNext())
				contextMenu.add(new Hr());
		}
		setNumber(allCurrentUserNotification.size());
	}

	private Label createLabel(String text) {
		Label label = new Label(text);
		label.getStyle().set("font-size", "var(--lumo-font-size-s)");
		return label;
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		UI ui = attachEvent.getUI();
		broadcasterRegistration = vaadinBroadcaster.register(
			VaadinListener.builder()
				.consumer(event -> ui.access(this::loadData))
				.predicate(this::isCurrentUserPoliciesAcceptanceListChanged)
				.orPredicate(this::isCurrentUserInvitationsListChanged)
				.orPredicate(this::isCurrentUserApplicationsListChanged)
				.build()
		);
	}

	private boolean isCurrentUserInvitationsListChanged(FurmsEvent furmsEvent) {
		if(!(furmsEvent instanceof UserInvitationsListChangedEvent))
			return false;
		UserInvitationsListChangedEvent event = (UserInvitationsListChangedEvent) furmsEvent;
		return currentUser.email.equals(event.email);
	}

	private boolean isCurrentUserApplicationsListChanged(FurmsEvent furmsEvent) {
		if(!(furmsEvent instanceof UserApplicationsListChangedEvent))
			return false;
		UserApplicationsListChangedEvent event = (UserApplicationsListChangedEvent) furmsEvent;
		return event.isTargetedAt(currentUser);
	}

	private boolean isCurrentUserPoliciesAcceptanceListChanged(FurmsEvent furmsEvent) {
		if(furmsEvent instanceof UserPoliciesListChangedEvent) {
			FenixUserId userId = ((UserPoliciesListChangedEvent) furmsEvent).fenixUserId;
			return currentUser.fenixUserId
				.filter(id -> id.equals(userId))
				.isPresent();
		}
		return false;
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		broadcasterRegistration.remove();
		broadcasterRegistration = null;
	}
}
