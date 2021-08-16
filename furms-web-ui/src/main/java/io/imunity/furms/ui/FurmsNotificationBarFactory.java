/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.components.NotificationBarComponent;
import io.imunity.furms.ui.notifications.UINotificationService;
import org.springframework.stereotype.Component;

@Component
public class FurmsNotificationBarFactory {
	private final VaadinBroadcaster vaadinBroadcaster;
	private final UINotificationService notificationService;
	private final AuthzService authzService;

	FurmsNotificationBarFactory(VaadinBroadcaster vaadinBroadcaster, UINotificationService notificationService, AuthzService authzService) {
		this.vaadinBroadcaster = vaadinBroadcaster;
		this.notificationService = notificationService;
		this.authzService = authzService;
	}

	public com.vaadin.flow.component.Component create(){
		return new NotificationBarComponent(vaadinBroadcaster, notificationService, authzService.getCurrentAuthNUser())
			.getContextMenuTarget();
	}
}
