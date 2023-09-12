/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import io.imunity.rest.api.RestGroupMemberWithAttributes;
import io.imunity.rest.api.RestMultiGroupMembersWithAttributes;
import io.imunity.rest.api.types.basic.RestAttributeExt;
import io.imunity.rest.api.types.basic.RestEntityInformation;
import io.imunity.rest.api.types.basic.RestIdentity;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.EntityInformation;
import pl.edu.icm.unity.types.basic.Identity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static io.imunity.furms.rest.admin.AcceptanceStatus.ACCEPTED;
import static io.imunity.furms.unity.common.UnityConst.FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE;
import static io.imunity.furms.unity.common.UnityConst.STRING;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

public class PolicyAcceptanceMockUtils {

	private final ObjectMapper objectMapper;
	private final WireMockServer server;

	public PolicyAcceptanceMockUtils(ObjectMapper objectMapper, WireMockServer server) {
		this.objectMapper = objectMapper;
		this.server = server;
	}

	public void createPolicyAcceptancesMock(Map<String, List<PolicyUser>> pathWithPolicies) throws JsonProcessingException {
		RestMultiGroupMembersWithAttributes multiGroupMembers = RestMultiGroupMembersWithAttributes.builder()
				.withMembers(pathWithPolicies.entrySet().stream()
					.collect(
						toMap(
							Map.Entry::getKey,
							entry -> entry.getValue().stream()
								.map(policyUser -> createEntityGroupInformation(entry.getKey(), policyUser))
								.collect(toList())
					))).build();

		server.stubFor(WireMock.get(WireMock.urlPathEqualTo("/unity/multi-group-members-attributes/"))
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(new ObjectMapper().writeValueAsString(multiGroupMembers))));
	}

	private RestGroupMemberWithAttributes createEntityGroupInformation(String path, PolicyUser policyAcceptance) {
		try {
			final List<RestAttributeExt> attributes = policyAcceptance.user.getAttributes().values().stream()
				.flatMap(Collection::stream)
				.map(PolicyAcceptanceMockUtils::map)
				.collect(toList());
			attributes.add(map(new Attribute(
					FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE, STRING,
					path,
					List.of(objectMapper.writeValueAsString(new PolicyAcceptanceUnityMock(
							policyAcceptance.policyId,
							1,
							ACCEPTED.name(),
							LocalDateTime.now().toInstant(ZoneOffset.UTC)))))));
			return RestGroupMemberWithAttributes.builder()
				.withEntityInformation(map(policyAcceptance.user.getEntity().getEntityInformation()))
				.withIdentities(policyAcceptance.user.getEntity().getIdentities().stream().map(PolicyAcceptanceMockUtils::map).collect(toList()))
				.withAttributes(attributes)
				.build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	static RestAttributeExt map(Attribute attributeExt) {
		return RestAttributeExt.builder()
			.withName(attributeExt.getName())
			.withValueSyntax(attributeExt.getValueSyntax())
			.withDirect(true)
			.withGroupPath(attributeExt.getGroupPath())
			.withValues(attributeExt.getValues())
			.withTranslationProfile(attributeExt.getTranslationProfile())
			.withRemoteIdp(attributeExt.getRemoteIdp())
			.build();
	}

	static RestIdentity map(Identity identity) {
		return RestIdentity.builder()
			.withEntityId(identity.getEntityId())
			.withCreationTs(identity.getCreationTs())
			.withUpdateTs(identity.getUpdateTs())
			.withComparableValue(identity.getComparableValue())
			.withTypeId(identity.getTypeId())
			.withValue(identity.getValue())
			.withTarget(identity.getTarget())
			.withRealm(identity.getRealm())
			.build();
	}

	static RestEntityInformation map(EntityInformation entityInformation) {
		return RestEntityInformation.builder()
			.withEntityId(entityInformation.getId())
			.withState(
				Optional.ofNullable(entityInformation.getEntityState())
					.map(Enum::name)
					.orElse(null)
			)
			.withScheduledOperationTime(entityInformation.getScheduledOperationTime())
			.withScheduledOperation(
				Optional.ofNullable(entityInformation.getScheduledOperation())
					.map(Enum::name)
					.orElse(null)
			)
			.withRemovalByUserTime(entityInformation.getRemovalByUserTime())
			.build();
	}

	public static class PolicyUser {
		public final String policyId;
		public final TestUser user;

		public PolicyUser(String policyId, TestUser user) {
			this.policyId = policyId;
			this.user = user;
		}
	}

	public static class PolicyAcceptanceUnityMock {
		public final String policyDocumentId;
		public final int policyDocumentRevision;
		public final String acceptanceStatus;
		public final Instant decisionTs;

		public PolicyAcceptanceUnityMock(String policyDocumentId, int policyDocumentRevision, String acceptanceStatus, Instant decisionTs) {
			this.policyDocumentId = policyDocumentId;
			this.policyDocumentRevision = policyDocumentRevision;
			this.acceptanceStatus = acceptanceStatus;
			this.decisionTs = decisionTs;
		}
	}

}
