/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.notifications;

import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.ui.views.user_settings.policy_documents.PolicyDocumentsView;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

@Component
class PolicyDocumentNotificationProducer implements NotificationProducer {
	private final PolicyDocumentService policyDocumentService;

	PolicyDocumentNotificationProducer(PolicyDocumentService policyDocumentService) {
		this.policyDocumentService = policyDocumentService;
	}

	@Override
	public Stream<NotificationBarElement> findAllCurrentUserNotifications() {
		return policyDocumentService.findAllByCurrentUser().stream()
			.filter(policyDocumentExtended -> policyDocumentExtended.utcAcceptedTime.isEmpty())
			.map(policyDocumentExtended ->
				new NotificationBarElement(getTranslation("notifications.new.policy", policyDocumentExtended), PolicyDocumentsView.class)
			);
	}
}
