/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.sites.SiteWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteServiceImplTest {

	@Mock
	private SiteRepository repository;
	@Mock
	private SiteWebClient webClient;
	private SiteServiceValidator validator;
	private SiteServiceImpl service;

	@BeforeEach
	void setUp() {
		validator = new SiteServiceValidator(repository);
		service = new SiteServiceImpl(repository, validator, webClient);
	}

	@Test
	void shouldReturnSiteIfExistsInRepository() {
		//given
		final String id = "id";
		when(repository.findById(id)).thenReturn(Optional.of(Site.builder()
				.id(id)
				.name("name")
				.build()));

		//when
		final Optional<Site> byId = service.findById(id);
		final Optional<Site> otherId = service.findById("otherId");

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id);
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllSitesIfExistsInRepository() {
		//given
		when(repository.findAll()).thenReturn(Set.of(
				Site.builder().id("id1").name("name").build(),
				Site.builder().id("id2").name("name").build()));

		//when
		final Set<Site> allSites = service.findAll();

		//then
		assertThat(allSites).hasSize(2);
	}

	@Test
	void shouldAllowToCreateSite() {
		//given
		final Site request = Site.builder()
				.name("name")
				.build();
		when(repository.isUniqueName(request.getName())).thenReturn(true);
		when(repository.create(request)).thenReturn(request.getId());
		when(repository.findById(request.getId())).thenReturn(Optional.of(request));

		//when
		service.create(request);

		//then
		verify(repository, times(1)).create(request);
		verify(webClient, times(1)).create(request);
	}

	@Test
	void shouldNotAllowToCreateSiteDueToNonUniqueName() {
		//given
		final Site request = Site.builder()
				.name("name")
				.build();
		when(repository.isUniqueName(request.getName())).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
	}

	@Test
	void shouldAllowToUpdateSite() {
		//given
		final Site request = Site.builder()
				.id("id")
				.name("name")
				.build();
		when(repository.exists(request.getId())).thenReturn(true);
		when(repository.isUniqueName(request.getName())).thenReturn(true);
		when(repository.update(request)).thenReturn(request.getId());
		when(repository.findById(request.getId())).thenReturn(Optional.of(request));

		//when
		service.update(request);

		//then
		verify(repository, times(1)).update(request);
		verify(webClient, times(1)).update(request);
	}

	@Test
	void shouldAllowToDeleteSite() {
		//given
		final String id = "id";
		when(repository.exists(id)).thenReturn(true);

		//when
		service.delete(id);

		verify(repository, times(1)).delete(id);
		verify(webClient, times(1)).delete(id);
	}

	@Test
	void shouldNotAllowToDeleteSiteDueToSiteNotExists() {
		//given
		final String id = "id";
		when(repository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id));
	}


	@Test
	void allPublicMethodsShouldBeSecured() {
		Method[] declaredMethods = SiteServiceImpl.class.getDeclaredMethods();
		Stream.of(declaredMethods)
				.filter(method -> Modifier.isPublic(method.getModifiers()))
				.forEach(method -> {
					assertThat(method.isAnnotationPresent(FurmsAuthorize.class)).isTrue();
					assertThat(method.getAnnotation(FurmsAuthorize.class).resourceType()).isEqualTo(SITE);
				});
	}

	@ParameterizedTest
	@MethodSource("parametersForAllDeclaredMethodsShouldHasCorrectlyDefinedSecurity")
	<T> void allPublicMethodsShouldHasDefinedSecurity(
			String methodName,
			Class<T>[] methodParameters,
			Capability capability,
			String id
	) throws NoSuchMethodException {
		Method findById = SiteServiceImpl.class.getMethod(methodName, methodParameters);
		FurmsAuthorize annotation = findById.getAnnotation(FurmsAuthorize.class);
		assertThat(annotation.id()).isEqualTo(id);
		assertThat(annotation.resourceType()).isEqualTo(SITE);
		assertThat(annotation.capability()).isEqualTo(capability);
	}

	private static Stream<Arguments> parametersForAllDeclaredMethodsShouldHasCorrectlyDefinedSecurity() {
		return Stream.of(
				Arguments.of("findById",    new Class[]{String.class},  SITE_READ,  "id"),
				Arguments.of("findAll",     new Class[]{},              SITE_READ,  ""),
				Arguments.of("create",      new Class[]{Site.class},    SITE_WRITE, ""),
				Arguments.of("update",      new Class[]{Site.class},    SITE_WRITE, "site.id"),
				Arguments.of("delete",      new Class[]{String.class},  SITE_WRITE, ""),
				Arguments.of("isNameUnique", new Class[]{String.class}, SITE_READ,  ""));
	}

}