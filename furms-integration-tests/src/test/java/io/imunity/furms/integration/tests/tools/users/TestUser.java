/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.tools.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.key.UserApiKey;
import io.imunity.furms.spi.users.api.key.UserApiKeyRepository;
import io.imunity.rest.api.types.authn.RestCredentialInfo;
import io.imunity.rest.api.types.authn.RestCredentialPublicInformation;
import io.imunity.rest.api.types.basic.RestAttribute;
import io.imunity.rest.api.types.basic.RestEntity;
import io.imunity.rest.api.types.basic.RestEntityInformation;
import io.imunity.rest.api.types.basic.RestIdentity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static io.imunity.furms.domain.authz.roles.Role.COMMUNITY_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.FENIX_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.PROJECT_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.PROJECT_USER;
import static io.imunity.furms.domain.authz.roles.Role.SITE_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.SITE_SUPPORT;
import static io.imunity.furms.domain.authz.roles.Role.translateRole;
import static io.imunity.furms.unity.client.UnityGroupParser.getResourceId;
import static io.imunity.furms.unity.common.UnityConst.ENUMERATION;
import static io.imunity.furms.unity.common.UnityConst.IDENTIFIER_IDENTITY;
import static io.imunity.furms.unity.common.UnityConst.PERSISTENT_IDENTITY;
import static io.imunity.furms.unity.common.UnityConst.STRING;
import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

public class TestUser {

	private final String userId;
	private final String fenixId;
	private final String apiKey;
	private final Map<String, Set<RestAttribute>> attributes;
	private RestEntity entity;
	private final FURMSUser furmsUser;

	public TestUser(String userId, String apiKey, int entityId) {
		this.userId = userId;
		this.fenixId = format("%s@fenix", userId);
		this.apiKey = apiKey;
		this.attributes = new HashMap<>();
		this.attributes.put("/", new HashSet<>(Set.of(
			RestAttribute.builder()
				.withName("sys:AuthorizationRole")
				.withValueSyntax(ENUMERATION)
				.withGroupPath("/")
				.withValues(List.of("Regular User"))
				.build(),
			RestAttribute.builder()
				.withName("firstname")
				.withValueSyntax(STRING)
				.withGroupPath("/")
				.withValues(List.of("Ahsoka"))
				.build(),
			RestAttribute.builder()
				.withName("surname")
				.withValueSyntax(STRING)
				.withGroupPath("/")
				.withValues(List.of("Thano"))
				.build(),
			RestAttribute.builder()
				.withName("email")
				.withValueSyntax(STRING)
				.withGroupPath("/")
				.withValues(List.of("{\"value\":\"jedi_office@domain.com\"," +
					"\"confirmationData\":{\"confirmed\":false,\"confirmationDate\":0,\"sentRequestAmount\":0},\"tags\":[]}"))
				.build()
		)));
		this.attributes.put("/fenix/users", new HashSet<>(Set.of(
			RestAttribute.builder()
				.withName("sys:AuthorizationRole")
				.withValueSyntax(ENUMERATION)
				.withGroupPath("/fenix/users")
				.withValues(List.of("Regular User"))
				.build())));
		this.entity = RestEntity.builder()
			.withIdentities(new ArrayList<>(List.of(
				RestIdentity.builder()
					.withTypeId(PERSISTENT_IDENTITY)
					.withValue(userId)
					.withEntityId(entityId)
					.withComparableValue(userId)
					.build(),
				RestIdentity.builder()
					.withTypeId(IDENTIFIER_IDENTITY)
					.withValue(userId)
					.withEntityId(entityId)
					.withComparableValue(fenixId)
					.build(),
				RestIdentity.builder()
					.withTypeId("username")
					.withValue(userId)
					.withEntityId(entityId)
					.withComparableValue(userId)
					.build()
			)))
			.withEntityInformation(RestEntityInformation.builder()
				.withEntityId(Integer.valueOf(entityId).longValue())
				.withState("valid")
				.build())
			.withCredentialInfo(RestCredentialInfo.builder()
				.withCredentialsState(Map.of(
					"userPassword", RestCredentialPublicInformation.builder()
							.withState("correct")
							.withExtraInformation("")
							.build(),
					"sys:password", RestCredentialPublicInformation.builder()
						.withState("correct")
						.withExtraInformation("")
						.build(),
					"clientPassword", RestCredentialPublicInformation.builder()
						.withState("correct")
						.withExtraInformation("")
						.build()))
				.build())
			.build();
		this.furmsUser = FURMSUser.builder()
				.id(new PersistentId("user"))
				.firstName("firstName")
				.lastName("lastName")
				.email("email@domain.com")
				.fenixUserId("fenixUserId")
				.build();
	}

	public String getUserId() {
		return userId;
	}

	public String getFenixId() {
		return fenixId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public RestEntity getEntity() {
		return entity;
	}

	public Map<String, Set<RestAttribute>> getAttributes() {
		return attributes;
	}

	public void addSiteAdmin(SiteId siteId) {
		addRole("/fenix/sites/"+siteId.id+"/users", SITE_ADMIN);
	}

	public void addSiteSupport(SiteId siteId) {
		addRole("/fenix/sites/"+siteId.id+"/users", SITE_SUPPORT);
	}

	public void addProjectAdmin(CommunityId communityId, ProjectId projectId) {
		addRole("/fenix/communities/"+communityId.id+"/projects/"+projectId.id+"/users", PROJECT_ADMIN);
	}

	public void addProjectUser(CommunityId communityId, ProjectId projectId) {
		addRole("/fenix/communities/"+communityId.id+"/projects/"+projectId.id+"/users", PROJECT_USER);
	}

	public void addCommunityAdmin(CommunityId communityId) {
		addRole("/fenix/communities/"+communityId.id+"/users", COMMUNITY_ADMIN);
	}

	public void addFenixAdminRole() {
		addRole("/", FENIX_ADMIN);
		addRole("/fenix/users", FENIX_ADMIN);
	}

	public void addRole(String path, Role role) {
		final RestAttribute attribute =
			RestAttribute.builder()
				.withName(role.unityRoleAttribute)
				.withValueSyntax(ENUMERATION)
				.withGroupPath(path)
				.withValues(List.of(role.unityRoleValue))
				.build();
		if (attributes.get(path) != null) {
			attributes.get(path).add(attribute);
		} else {
			attributes.put(path, new HashSet<>(Set.of(attribute)));
		}
	}

	public Map<ResourceId, Set<Role>> getRoles() {
		return attributes.values().stream()
				.flatMap(Collection::stream)
				.filter(attribute -> attribute.groupPath.endsWith("/users"))
				.collect(groupingBy(
						attribute -> getResourceId(attribute.groupPath),
						flatMapping(this::attr2Role, mapping(identity(), toSet()))
				))
				.entrySet().stream()
				.filter(resourceEntry -> !resourceEntry.getValue().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public RequestPostProcessor getHttpBasic() {
		return httpBasic(userId, apiKey);
	}

	public void setupUser(WireMockServer wireMockServer, UserApiKeyRepository repository) throws JsonProcessingException {
		registerApiKey(repository);
		registerUserMock(wireMockServer);
	}

	public void registerApiKey(UserApiKeyRepository repository) {
		repository.create(UserApiKey.builder()
				.userId(new PersistentId(userId))
				.apiKey(UUID.fromString(apiKey))
				.build());
	}

	public void registerUserMock(WireMockServer wireMockServer) throws JsonProcessingException {
		createAttributeMock(wireMockServer);
		createEntityMock(wireMockServer);
		createFenixAttributeMock(wireMockServer);
		createFenixIdentifierMock(wireMockServer);
	}

	public void registerInSecurityContext() {
		final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				new TestUserProvider(furmsUser, getRoles()), null, Set.of());

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	public void removeFromSecurityContext() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	public void disableCentralIDPIdentity(WireMockServer wireMockServer) {
		entity.identities.stream()
				.filter(identity -> identity.typeId.equals(IDENTIFIER_IDENTITY)
												&& identity.comparableValue.equals(fenixId))
				.findFirst()
				.ifPresent(identity -> {
					try {
						entity =RestEntity.builder()
							.withCredentialInfo(entity.credentialInfo)
							.withEntityInformation(entity.entityInformation)
							.withIdentities(entity.identities.stream()
								.filter(id -> !id.equals(identity))
								.collect(Collectors.toList()))
							.build();
						registerUserMock(wireMockServer);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				});
	}

	private void createAttributeMock(WireMockServer wireMockServer) throws JsonProcessingException {
		wireMockServer.stubFor(get("/unity/entity/"+userId+"/groups/direct/attributes")
			.willReturn(aResponse().withStatus(200)
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(new ObjectMapper().writeValueAsString(attributes.values().stream()
					.flatMap(Collection::stream)
					.collect(Collectors.groupingBy(attr -> attr.groupPath))))));
		wireMockServer.stubFor(get("/unity/entity/"+userId+"/attributes?group=/")
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(new ObjectMapper().writeValueAsString(attributes.values().stream()
								.flatMap(Collection::stream)
								.collect(Collectors.toSet())))));
	}

	private void createEntityMock(WireMockServer wireMockServer) throws JsonProcessingException {
		wireMockServer.stubFor(get("/unity/entity/"+userId+"?identityType=persistent")
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(new ObjectMapper().writeValueAsString(entity))));
		wireMockServer.stubFor(get("/unity/entity/"+fenixId+"?identityType=identifier")
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(new ObjectMapper().writeValueAsString(entity))));
	}

	private void createFenixAttributeMock(WireMockServer wireMockServer) throws JsonProcessingException {
		wireMockServer.stubFor(get("/unity/entity/"+userId+"/groups/attributes?groupsPatterns=/fenix/**/users")
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(new ObjectMapper().writeValueAsString(attributes))));
	}

	private void createFenixIdentifierMock(WireMockServer wireMockServer) throws JsonProcessingException {
		wireMockServer.stubFor(get("/unity/entity/"+fenixId+"/attributes?identityType=identifier&group=/")
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(new ObjectMapper().writeValueAsString(attributes.values().stream()
								.flatMap(Collection::stream)
								.collect(Collectors.toSet())))));
	}

	private Stream<Role> attr2Role(RestAttribute attribute) {
		return attribute.values.stream()
				.map(value -> translateRole(attribute.name, value))
				.filter(Optional::isPresent)
				.map(Optional::get);
	}

	@Override
	public String toString() {
		return "{" +
				"userId='" + userId + '\'' +
				", roles=" + getRoles() +
				'}';
	}
}
