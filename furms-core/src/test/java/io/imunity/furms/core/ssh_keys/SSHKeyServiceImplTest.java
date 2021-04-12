/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.Sets;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.validation.exceptions.SSHKeyAuthzException;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.ssh_key.SSHKey;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;

@ExtendWith(MockitoExtension.class)
public class SSHKeyServiceImplTest {

	@Mock
	private SSHKeyRepository repository;

	@Mock
	private SiteRepository siteRepository;

	@Mock
	private AuthzService authzService;

	private SSHKeyServiceImpl service;

	private SSHKeyServiceValidator validator;

	@BeforeEach
	void setUp() {
		validator = new SSHKeyServiceValidator(repository, authzService, siteRepository);
		service = new SSHKeyServiceImpl(repository, validator, authzService);
	}

	@Test
	void shouldReturnSSHKeyIfExistsInRepository() {
		// given
		final String id = "id";
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("ownerId"));
		when(repository.findById(id)).thenReturn(
				Optional.of(SSHKey.builder().id(id).name("name").ownerId("ownerId").build()));

		// when
		final Optional<SSHKey> byId = service.findById(id);
		final Optional<SSHKey> otherId = service.findById("otherId");

		// then
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(id);
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllSSHKeyssIfExistsInRepository() {
		// given
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("ownerId"));
		when(repository.findAllByOwnerId("ownerId"))
				.thenReturn(Set.of(SSHKey.builder().id("id1").name("name").ownerId("ownerId").build(),
						SSHKey.builder().id("id2").name("name").ownerId("ownerId").build()));

		// when
		final Set<SSHKey> allKeys = service.findOwned();

		// then
		assertThat(allKeys).hasSize(2);
	}

	@Test
	void shouldNotAllowToCreateSSHKeyDueToNonUniqueName() {
		// given
		final SSHKey request = SSHKey.builder().id("id").name("name").ownerId("ownerId").build();

		when(repository.isNamePresent(request.name)).thenReturn(true);

		// when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		verify(repository, times(0)).create(request);

	}

	@Test
	void shouldAllowToUpdateSSHKey() {
		// given
		final SSHKey request = getKey("name", Sets.newHashSet("s1"));

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(repository.exists(request.id)).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.name, request.id)).thenReturn(false);
		when(repository.update(request)).thenReturn(request.id);
		when(repository.findById(request.id)).thenReturn(Optional.of(request));
		when(siteRepository.exists("s1")).thenReturn(true);
		
		// when
		service.update(request);

		// then
		verify(repository, times(1)).update(request);
	}

	@Test
	void shouldNotAllowToUpdateSSHKeyByNotOwner() {
		// given
		final SSHKey request = SSHKey.builder().id("id").name("name").ownerId("ownerId").sites(Set.of("s1"))
				.build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("ownerId2"));
		when(repository.exists(request.id)).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.name, request.id)).thenReturn(false);

		// then
		assertThrows(SSHKeyAuthzException.class, () -> service.update(request));
	}

	@Test
	void shouldNotAllowToCreateSSHKeyByNotOwner() {
		// given
		final SSHKey request = SSHKey.builder().id("id").name("name").ownerId("ownerId").sites(Set.of("s1"))
				.build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("ownerId2"));
		when(repository.isNamePresent("name")).thenReturn(false);

		// then
		assertThrows(SSHKeyAuthzException.class, () -> service.create(request));
	}

	@Test
	void shouldUpdateOnlySentFields() {
		// given
		final SSHKey oldKey = getKey("name", Set.of("s1"));
				
		final SSHKey request = getKey("brandNewName", null);
					
		final SSHKey expectedKey = getKey(request.name, oldKey.sites); 


		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(repository.exists(request.id)).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.name, request.id)).thenReturn(false);
		when(repository.update(expectedKey)).thenReturn(request.id);
		when(repository.findById(request.id)).thenReturn(Optional.of(expectedKey));

		// when
		service.update(request);

		// then
		verify(repository, times(1)).update(expectedKey);

	}

	@Test
	void shouldAllowToDeleteSSHKey() {
		// given
		final String id = "id";
		when(repository.exists(id)).thenReturn(true);
		when(repository.findById(id)).thenReturn(Optional.of(
				SSHKey.builder().id("id").name("name").ownerId("ownerId").sites(Set.of("s1")).build()));
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("ownerId"));

		// when
		service.delete(id);

		verify(repository, times(1)).delete(id);

	}

	@Test
	void shouldNotAllowToDeleteSSHKeyDueToSiteNotExists() {
		// given
		final String id = "id";
		when(repository.exists(id)).thenReturn(false);

		// when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id));
		verify(repository, times(0)).delete(id);

	}

	@Test
	void shouldReturnFalseForUniqueName() {
		//given
		final String name = "name";
		when(repository.isNamePresent(name)).thenReturn(false);

		//when
		assertThat(service.isNamePresent(name)).isFalse();
	}
	
	@Test
	void shouldReturnTrueForNomUniqueName() {
		// given
		final String name = "name";
		when(repository.isNamePresent(name)).thenReturn(true);

		// when
		assertThat(service.isNamePresent(name)).isTrue();
	}

	@Test
	void shouldReturnTrueIfNamePresentOutOfSpecificRecord() {
		// given
		final SSHKey key = SSHKey.builder().id("id").name("name").build();
		when(repository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(true);

		// when
		assertThat(service.isNamePresentIgnoringRecord(key.name, key.id)).isTrue();
	}

	@Test
	void shouldReturnFalseIfNamePresentInSpecificRecord() {
		// given
		final SSHKey key = SSHKey.builder().id("id").name("name").build();
		when(repository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);

		// when
		assertThat(service.isNamePresentIgnoringRecord(key.name, key.id)).isFalse();
	}
	
	@Test
	void allPublicMethodsShouldBeSecured() {
		Method[] declaredMethods = SSHKeyServiceImpl.class.getDeclaredMethods();
		Stream.of(declaredMethods)
				.filter(method -> Modifier.isPublic(method.getModifiers()))
				.forEach(method -> {
					assertThat(method.isAnnotationPresent(FurmsAuthorize.class)).isTrue();
				});
	}

	private SSHKey getKey(String name, Set<String> sites) {
		return SSHKey.builder().id("id").name(name)
				.value("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/"
						+ "fWMGol4PyhUHgRCn6/Hiaz/pnedckSpgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJs"
						+ "yO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/ZY8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9"
						+ "U57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tpbbgd1Q2km5eySfit/5E3EJBYY4"
						+ "PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5ALmb9xUkdFjZk9bL demo@demo.pl")
				.ownerId("id").sites(sites).build();
	}

}
