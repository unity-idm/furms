/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.components.NotificationBarComponent;
import io.imunity.furms.ui.notifications.NotificationService;
import org.springframework.stereotype.Component;

import io.imunity.furms.ui.components.FurmsSelect;
import io.imunity.furms.ui.user_context.RoleTranslator;

@Component
public class FurmsComponentFactory {
	private final RoleTranslator roleTranslator;
	private final VaadinBroadcaster vaadinBroadcaster;
	private final NotificationService notificationService;
	private final AuthzService authzService;

	public FurmsComponentFactory(RoleTranslator roleTranslator, VaadinBroadcaster vaadinBroadcaster,
	                             NotificationService notificationService, AuthzService authzService) {
		this.roleTranslator = roleTranslator;
		this.vaadinBroadcaster = vaadinBroadcaster;
		this.notificationService = notificationService;
		this.authzService = authzService;
	}

	public FurmsSelect createFurmsSelect(){
		return new FurmsSelect(roleTranslator, vaadinBroadcaster);
	}

	public com.vaadin.flow.component.Component createNotificationBar(){
		return new NotificationBarComponent(vaadinBroadcaster, notificationService, authzService.getCurrentAuthNUser())
			.getContextMenuTarget();
	}
}
