/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.invitations;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.spi.invitations.InvitationDAO;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.rest.api.types.basic.RestAttribute;
import io.imunity.rest.api.types.registration.RestGroupSelection;
import io.imunity.rest.api.types.registration.invite.RestFormPrefill;
import io.imunity.rest.api.types.registration.invite.RestPrefilledEntry;
import io.imunity.rest.api.types.registration.invite.RestRegistrationInvitationParam;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
		RestRegistrationInvitationParam registrationInvitationParam = RestRegistrationInvitationParam.builder()
			.withFormPrefill(RestFormPrefill.builder()
				.withFormId(formId)
				.withMessageParams(getMessageParameters(role))
				.withGroupSelections(Map.of(0, getGroupSelections(group)))
				.withAttributes(Map.of(0, getAttributes(role, group)))
				.build()
			)
			.withExpiration(expiration.toEpochMilli())
			.withContactAddress(email)
			.build();
		String code = unityClient.post("/invitation", registrationInvitationParam, Map.of(), new ParameterizedTypeReference<>() {});
		return new InvitationCode(code);
	}

	private RestPrefilledEntry<RestGroupSelection> getGroupSelections(String group) {
		return new RestPrefilledEntry.Builder<RestGroupSelection>()
				.withEntry(RestGroupSelection.builder().withSelectedGroups(List.of(group)).build())
				.withMode("HIDDEN")
				.build();
	}

	private RestPrefilledEntry<RestAttribute> getAttributes(Role role, String group) {
		return new RestPrefilledEntry.Builder<RestAttribute>()
				.withEntry(RestAttribute.builder()
				.withName(role.unityRoleAttribute)
				.withValueSyntax("enumeration")
				.withGroupPath(group)
				.withValues(List.of(role.unityRoleValue))
				.build()
				)
			.withMode("HIDDEN")
			.build();
	}

	private Map<String, String> getMessageParameters(Role role) {
		return Map.of("custom.role", role.toString().toLowerCase().replace("_", " "));
	}

	@Override
	public void updateInvitation(ResourceId resourceId, Role role, String email, InvitationCode code, Instant expiration) {
		String group = groupResolver.resolveGroup(resourceId, role);
		String formId = invitationFormIdResolver.getFormId(role);
		RestRegistrationInvitationParam registrationInvitationParam = RestRegistrationInvitationParam.builder()
			.withFormPrefill(RestFormPrefill.builder()
				.withFormId(formId)
				.withMessageParams(getMessageParameters(role))
				.withGroupSelections(Map.of(0, getGroupSelections(group)))
				.withAttributes(Map.of(0, getAttributes(role, group)))
				.build()
			)
			.withExpiration(expiration.toEpochMilli())
			.withContactAddress(email)
			.build();
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
