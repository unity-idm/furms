/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui;

import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.components.branding.FurmsLogoFactory;
import io.imunity.furms.ui.view_picker.FurmsRolePickerFactory;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FurmsLayoutFactory {
	private final FurmsRolePickerFactory furmsSelectFactory;
	private final FurmsNotificationBarFactory notificationBarFactory;
	private final FurmsLogoFactory furmsLogoFactory;

	FurmsLayoutFactory(FurmsRolePickerFactory furmsSelectFactory,
	                   FurmsNotificationBarFactory notificationBarFactory,
	                   FurmsLogoFactory furmsLogoFactory) {
		this.furmsSelectFactory = furmsSelectFactory;
		this.notificationBarFactory = notificationBarFactory;
		this.furmsLogoFactory = furmsLogoFactory;
	}

	public FurmsLayout create(List<MenuComponent> menuComponents){
		return new FurmsLayout(
				menuComponents,
				furmsSelectFactory.create(),
				notificationBarFactory.create(),
				furmsLogoFactory::create);
	}

}
