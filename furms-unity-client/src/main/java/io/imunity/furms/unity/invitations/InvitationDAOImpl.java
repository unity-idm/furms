/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.invitations;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.spi.invitations.InvitationDAO;
import io.imunity.furms.unity.client.UnityClient;
import org.springframework.beans.factory.annotation.Value;
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
	private final String unityUrl;

	InvitationDAOImpl(UnityClient unityClient, @Value("${furms.unity.url}") String unityUrl) {
		this.unityClient = unityClient;
		this.unityUrl = unityUrl;
	}

	@Override
	public InvitationCode createInvitation(String formId, String email, Instant expiration, Role role) {
		RegistrationInvitationParam registrationInvitationParam = new RegistrationInvitationParam(formId, expiration, email);
		addMessageParameters(role, registrationInvitationParam);
		String code = unityClient.post("/invitation", registrationInvitationParam, emptyMap(), new ParameterizedTypeReference<>() {});
		return new InvitationCode(code);
	}

	private void addMessageParameters(Role role, RegistrationInvitationParam registrationInvitationParam) {
		registrationInvitationParam.getMessageParams().put("custom.role", role.toString().toLowerCase().replace("_", " "));
		registrationInvitationParam.getMessageParams().put("custom.unityUrl", unityUrl);
	}

	@Override
	public void updateInvitation(String formId, String email, InvitationCode code, Instant expiration, Role role) {
		RegistrationInvitationParam registrationInvitationParam = new RegistrationInvitationParam(formId, expiration, email);
		addMessageParameters(role, registrationInvitationParam);
		unityClient.put("/invitation/" + code.code, registrationInvitationParam);
	}

	@Override
	public InvitationCode findInvitationCode(String registrationId) {
		String invitationCode = (String) unityClient.get("/registrationRequest/" + registrationId, new ParameterizedTypeReference<Map<String, Object>>() {})
			.get("RegistrationCode");
		return new InvitationCode(invitationCode);
	}

	@Override
	public void sendInvitation(InvitationCode code) {
		String path = UriComponentsBuilder.newInstance()
			.path("/invitation/{code}/send")
			.buildAndExpand(Map.of("code", code.code))
			.encode()
			.toUriString();
		unityClient.post(path);
	}

	@Override
	public void removeInvitation(InvitationCode code) {
		String path = UriComponentsBuilder.newInstance()
			.path("/invitation/{code}")
			.buildAndExpand(Map.of("code", code.code))
			.encode()
			.toUriString();
		unityClient.delete(path, Map.of());
	}
}
