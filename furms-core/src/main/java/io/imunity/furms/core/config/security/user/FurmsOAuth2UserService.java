/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user;

import org.apache.commons.codec.binary.Base64;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.imunity.furms.core.config.security.user.Role.translateRole;
import static io.imunity.furms.core.config.security.user.UnityGroupParser.getResourceId;
import static java.util.stream.Collectors.*;

public class FurmsOAuth2UserService extends DefaultOAuth2UserService {
	private final RestOperations restOperations;

	public FurmsOAuth2UserService(RestOperations restOperations) {
		super.setRestOperations(restOperations);
		this.restOperations = restOperations;
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		String key = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
			.getUserNameAttributeName();
		String sub = oAuth2User.getAttribute("sub");
		Map<String, List<Attribute>> attributes = loadUserAttributes(sub);
		Map<FurmsRole, List<ResourceId>> roles = loadUserRoles(attributes);
		return new FurmsUserContext(oAuth2User, key, roles);
	}

	private Map<FurmsRole, List<ResourceId>> loadUserRoles(Map<String, List<Attribute>> attributes) {
		return attributes.values().stream()
			.flatMap(Collection::stream)
			.filter(x -> x.groupPath.contains("users"))
			.collect(
				groupingBy(
					attribute -> translateRole(attribute.name, attribute.values.iterator().next()),
					mapping(attribute -> getResourceId(attribute.groupPath), toList())
				)
			);
	}

	//TODO all code below should be move to coming unity module
	//TODO url and user data credential should be not hardcoded
	private Map<String, List<Attribute>> loadUserAttributes(String persistentId) {
		RequestEntity<Void> request = RequestEntity
			.get(getUrl(persistentId))
			.accept(MediaType.APPLICATION_JSON)
			.headers(createHeaders("a", "a"))
			.build();
		return restOperations.exchange(request, new ParameterizedTypeReference<Map<String, List<Attribute>>>() {})
			.getBody();
	}

	private URI getUrl(String userId) {
		try {
			return new URI("https://localhost:2443/rest-admin/v1/entity/" + userId + "/groups/attributes");
		} catch (URISyntaxException e) {
			throw new RuntimeException("This should not happened - URI is not correct", e);
		}
	}

	private HttpHeaders createHeaders(String username, String password){
		return new HttpHeaders() {{
			String auth = username + ":" + password;
			byte[] encodedAuth = Base64.encodeBase64(
				auth.getBytes(StandardCharsets.US_ASCII) );
			String authHeader = "Basic " + new String( encodedAuth );
			set( "Authorization", authHeader );
		}};
	}
}
