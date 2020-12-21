/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user.unity;

import io.imunity.furms.core.config.security.user.resource.ResourceId;
import io.imunity.furms.core.config.security.user.role.Role;
import org.apache.commons.codec.binary.Base64;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.imunity.furms.core.config.security.user.role.Role.translateRole;
import static io.imunity.furms.core.config.security.user.unity.UnityGroupParser.getResourceId;
import static io.imunity.furms.core.config.security.user.unity.UnityGroupParser.usersGroupPredicate;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

//TODO all code below should be move to coming unity module
//TODO url and user data credential should be not hardcoded
@Service
public class UnityRoleLoader implements RoleLoader{

	private final RestOperations restOperations;

	public UnityRoleLoader(RestOperations restOperations) {
		this.restOperations = restOperations;
	}

	@Override
	public Map<ResourceId, Set<Role>> loadUserRoles(String sub){
		Map<String, List<Attribute>> attributes = loadUserAttributes(sub);
		return loadUserRoles(attributes);
	}

	private Map<ResourceId, Set<Role>> loadUserRoles(Map<String, List<Attribute>> attributes) {
		return attributes.values().stream()
			.flatMap(Collection::stream)
			.filter(usersGroupPredicate)
			.collect(
				groupingBy(
					attribute -> getResourceId(attribute.groupPath),
					mapping(attribute ->  translateRole(attribute.name, attribute.values.iterator().next()), toSet())
				)
			);
	}

	private Map<String, List<Attribute>> loadUserAttributes(String persistentId) {
		RequestEntity<Void> request = RequestEntity
			.get(getUrl(persistentId))
			.accept(MediaType.APPLICATION_JSON)
			.headers(createHeaders("a", "a"))
			.build();
		//TODO this is temporary solution. Currently rest client user is not created
		try {
			return restOperations.exchange(request, new ParameterizedTypeReference<Map<String, List<Attribute>>>() {})
				.getBody();
		}catch (HttpClientErrorException e){
			return emptyMap();
		}
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
