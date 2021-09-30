/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.invitations;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.spi.invitations.InvitationDAO;
import io.imunity.furms.unity.client.UnityClient;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.registration.GroupSelection;
import pl.edu.icm.unity.types.registration.invite.PrefilledEntry;
import pl.edu.icm.unity.types.registration.invite.PrefilledEntryMode;
import pl.edu.icm.unity.types.registration.invite.RegistrationInvitationParam;

@Service
class InvitationDAOImpl implements InvitationDAO {
	private final UnityClient unityClient;
	private final InvitationFormIdResolver invitationFormIdResolver;
	private final GroupResolver groupResolver;

	InvitationDAOImpl(UnityClient unityClient, InvitationFormIdResolver invitationFormIdResolver, GroupResolver groupResolver) {
		this.unityClient = unityClient;
		this.invitationFormIdResolver = invitationFormIdResolver;
		this.groupResolver = groupResolver;
	}

	@Override
	public InvitationCode createInvitation(ResourceId resourceId, Role role, String email, Instant expiration) {
		String group = groupResolver.resolveGroup(resourceId, role);
		String formId = invitationFormIdResolver.getFormId(role);
		RegistrationInvitationParam registrationInvitationParam = new RegistrationInvitationParam(formId, expiration, email);
		addMessageParameters(role, registrationInvitationParam);
		addGroupAndAttributes(role, group, registrationInvitationParam);
		String code = unityClient.post("/invitation", registrationInvitationParam, Map.of(), new ParameterizedTypeReference<>() {});
		return new InvitationCode(code);
	}

	private void addGroupAndAttributes(Role role, String group, RegistrationInvitationParam registrationInvitationParam) {
		registrationInvitationParam
			.getFormPrefill()
			.getGroupSelections()
			.put(0, new PrefilledEntry<>(new GroupSelection(group), PrefilledEntryMode.HIDDEN));
		registrationInvitationParam
			.getFormPrefill()
			.getAttributes()
			.put(0, new PrefilledEntry<>(new Attribute(role.unityRoleAttribute, "enumeration", group, List.of(role.unityRoleValue)), PrefilledEntryMode.HIDDEN));
	}

	private void addMessageParameters(Role role, RegistrationInvitationParam registrationInvitationParam) {
		registrationInvitationParam.getFormPrefill().getMessageParams().put("custom.role", role.toString().toLowerCase().replace("_", " "));
	}

	@Override
	public void updateInvitation(ResourceId resourceId, Role role, String email, InvitationCode code, Instant expiration) {
		String group = groupResolver.resolveGroup(resourceId, role);
		String formId = invitationFormIdResolver.getFormId(role);
		RegistrationInvitationParam registrationInvitationParam = new RegistrationInvitationParam(formId, expiration, email);
		addMessageParameters(role, registrationInvitationParam);
		addGroupAndAttributes(role, group, registrationInvitationParam);
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
