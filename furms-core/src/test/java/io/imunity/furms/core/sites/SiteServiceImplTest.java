/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.sites.SiteWebClient;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
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
	private UsersDAO usersDAO;
	private AuthzService authzService;

	@BeforeEach
	void setUp() {
		validator = new SiteServiceValidator(repository);
		service = new SiteServiceImpl(repository, validator, webClient, usersDAO, authzService);
	}

	@Test
	void shouldReturnSiteIfExistsInRepository(  ) {
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
		when(repository.isNamePresent(request.getName())).thenReturn(false);
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
		when(repository.isNamePresent(request.getName())).thenReturn(true);

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
		when(repository.isNamePresentIgnoringRecord(request.getName(), request.getId())).thenReturn(false);
		when(repository.update(request)).thenReturn(request.getId());
		when(repository.findById(request.getId())).thenReturn(Optional.of(request));

		//when
		service.update(request);

		//then
		verify(repository, times(1)).update(request);
		verify(webClient, times(1)).update(request);
	}

	@Test
	void shouldUpdateOnlySentFields() {
		//given
		final Site oldSite = Site.builder()
				.id("id")
				.name("name")
				.logo(new FurmsImage(new byte[0], "png"))
				.connectionInfo("connectionInfo")
				.build();
		final Site request = Site.builder()
				.id("id")
				.name("brandNewName")
				.build();
		final Site expectedSite = Site.builder()
				.id(oldSite.getId())
				.name(request.getName())
				.logo(oldSite.getLogo())
				.connectionInfo(oldSite.getConnectionInfo())
				.build();

		when(repository.exists(request.getId())).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.getName(), request.getId())).thenReturn(false);
		when(repository.update(expectedSite)).thenReturn(request.getId());
		when(repository.findById(request.getId())).thenReturn(Optional.of(expectedSite));

		//when
		service.update(request);

		//then
		verify(repository, times(1)).update(expectedSite);
		verify(webClient, times(1)).update(expectedSite);
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
	void shouldReturnTrueForUniqueName() {
		//given
		final String name = "name";
		when(repository.isNamePresent(name)).thenReturn(false);

		//when
		assertThat(service.isNamePresent(name)).isTrue();
	}

	@Test
	void shouldReturnFalseForNomUniqueName() {
		//given
		final String name = "name";
		when(repository.isNamePresent(name)).thenReturn(true);

		//when
		assertThat(service.isNamePresent(name)).isFalse();
	}

	@Test
	void shouldReturnTrueIfNamePresentOutOfSpecificRecord() {
		//given
		final Site site = Site.builder()
				.id("id")
				.name("name")
				.build();
		when(repository.isNamePresentIgnoringRecord(site.getName(), site.getId())).thenReturn(true);

		//when
		assertThat(service.isNamePresentIgnoringRecord(site.getName(), site.getId())).isTrue();
	}

	@Test
	void shouldReturnFalseIfNamePresentInSpecificRecord() {
		//given
		final Site site = Site.builder()
				.id("id")
				.name("name")
				.build();
		when(repository.isNamePresentIgnoringRecord(site.getName(), site.getId())).thenReturn(false);

		//when
		assertThat(service.isNamePresentIgnoringRecord(site.getName(), site.getId())).isFalse();
	}

	@Test
	void shouldReturnAllSiteAdmins() {
		//given
		String siteId = "id";
		when(webClient.getAllAdmins(siteId)).thenReturn(List.of(new User("id", "firstName", "lastName", "email")));

		//when
		List<User> allAdmins = service.findAllAdmins(siteId);

		//then
		assertThat(allAdmins).hasSize(1);
	}

	@Test
	void shouldThrowExceptionWhenSiteIdIsEmptyForFindAllAdmins() {
		//then
		assertThrows(IllegalArgumentException.class, () -> service.findAllAdmins(null));
		assertThrows(IllegalArgumentException.class, () -> service.findAllAdmins(""));
	}

	@Test
	void shouldAddAdminToSite() {
		//given
		String siteId = "siteId";
		String userId = "userId";

		//when
		service.addAdmin(siteId, userId);

		//then
		verify(webClient, times(1)).addAdmin(siteId, userId);
	}

	@Test
	void shouldThrowExceptionWhenSiteIdOrUserIdAreEmptyForAddAdmin() {
		//then
		assertThrows(IllegalArgumentException.class, () -> service.addAdmin(null, null));
		assertThrows(IllegalArgumentException.class, () -> service.addAdmin("", null));
		assertThrows(IllegalArgumentException.class, () -> service.addAdmin("testId", null));
		assertThrows(IllegalArgumentException.class, () -> service.addAdmin(null, ""));
		assertThrows(IllegalArgumentException.class, () -> service.addAdmin(null, "testId"));
		assertThrows(IllegalArgumentException.class, () -> service.addAdmin("", ""));
	}

	@Test
	void shouldTryRollbackAndThrowExceptionWhenWebClientFailedForAddAdmin() {
		//given
		String siteId = "siteId";
		String userId = "userId";
		doThrow(UnityFailureException.class).when(webClient).addAdmin(siteId, userId);
		when(webClient.get(siteId)).thenReturn(Optional.of(Site.builder().id(siteId).build()));

		//then
		assertThrows(UnityFailureException.class, () -> service.addAdmin(siteId, userId));
		verify(webClient, times(1)).get(siteId);
		verify(webClient, times(1)).removeAdmin(siteId, userId);
	}

	@Test
	void shouldRemoveAdminFromSite() {
		//given
		String siteId = "siteId";
		String userId = "userId";

		//when
		service.removeAdmin(siteId, userId);

		//then
		verify(webClient, times(1)).removeAdmin(siteId, userId);
	}

	@Test
	void shouldThrowExceptionWhenSiteIdOrUserIdAreEmptyForRemoveAdmin() {
		//then
		assertThrows(IllegalArgumentException.class, () -> service.removeAdmin(null, null));
		assertThrows(IllegalArgumentException.class, () -> service.removeAdmin("", null));
		assertThrows(IllegalArgumentException.class, () -> service.removeAdmin("testId", null));
		assertThrows(IllegalArgumentException.class, () -> service.removeAdmin(null, ""));
		assertThrows(IllegalArgumentException.class, () -> service.removeAdmin(null, "testId"));
		assertThrows(IllegalArgumentException.class, () -> service.removeAdmin("", ""));
	}

	@Test
	void shouldThrowExceptionWhenWebClientFailedForRemoveAdmin() {
		//given
		String siteId = "siteId";
		String userId = "userId";
		doThrow(UnityFailureException.class).when(webClient).removeAdmin(siteId, userId);

		//then
		assertThrows(UnityFailureException.class, () -> service.removeAdmin(siteId, userId));
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

}