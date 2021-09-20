/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui;

import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.view_picker.FurmsRolePickerFactory;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FurmsLayoutFactory {
	private final FurmsRolePickerFactory furmsSelectFactory;
	private final FurmsNotificationBarFactory notificationBarFactory;

	FurmsLayoutFactory(FurmsRolePickerFactory furmsSelectFactory, FurmsNotificationBarFactory notificationBarFactory) {
		this.furmsSelectFactory = furmsSelectFactory;
		this.notificationBarFactory = notificationBarFactory;
	}

	public FurmsLayout create(List<MenuComponent> menuComponents){
		return new FurmsLayout(menuComponents, furmsSelectFactory.create(), notificationBarFactory.create());
	}

}
