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
	public final String newInvitationTemplateId;
	public final String acceptedInvitationTemplateId;
	public final String rejectedInvitationTemplateId;
	public final String furmsServerBaseURL;

	EmailNotificationProperties(
		@Value("${furms.notification.new-policy-template-id}") String newPolicyAcceptanceTemplateId,
		@Value("${furms.notification.new-policy-revision-template-id}") String newPolicyRevisionTemplateId,
		@Value("${furms.notification.new-invitation-template-id}") String newInvitationTemplateId,
		@Value("${furms.notification.accepted-invitation-template-id}") String acceptedInvitationTemplateId,
		@Value("${furms.notification.rejected-invitation-template-id}") String rejectedInvitationTemplateId,
		@Value("${furms.url}") String furmsServerBaseURL
	) {
		this.newPolicyAcceptanceTemplateId = newPolicyAcceptanceTemplateId;
		this.newPolicyRevisionTemplateId = newPolicyRevisionTemplateId;
		this.newInvitationTemplateId = newInvitationTemplateId;
		this.acceptedInvitationTemplateId = acceptedInvitationTemplateId;
		this.rejectedInvitationTemplateId = rejectedInvitationTemplateId;
		this.furmsServerBaseURL = furmsServerBaseURL;
	}
}
