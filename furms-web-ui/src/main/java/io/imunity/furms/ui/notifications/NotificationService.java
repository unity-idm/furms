/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.notifications;

import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.ui.views.user_settings.policy_documents.PolicyDocumentsView;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

@Service
public class NotificationService {
	private final PolicyDocumentService policyDocumentService;

	NotificationService(PolicyDocumentService policyDocumentService) {
		this.policyDocumentService = policyDocumentService;
	}

	public Set<NotificationBarElement> findAllCurrentUserNotification(){
		return Stream.of(
			findPolicyNotification()
		)
			.flatMap(identity())
			.collect(Collectors.toSet());
	}

	private Stream<NotificationBarElement> findPolicyNotification() {
		return policyDocumentService.findAllByCurrentUser().stream()
			.filter(x -> x.utcAcceptedTime.isEmpty())
			.map(x -> new NotificationBarElement("You have policy " + x.name + " to accept", PolicyDocumentsView.class));
	}
}
