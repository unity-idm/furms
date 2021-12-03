/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.notifications;

import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.unity.client.users.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EmailNotificationSenderTest {
	private static final String FURMS_BASE_URL = "https://localhost:333";
	private static final String POLICY_DOCUMENT_BASE_URL = FURMS_BASE_URL + "/front/users/settings/policy/documents";
	@Mock
	private UserService userService;
	@Mock
	private PolicyDocumentDAO policyDocumentDAO;

	private EmailNotificationSenderImpl emailNotificationDAO;

	@BeforeEach
	void setUp() {
		EmailNotificationProperties emailNotificationProperties = new EmailNotificationProperties(
			"policyAcceptanceNew",
			"policyAcceptanceRevision",
			"newInvitation",
			"acceptedInvitation",
			"rejectedInvitation",
			"newApplication",
			"acceptedApplication",
			"rejectedApplication",
			"resourceUsageAlarm",
			"resourceUsageAlarmWithoutUrl",
				FURMS_BASE_URL);
		emailNotificationDAO = new EmailNotificationSenderImpl(userService, emailNotificationProperties);
	}

	@Test
	void shouldNotifyAboutChangedPolicy() {
		PersistentId id = new PersistentId(UUID.randomUUID().toString());
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		String siteId = "siteId";
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.siteId(siteId)
			.name("policyName")
			.build();

		FURMSUser user = FURMSUser.builder()
			.id(id)
			.fenixUserId("fenixUserId")
			.email("email")
			.build();

		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.build();

		UserPolicyAcceptances userPolicyAcceptances = new UserPolicyAcceptances(user, Set.of(policyAcceptance));

		when(policyDocumentDAO.getUserPolicyAcceptances(siteId))
			.thenReturn(Set.of(userPolicyAcceptances));

		emailNotificationDAO.notifyAboutChangedPolicy(id, policyDocument.name);

		verify(userService).sendUserNotification(id, "policyAcceptanceRevision",
			Map.of("custom.name", "policyName",
				"custom.furmsUrl", POLICY_DOCUMENT_BASE_URL)
		);
	}

	@Test
	void shouldNotNotifyAboutChangedPolicy() {
		PersistentId id = new PersistentId(UUID.randomUUID().toString());
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		String siteId = "siteId";
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.siteId(siteId)
			.name("policyName")
			.build();

		FURMSUser user = FURMSUser.builder()
			.id(id)
			.fenixUserId("fenixUserId")
			.email("email")
			.build();

		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(new PolicyId(UUID.randomUUID()))
			.build();

		UserPolicyAcceptances userPolicyAcceptances = new UserPolicyAcceptances(user, Set.of(policyAcceptance));

		when(policyDocumentDAO.getUserPolicyAcceptances(siteId))
			.thenReturn(Set.of(userPolicyAcceptances));

		emailNotificationDAO.notifyAboutChangedPolicy(id, policyDocument.name);

		verify(userService, times(0)).sendUserNotification(id, "policyAcceptanceRevision", Map.of("custom.name", "policyName", "custom.furmsUrl", FURMS_BASE_URL));
	}

	@Test
	void shouldNotifyAboutNewPolicy() {
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		PersistentId id = new PersistentId(UUID.randomUUID().toString());

		when(userService.getPersistentId(fenixUserId)).thenReturn(id);

		emailNotificationDAO.notifyAboutNotAcceptedPolicy(fenixUserId,"policyName");

		verify(userService).sendUserNotification(id, "policyAcceptanceNew",
			Map.of("custom.name", "policyName",
				"custom.furmsUrl", POLICY_DOCUMENT_BASE_URL)
		);
	}

	@Test
	void shouldNotifyAboutNewSitePolicyAndServicePolicy() {
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		PersistentId id = new PersistentId(UUID.randomUUID().toString());

		when(userService.getPersistentId(fenixUserId)).thenReturn(id);

		emailNotificationDAO.notifyAboutNotAcceptedPolicy(fenixUserId,"sitePolicyName");

		verify(userService).sendUserNotification(id, "policyAcceptanceNew",
			Map.of("custom.name", "sitePolicyName",
				"custom.furmsUrl", POLICY_DOCUMENT_BASE_URL)
		);
	}

	@Test
	void shouldNotifyProjectAdminAboutResourceUsage() {
		PersistentId id = new PersistentId(UUID.randomUUID().toString());

		emailNotificationDAO.notifyProjectAdminAboutResourceUsage(id, "projectId", "projectAllocationId","projectAllocationName", "alarmName");

		verify(userService).sendUserNotification(id, "resourceUsageAlarm",
			Map.of("custom.projectAllocationName", "projectAllocationName",
				"custom.alarmName", "alarmName",
				"custom.furmsUrl", FURMS_BASE_URL + "/front/project/admin/resource/allocations/details/" + "projectAllocationId?resourceId=projectId"
			)
		);
	}

	@Test
	void shouldNotifyProjectUserAboutResourceUsage() {
		PersistentId id = new PersistentId(UUID.randomUUID().toString());

		emailNotificationDAO.notifyProjectUserAboutResourceUsage(id, "projectId", "projectAllocationId","projectAllocationName", "alarmName");

		verify(userService).sendUserNotification(id, "resourceUsageAlarm",
			Map.of("custom.projectAllocationName", "projectAllocationName",
				"custom.alarmName", "alarmName",
				"custom.furmsUrl", FURMS_BASE_URL + "/front/users/settings/project/projectId"
			)
		);
	}

	@Test
	void shouldNotifyUserAboutResourceUsage() {
		PersistentId id = new PersistentId(UUID.randomUUID().toString());

		emailNotificationDAO.notifyUserAboutResourceUsage(id, "projectId", "projectAllocationId","projectAllocationName", "alarmName");

		verify(userService).sendUserNotification(id, "resourceUsageAlarmWithoutUrl",
			Map.of("custom.projectAllocationName", "projectAllocationName",
				"custom.alarmName", "alarmName"
			)
		);
	}

	@Test
	void shouldNotNotifyAboutNewPolicy() {
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		PersistentId id = new PersistentId(UUID.randomUUID().toString());

		emailNotificationDAO.notifyAboutNotAcceptedPolicy(fenixUserId, "grantId");

		verify(userService, times(0)).sendUserNotification(id, "policyAcceptanceNew", Map.of("custom.name", "policyName", "custom.furmsUrl", FURMS_BASE_URL));
	}

	@Test
	void shouldNotifySiteUserAboutPolicyAssignmentChange() {
		//given
		final FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		final PersistentId id = new PersistentId(UUID.randomUUID().toString());

		when(userService.getPersistentId(fenixUserId)).thenReturn(id);

		//when
		emailNotificationDAO.notifySiteUserAboutPolicyAssignmentChange(fenixUserId, "name");

		verify(userService, times(1))
				.sendUserNotification(id, "policyAcceptanceNew",
						Map.of("custom.name", "name",
								"custom.furmsUrl", POLICY_DOCUMENT_BASE_URL));
	}
}