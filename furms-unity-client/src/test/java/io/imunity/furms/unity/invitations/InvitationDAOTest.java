/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.invitations;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.unity.client.UnityClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.edu.icm.unity.types.registration.invite.RegistrationInvitationParam;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class InvitationDAOTest {

	@Mock
	private UnityClient unityClient;

	private InvitationDAOImpl invitationDAO;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		invitationDAO = new InvitationDAOImpl(unityClient, "url");
	}

	@Test
	void shouldCreateInvitation() {
		//given
		Instant instant = Instant.now();
		RegistrationInvitationParam registrationInvitationParam = new RegistrationInvitationParam("fenixAdminForm", instant, "email");
		registrationInvitationParam.getMessageParams().put("custom.role", Role.FENIX_ADMIN.toString().toLowerCase().replace("_", " "));
		registrationInvitationParam.getMessageParams().put("custom.unityUrl", "url");
		when(unityClient.post("/invitation", registrationInvitationParam, Map.of(), new ParameterizedTypeReference<String>(){})).thenReturn("code");

		//when
		InvitationCode invitationCode = invitationDAO.createInvitation("fenixAdminForm","email", instant, Role.FENIX_ADMIN);

		//then
		assertEquals("code", invitationCode.code);
	}

	@Test
	void shouldUpdateInvitation() {
		//given
		Instant instant = Instant.now();
		RegistrationInvitationParam registrationInvitationParam = new RegistrationInvitationParam("fenixAdminForm", instant, "email");
		registrationInvitationParam.getMessageParams().put("custom.role", Role.FENIX_ADMIN.toString().toLowerCase().replace("_", " "));
		registrationInvitationParam.getMessageParams().put("custom.unityUrl", "url");

		//when
		invitationDAO.updateInvitation("fenixAdminForm","email",  new InvitationCode("code"), instant, Role.FENIX_ADMIN);

		//then
		verify(unityClient).put("/invitation/code", registrationInvitationParam);
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