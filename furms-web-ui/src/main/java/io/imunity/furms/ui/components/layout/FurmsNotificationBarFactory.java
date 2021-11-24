/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.layout;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.components.NotificationBarComponent;
import io.imunity.furms.ui.notifications.UINotificationService;
import io.imunity.furms.ui.user_context.RoleTranslator;
import org.springframework.stereotype.Component;

@Component
class FurmsNotificationBarFactory {
	private final VaadinBroadcaster vaadinBroadcaster;
	private final UINotificationService notificationService;
	private final AuthzService authzService;
	private final RoleTranslator roleTranslator;

	FurmsNotificationBarFactory(VaadinBroadcaster vaadinBroadcaster,
	                            UINotificationService notificationService,
	                            AuthzService authzService,
	                            RoleTranslator roleTranslator) {
		this.vaadinBroadcaster = vaadinBroadcaster;
		this.notificationService = notificationService;
		this.authzService = authzService;
		this.roleTranslator = roleTranslator;
	}

	com.vaadin.flow.component.Component create(){
		return new NotificationBarComponent(vaadinBroadcaster, notificationService, authzService.getCurrentAuthNUser(), roleTranslator)
			.getContextMenuTarget();
	}
}
