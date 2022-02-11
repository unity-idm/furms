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
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.key.UserApiKey;
import io.imunity.furms.spi.users.api.key.UserApiKeyRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pl.edu.icm.unity.types.authn.CredentialInfo;
import pl.edu.icm.unity.types.authn.CredentialPublicInformation;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.Entity;
import pl.edu.icm.unity.types.basic.EntityInformation;
import pl.edu.icm.unity.types.basic.Identity;

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
import static pl.edu.icm.unity.types.authn.LocalCredentialState.correct;

public class TestUser {

	private final String userId;
	private final String fenixId;
	private final String apiKey;
	private final Map<String, Set<Attribute>> attributes;
	private final Entity entity;
	private final FURMSUser furmsUser;

	public TestUser(String userId, String apiKey, int entityId) {
		this.userId = userId;
		this.fenixId = format("%s@fenix", userId);
		this.apiKey = apiKey;
		this.attributes = new HashMap<>();
		this.attributes.put("/", new HashSet<>(Set.of(
				new Attribute("sys:AuthorizationRole", ENUMERATION, "/", List.of("Regular User")),
				new Attribute("firstname", STRING, "/", List.of("Ahsoka")),
				new Attribute("surname", STRING, "/", List.of("Thano")),
				new Attribute("email", STRING, "/", List.of("{\"value\":\"jedi_office@domain.com\"," +
						"\"confirmationData\":{\"confirmed\":false,\"confirmationDate\":0,\"sentRequestAmount\":0},\"tags\":[]}")))));
		this.attributes.put("/fenix/users", new HashSet<>(Set.of(
				new Attribute("sys:AuthorizationRole", ENUMERATION, "/fenix/users", List.of("Regular User")))));
		this.entity = new Entity(
				new ArrayList<>(List.of(
						new Identity(PERSISTENT_IDENTITY, userId, entityId, userId),
						new Identity(IDENTIFIER_IDENTITY, userId, entityId, fenixId),
						new Identity("username", userId, entityId, userId))),
				new EntityInformation(entityId),
				new CredentialInfo("sys:all", Map.of(
						"userPassword", new CredentialPublicInformation(correct, ""),
						"sys:password", new CredentialPublicInformation(correct, ""),
						"clientPassword", new CredentialPublicInformation(correct, ""))));
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

	public Entity getEntity() {
		return entity;
	}

	public Map<String, Set<Attribute>> getAttributes() {
		return attributes;
	}

	public void addSiteAdmin(String siteId) {
		addRole("/fenix/sites/"+siteId+"/users", SITE_ADMIN);
	}

	public void addSiteSupport(String siteId) {
		addRole("/fenix/sites/"+siteId+"/users", SITE_SUPPORT);
	}

	public void addProjectAdmin(String communityId, String projectId) {
		addRole("/fenix/communities/"+communityId+"/projects/"+projectId+"/users", PROJECT_ADMIN);
	}

	public void addProjectUser(String communityId, String projectId) {
		addRole("/fenix/communities/"+communityId+"/projects/"+projectId+"/users", PROJECT_USER);
	}

	public void addCommunityAdmin(String communityId) {
		addRole("/fenix/communities/"+communityId+"/users", COMMUNITY_ADMIN);
	}

	public void addFenixAdminRole() {
		addRole("/", FENIX_ADMIN);
		addRole("/fenix/users", FENIX_ADMIN);
	}

	public void addRole(String path, Role role) {
		final Attribute attribute = new Attribute(role.unityRoleAttribute, ENUMERATION, path, List.of(role.unityRoleValue));
		if (attributes.get(path) != null) {
			attributes.get(path).add(attribute);
		} else {
			attributes.put(path, new HashSet<>(Set.of(attribute)));
		}
	}

	public Map<ResourceId, Set<Role>> getRoles() {
		return attributes.values().stream()
				.flatMap(Collection::stream)
				.filter(attribute -> attribute.getGroupPath().endsWith("/users"))
				.collect(groupingBy(
						attribute -> getResourceId(attribute.getGroupPath()),
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
		entity.getIdentities().stream()
				.filter(identity -> identity.getTypeId().equals(IDENTIFIER_IDENTITY)
												&& identity.getComparableValue().equals(fenixId))
				.findFirst()
				.ifPresent(identity -> {
					try {
						entity.getIdentities().remove(identity);
						registerUserMock(wireMockServer);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				});
	}

	private void createAttributeMock(WireMockServer wireMockServer) throws JsonProcessingException {
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

	private Stream<Role> attr2Role(Attribute attribute) {
		return attribute.getValues().stream()
				.map(value -> translateRole(attribute.getName(), value))
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
