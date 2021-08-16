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
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.shared.Registration;
import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.policy_documents.UserPendingPoliciesChangedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.VaadinListener;
import io.imunity.furms.ui.notifications.NotificationBarElement;
import io.imunity.furms.ui.notifications.UINotificationService;

import java.util.Set;

import static com.vaadin.flow.component.icon.VaadinIcon.BELL;

@CssImport(value = "./styles/custom-lumo-theme.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class NotificationBarComponent extends Button {
	private final VaadinBroadcaster vaadinBroadcaster;
	private final UINotificationService notificationService;
	private final FURMSUser currentUser;
	private final Span badge;
	private final ContextMenu contextMenu;
	private Registration broadcasterRegistration;

	public NotificationBarComponent(VaadinBroadcaster vaadinBroadcaster, UINotificationService notificationService, FURMSUser furmsUser) {
		this.vaadinBroadcaster = vaadinBroadcaster;
		this.notificationService = notificationService;
		this.currentUser = furmsUser;

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
		allCurrentUserNotification
			.forEach(x -> contextMenu.addItem(x.text, y -> UI.getCurrent().navigate(x.redirect)));
		setNumber(allCurrentUserNotification.size());
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		UI ui = attachEvent.getUI();
		broadcasterRegistration = vaadinBroadcaster.register(
			VaadinListener.builder()
				.consumer(event -> ui.access(this::loadData))
				.predicate(this::isCurrentUserPoliciesAcceptanceListChanged)
				.build()
		);
	}

	private boolean isCurrentUserPoliciesAcceptanceListChanged(FurmsEvent furmsEvent) {
		if(!(furmsEvent instanceof UserPendingPoliciesChangedEvent))
			return false;
		UserPendingPoliciesChangedEvent event = (UserPendingPoliciesChangedEvent) furmsEvent;
		return currentUser.fenixUserId
			.filter(id -> id.equals(event.fenixUserId))
			.isPresent();
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		broadcasterRegistration.remove();
		broadcasterRegistration = null;
	}
}
