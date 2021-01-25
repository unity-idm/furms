/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import static com.vaadin.flow.component.notification.Notification.Position.TOP_END;

public class NotificationUtils {

	public static void showErrorNotification(String message) {
		HorizontalLayout errorLayout = new HorizontalLayout(VaadinIcon.WARNING.create(), new Label(message));
		errorLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		Notification error = new Notification(errorLayout);
		error.setThemeName("error");
		setupNotification(error);
	}

	public static void showSuccessNotification(String message) {
		HorizontalLayout successLayout = new HorizontalLayout(new Label(message));
		successLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		Notification success = new Notification(successLayout);
		success.setThemeName("success");
		setupNotification(success);
	}

	private static void setupNotification(Notification notification) {
		notification.setDuration(5000);
		notification.setPosition(TOP_END);
		notification.setOpened(true);
	}

}
