/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.AttributeExt;
import pl.edu.icm.unity.types.basic.MultiGroupMembers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static io.imunity.furms.rest.admin.AcceptanceStatus.ACCEPTED;
import static io.imunity.furms.unity.common.UnityConst.FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE;
import static io.imunity.furms.unity.common.UnityConst.STRING;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

public class PolicyAcceptanceMockUtils {

	private final ObjectMapper objectMapper;
	private final WireMockServer server;

	public PolicyAcceptanceMockUtils(ObjectMapper objectMapper, WireMockServer server) {
		this.objectMapper = objectMapper;
		this.server = server;
	}

	public void createPolicyAcceptancesMock(String path, List<PolicyUser> policies) throws JsonProcessingException {
		final MultiGroupMembers multiGroupMembers = new MultiGroupMembers(
				policies.stream()
						.map(policy -> policy.user.getEntity())
						.collect(toSet()),
				Map.of(path, policies.stream()
						.map(policyUser -> createEntityGroupInformation(path, policyUser))
						.collect(toList())));

		server.stubFor(WireMock.post("/unity/group-members-multi/%2F")
				.withRequestBody(new ContainsPattern(path))
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(new ObjectMapper().writeValueAsString(multiGroupMembers))));
	}

	private MultiGroupMembers.EntityGroupAttributes createEntityGroupInformation(String path, PolicyUser policyAcceptance) {
		try {
			final List<AttributeExt> attributes = policyAcceptance.user.getAttributes().values().stream()
				.flatMap(Collection::stream)
				.map(attribute -> new AttributeExt(attribute, true))
				.collect(toList());
			attributes.add(new AttributeExt(new Attribute(
					FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE, STRING,
					path,
					List.of(objectMapper.writeValueAsString(new PolicyAcceptanceUnityMock(
							policyAcceptance.policyId,
							1,
							ACCEPTED.name(),
							LocalDateTime.now().toInstant(ZoneOffset.UTC))))),
					true));
			return new MultiGroupMembers.EntityGroupAttributes(policyAcceptance.user.getEntity().getEntityInformation().getId(), attributes);
		} catch (JsonProcessingException e) {
			return null;
		}
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
