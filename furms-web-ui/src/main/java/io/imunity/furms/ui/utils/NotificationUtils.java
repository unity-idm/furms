/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class NotificationUtils {

	private static final int INFO_DURATION_MS = 3000;
	private static final int ERROR_DURATION_MS = 5000;
	
	public static void showErrorNotification(String message) {
		HorizontalLayout errorLayout = new HorizontalLayout(new Label(message));
		errorLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		Notification error = new Notification(errorLayout);
		error.setThemeName("error");
		error.setDuration(ERROR_DURATION_MS);
		setupNotification(error);
	}

	public static void showSuccessNotification(String message) {
		HorizontalLayout successLayout = new HorizontalLayout(new Label(message));
		successLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		Notification success = new Notification(successLayout);
		success.setThemeName("success");
		success.setDuration(INFO_DURATION_MS);
		setupNotification(success);
	}

	private static void setupNotification(Notification notification) {
		notification.setPosition(Position.TOP_CENTER);
		notification.setOpened(true);
	}

}
