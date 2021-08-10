/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.shared.Registration;
import io.imunity.furms.domain.policy_documents.PolicyDocumentUpdatedEvent;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.VaadinListener;
import io.imunity.furms.ui.notifications.NotificationBarElement;
import io.imunity.furms.ui.notifications.NotificationService;

import java.util.Set;

import static com.vaadin.flow.component.icon.VaadinIcon.BELL;

@CssImport(value = "./styles/custom-lumo-theme.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class NotificationBarComponent extends ContextMenu {
	private final VaadinBroadcaster vaadinBroadcaster;
	private final NotificationService notificationService;
	private final Span badge;
	private Registration broadcasterRegistration;

	public NotificationBarComponent(VaadinBroadcaster vaadinBroadcaster, NotificationService notificationService) {
		this.vaadinBroadcaster = vaadinBroadcaster;
		this.notificationService = notificationService;
		Icon bell = BELL.create();
		badge = new Span();
		badge.getElement().setAttribute("theme","badge error primary small pill");

		Button button = new Button(new Div(bell, badge));
		setOpenOnClick(true);
		setTarget(button);
		loadData();
	}

	private void setNumber(int number) {
		Label label = new Label(String.valueOf(number));
		label.getStyle().set("margin-top", "-4px");
		badge.removeAll();
		badge.add(label);
	}

	private void loadData() {
		removeAll();
		Set<NotificationBarElement> allCurrentUserNotification = notificationService.findAllCurrentUserNotification();
		allCurrentUserNotification
			.forEach(x -> addItem(x.text, y -> UI.getCurrent().navigate(x.redirect)));
		setNumber(allCurrentUserNotification.size());
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		UI ui = attachEvent.getUI();
		broadcasterRegistration = vaadinBroadcaster.register(
			VaadinListener.builder()
				.consumer(event -> ui.access(this::loadData))
				.predicate(event -> event instanceof PolicyDocumentUpdatedEvent)
//				.orPredicate(event -> event instanceof SiteEvent)
				.build()
		);
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		broadcasterRegistration.remove();
		broadcasterRegistration = null;
	}
}
