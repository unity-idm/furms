/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.notifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class EmailNotificationProperties {
	public final String newPolicyAcceptanceTemplateId;
	public final String newPolicyRevisionTemplateId;
	public final String furmsServerBaseURL;

	EmailNotificationProperties(
		@Value("${furms.notification.new-policy-template-id}") String newPolicyAcceptanceTemplateId,
		@Value("${furms.notification.new-policy-revision-template-id}") String newPolicyRevisionTemplateId,
		@Value("${furms.url}") String furmsServerBaseURL
	) {
		this.newPolicyAcceptanceTemplateId = newPolicyAcceptanceTemplateId;
		this.newPolicyRevisionTemplateId = newPolicyRevisionTemplateId;
		this.furmsServerBaseURL = furmsServerBaseURL;
	}
}
