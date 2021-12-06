/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.layout;

import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.components.branding.logo.FurmsLogoFactory;
import io.imunity.furms.ui.view_picker.FurmsRolePickerFactory;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FurmsAppLayoutComponentsFactory {
	private final FurmsRolePickerFactory furmsSelectFactory;
	private final FurmsNotificationBarFactory notificationBarFactory;
	private final FurmsLogoFactory furmsLogoFactory;

	FurmsAppLayoutComponentsFactory(FurmsRolePickerFactory furmsSelectFactory,
	                                FurmsNotificationBarFactory notificationBarFactory,
	                                FurmsLogoFactory furmsLogoFactory) {
		this.furmsSelectFactory = furmsSelectFactory;
		this.notificationBarFactory = notificationBarFactory;
		this.furmsLogoFactory = furmsLogoFactory;
	}

	FurmsAppLayoutComponentsHolder create(List<MenuComponent> menuComponents){
		return new FurmsAppLayoutComponentsHolder(
				menuComponents,
				furmsSelectFactory.create(),
				notificationBarFactory.create(),
				furmsLogoFactory::create);
	}

}
