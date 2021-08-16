/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.notifications;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UINotificationService {
	private final List<NotificationProducer> notificationProducers;

	UINotificationService(List<NotificationProducer> notificationProducers) {
		this.notificationProducers = notificationProducers;
	}

	public Set<NotificationBarElement> findAllCurrentUserNotification(){
		return notificationProducers.stream()
			.flatMap(NotificationProducer::findAllCurrentUserNotifications)
			.collect(Collectors.toSet());
	}
}
