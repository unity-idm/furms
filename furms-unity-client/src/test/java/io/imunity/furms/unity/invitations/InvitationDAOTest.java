/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.invitations;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.unity.client.UnityClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.edu.icm.unity.types.registration.invite.RegistrationInvitationParam;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class InvitationDAOTest {

	@Mock
	private UnityClient unityClient;
	@Mock
	private InvitationFormIdResolver invitationFormIdResolver;
	@Mock
	private GroupResolver groupResolver;

	private InvitationDAOImpl invitationDAO;

	@BeforeEach
	void setUp() {
		invitationDAO = new InvitationDAOImpl(unityClient, invitationFormIdResolver, groupResolver);
	}

	@Test
	void shouldCreateInvitation() {
		//given
		ResourceId resourceId = new ResourceId(UUID.randomUUID(), ResourceType.SITE);
		Instant instant = Instant.now();
		Role fenixAdmin = Role.FENIX_ADMIN;

		RegistrationInvitationParam registrationInvitationParam = new RegistrationInvitationParam("fenixAdminForm", instant, "email");

		when(invitationFormIdResolver.getFormId(fenixAdmin)).thenReturn("formId");
		when(groupResolver.resolveGroup(resourceId, fenixAdmin)).thenReturn("group");
		when(unityClient.post(eq("/invitation"), any(), eq(Map.of()), eq(new ParameterizedTypeReference<String>(){}))).thenReturn("code");

		//when
		InvitationCode invitationCode = invitationDAO.createInvitation(resourceId, fenixAdmin, "email", instant);

		//then
		assertEquals("code", invitationCode.code);
	}

	@Test
	void shouldUpdateInvitation() {
		//given
		ResourceId resourceId = new ResourceId(UUID.randomUUID(), ResourceType.SITE);
		Instant instant = Instant.now();
		Role role = Role.SITE_SUPPORT;
		String email = "email";
		InvitationCode invitationCode = new InvitationCode("code");

		//when
		when(invitationFormIdResolver.getFormId(role)).thenReturn("formId");
		when(groupResolver.resolveGroup(resourceId, role)).thenReturn("group");

		invitationDAO.updateInvitation(resourceId, role, email, invitationCode, instant);

		//then
		verify(unityClient).put(eq("/invitation/code"), any());
	}

	@Test
	void shouldFindInvitationCode() {
		//given
		String registrationId = "registrationId";
		when(unityClient.get("/registrationRequest/" + registrationId, new ParameterizedTypeReference<Map<String, Object>>() {}))
			.thenReturn(Map.of("RegistrationCode", "code"));

		//when
		InvitationCode invitationCode = invitationDAO.findInvitationCode(registrationId);

		//then
		assertEquals(invitationCode.code, "code");
	}

	@Test
	void shouldSendInvitation() {
		//when
		invitationDAO.sendInvitation(new InvitationCode("code"));

		//then
		verify(unityClient).post("/invitation/code/send");
	}

	@Test
	void shouldRemoveInvitation() {
		//when
		invitationDAO.removeInvitation(new InvitationCode("code"));

		//then
		verify(unityClient).delete("/invitation/code", Map.of());
	}
}