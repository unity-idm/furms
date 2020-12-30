/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.core.config.security.user.FurmsUserContext;
import io.imunity.furms.domain.roles.ResourceId;
import io.imunity.furms.domain.roles.Role;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static io.imunity.furms.domain.roles.ResourceType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
public class AuthTest {

	@Autowired
	private ServiceMock mockService;

	@MockBean
	AuthenticationManager authenticationManager;

	@Mock
	Authentication authentication;
	@Mock
	OAuth2User oAuth2User;

	Map<ResourceId, Set<Role>> roles = new HashMap<>();

	@BeforeAll
	public void configSecurityContext(){
		Map<String, Object> attributes = Map.of("name", "aaa");
		OAuth2UserAuthority oAuth2UserAuthority = new OAuth2UserAuthority(attributes);
		Collection<GrantedAuthority> grantedAuthorities = List.of(oAuth2UserAuthority);

		Mockito.<Collection<? extends GrantedAuthority>>when(oAuth2User.getAuthorities())
			.thenReturn(grantedAuthorities);
		when(oAuth2User.getAttributes()).thenReturn(attributes);

		FurmsUserContext furmsUserContext = new FurmsUserContext(oAuth2User, "name", roles);
		when(authentication.getPrincipal()).thenReturn(furmsUserContext);
		when(authentication.isAuthenticated()).thenReturn(true);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@BeforeEach
	public void clearContext(){
		roles.clear();
	}

	@Test
	public void authShouldPassFenixAdminHasAccessToAPPLevel(){
		roles.put(new ResourceId((String) null, APP_LEVEL), Set.of(Role.FENIX_ADMIN));

		Throwable throwable = catchThrowable(() -> mockService.findAll());
		assertThat(throwable).isNull();
	}

	@Test
	public void authShouldPassFenixAdminHasAccessToResource(){
		UUID uuid = UUID.randomUUID();
		roles.put(new ResourceId(uuid, APP_LEVEL), Set.of(Role.FENIX_ADMIN));

		Throwable throwable = catchThrowable(() -> mockService.findById(uuid.toString()));
		assertThat(throwable).isNull();
	}

	@Test
	public void authShouldPassFenixAdminHasSpecialAdminRights(){
		UUID uuid = UUID.randomUUID();
		roles.put(new ResourceId(uuid, APP_LEVEL), Set.of(Role.FENIX_ADMIN));

		Throwable throwable = catchThrowable(() -> mockService.getCommunity(uuid.toString()));
		assertThat(throwable).isNull();
	}

	@Test
	public void authShouldNotPassWrongResourceId(){
		UUID uuid = UUID.randomUUID();
		UUID uuid1 = UUID.randomUUID();
		roles.put(new ResourceId(uuid, APP_LEVEL), Set.of(Role.FENIX_ADMIN));

		assertThrows(AccessDeniedException.class, () -> mockService.findById(uuid1.toString()));
	}

	@Test
	public void authShouldNotPassWrongResourceType(){
		UUID uuid = UUID.randomUUID();
		roles.put(new ResourceId(uuid, PROJECT), Set.of(Role.FENIX_ADMIN));

		assertThrows(AccessDeniedException.class, () -> mockService.findById(uuid.toString()));
	}

	@Test
	public void authShouldNotPassWrongRole(){
		UUID uuid = UUID.randomUUID();
		roles.put(new ResourceId(uuid, COMMUNITY), Set.of(Role.SITE_ADMIN));

		assertThrows(AccessDeniedException.class, () -> mockService.getCommunity(uuid.toString()));
	}

	@Test
	public void authShouldNotPassCommunityAdminHasNotAccessToAppLevel(){
		roles.put(new ResourceId((String) null, APP_LEVEL), Set.of(Role.COMMUNITY_ADMIN));

		assertThrows(AccessDeniedException.class, () -> mockService.findAllWithClassScopeAuthorization());
	}
}