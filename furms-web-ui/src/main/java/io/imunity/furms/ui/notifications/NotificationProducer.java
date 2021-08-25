/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.notifications;

import java.util.stream.Stream;

public interface NotificationProducer {
	Stream<NotificationBarElement> findAllCurrentUserNotifications();
}
