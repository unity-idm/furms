/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.invitations;

import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.spi.invitations.InvitationDAO;
import io.imunity.furms.unity.client.UnityClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.registration.invite.RegistrationInvitationParam;

import java.time.Instant;
import java.util.Map;

import static java.util.Collections.emptyMap;

@Service
class InvitationDAOImpl implements InvitationDAO {
	private final UnityClient unityClient;

	InvitationDAOImpl(UnityClient unityClient) {
		this.unityClient = unityClient;
	}
	@Override
	public String createInvitation(String email, Instant expiration) {
		RegistrationInvitationParam fenixAdminForm = new RegistrationInvitationParam("fenixAdminForm", expiration, email);
		return unityClient.post("/invitation", fenixAdminForm, emptyMap(), new ParameterizedTypeReference<>(){});
	}

	@Override
	public InvitationCode findInvitationCode(String registrationId) {
		String invitationCode = (String) unityClient.get("/registrationRequest/" + registrationId, new ParameterizedTypeReference<Map<String, Object>>() {
		})
			.get("RegistrationCode");
		return new InvitationCode(invitationCode);
	}

	@Override
	public void sendInvitation(String code) {
		String path = UriComponentsBuilder.newInstance()
			.path("/invitation/{code}/send")
			.buildAndExpand(Map.of("code", code))
			.encode()
			.toUriString();
		unityClient.post(path);
	}

	@Override
	public void removeInvitation(String code) {
		String path = UriComponentsBuilder.newInstance()
			.path("/invitation/{code}")
			.buildAndExpand(Map.of("code", code))
			.encode()
			.toUriString();
		unityClient.delete(path, Map.of());
	}
}
