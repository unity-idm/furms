/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.ADD;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.UPDATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.collect.Sets;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.ssh_keys.SSHKeyAuthzException;
import io.imunity.furms.api.validation.exceptions.UninstalledUserError;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.ssh_keys.SiteAgentSSHKeyOperationService;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;

@ExtendWith(MockitoExtension.class)
public class SSHKeyServiceImplTest {

	@Mock
	private SSHKeyRepository repository;

	@Mock
	private SiteRepository siteRepository;

	@Mock
	private AuthzService authzService;

	@Mock
	private UsersDAO usersDAO;

	@Mock
	private SSHKeyOperationRepository sshKeyOperationRepository;

	@Mock
	private SiteAgentSSHKeyOperationService siteAgentSSHKeyInstallationService;

	@Mock
	private SSHKeyHistoryRepository sshKeyHistoryRepository;

	@Mock
	private UserOperationRepository userOperationRepository;

	private SSHKeyServiceImpl service;

	private SSHKeyServiceValidator validator;
	
	@BeforeEach
	void setUp() {
		TransactionSynchronizationManager.initSynchronization();

		validator = new SSHKeyServiceValidator(repository, authzService, siteRepository,
				sshKeyOperationRepository, usersDAO, sshKeyHistoryRepository, userOperationRepository);
		service = new SSHKeyServiceImpl(repository, validator, authzService, siteRepository,
				sshKeyOperationRepository, siteAgentSSHKeyInstallationService, usersDAO, new SSHKeyFromSiteRemover(repository, siteRepository, sshKeyOperationRepository, siteAgentSSHKeyInstallationService));
	}
	
	@AfterEach
	void clear() {
		TransactionSynchronizationManager.clear();
	}

	@Test
	void shouldReturnSSHKeyIfExistsInRepository() {
		// given
		final String id = "id";
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("ownerId"));
		when(repository.findById(id)).thenReturn(Optional
				.of(SSHKey.builder().id(id).name("name").ownerId(new PersistentId("ownerId")).build()));

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
		when(repository.findAllByOwnerId(new PersistentId("ownerId"))).thenReturn(Set.of(
				SSHKey.builder().id("id1").name("name").ownerId(new PersistentId("ownerId")).build(),
				SSHKey.builder().id("id2").name("name").ownerId(new PersistentId("ownerId")).build()));

		// when
		final Set<SSHKey> allKeys = service.findOwned();

		// then
		assertThat(allKeys).hasSize(2);
	}

	@Test
	void shouldNotAllowToCreateSSHKeyDueToNonUniqueName() {
		// given
		final SSHKey request = SSHKey.builder().id("id").name("name").ownerId(new PersistentId("ownerId"))
				.build();

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
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("id", "s1"))
				.thenReturn(SSHKeyOperationJob.builder().operation(ADD).status(SSHKeyOperationStatus.FAILED).build());
		when(siteRepository.findById("s1")).thenReturn(Optional.of(Site.builder().id("s1").build()));
		when(userOperationRepository.isUserAdded("s1", "id")).thenReturn(true);

		// when
		service.update(request);

		// then
		verify(repository, times(1)).update(request);
	}

	@Test
	void shouldNotAllowToUpdateSSHKeyByNotOwner() {
		// given
		final SSHKey request = SSHKey.builder().id("id").name("name").ownerId(new PersistentId("ownerId"))
				.sites(Set.of("s1")).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("ownerId2"));
		when(repository.exists(request.id)).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.name, request.id)).thenReturn(false);

		// then
		assertThrows(SSHKeyAuthzException.class, () -> service.update(request));
	}

	@Test
	void shouldNotAllowToUpdateSSHKeyWhenUnfinishedOperationExists() {
		// given
		final SSHKey request = getKey("name", Sets.newHashSet("s1"));

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(repository.exists(request.id)).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.name, request.id)).thenReturn(false);
		when(siteRepository.exists("s1")).thenReturn(true);
		when(sshKeyOperationRepository.findBySSHKey("id")).thenReturn(List
				.of(SSHKeyOperationJob.builder().id("id").status(SSHKeyOperationStatus.SEND).build()));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional.of(
				FURMSUser.builder().email("demo@demo.pl").fenixUserId(new FenixUserId("id")).build()));

		// when
		assertThrows(IllegalArgumentException.class, () -> service.update(request));

	}

	@Test
	void shouldNotAllowToCreateSSHKeyByNotOwner() {
		// given
		final SSHKey request = SSHKey.builder().id("id").name("name").ownerId(new PersistentId("ownerId"))
				.sites(Set.of("s1")).build();

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
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		when(repository.exists(request.id)).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.name, request.id)).thenReturn(false);
		when(repository.update(expectedKey)).thenReturn(request.id);
		when(repository.findById(request.id)).thenReturn(Optional.of(expectedKey));
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("id", "s1"))
				.thenReturn(SSHKeyOperationJob.builder().operation(ADD).status(SSHKeyOperationStatus.DONE).build());

		// when
		service.update(request);

		// then
		verify(repository, times(1)).update(expectedKey);

	}
	
	
	@Test
	void shouldCreateOnSitesIfStatusAddIsNotTerminedWhenUpdateKey() {
		// given
		final SSHKey oldKey = getKey("name", Set.of("s1"));

		final SSHKey request = getKey("brandNewName", Set.of("s1", "s2"));

		final SSHKey expectedKey = getKey(request.name, Set.of("s1", "s2"));

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		when(repository.exists(request.id)).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.name, request.id)).thenReturn(false);
		when(repository.update(expectedKey)).thenReturn(request.id);
		when(repository.findById(request.id)).thenReturn(Optional.of(oldKey));
		when(siteRepository.exists("s1")).thenReturn(true);
		when(siteRepository.exists("s2")).thenReturn(true);
		when(siteRepository.findById("s1")).thenReturn(Optional.of(Site.builder().id("s1").build()));
		when(siteRepository.findById("s2")).thenReturn(Optional.of(Site.builder().id("s2").build()));
		when(userOperationRepository.isUserAdded("s1", "id")).thenReturn(true);
		when(userOperationRepository.isUserAdded("s2", "id")).thenReturn(true);
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("id", "s1"))
				.thenReturn(SSHKeyOperationJob.builder().operation(ADD).status(SSHKeyOperationStatus.FAILED).build());
		when(userOperationRepository.isUserAdded("s1", "id")).thenReturn(true);
		when(userOperationRepository.isUserAdded("s2", "id")).thenReturn(true);

		// when
		service.update(request);
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
				.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

		// then
		verify(sshKeyOperationRepository).deleteBySSHKeyIdAndSiteId("id", "s2");
		verify(siteAgentSSHKeyInstallationService, times(2)).addSSHKey(any(), any());
	}
	
	@Test
	void shouldUpdateOnSitesIfStatusUpdateIsNotTerminedWhenUpdateKey() {
		// given
		final SSHKey oldKey = getKey("name", Set.of("s1"));

		final SSHKey request = getKey("brandNewName", Set.of("s1", "s2"));

		final SSHKey expectedKey = getKey(request.name, Set.of("s1", "s2"));

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		when(repository.exists(request.id)).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.name, request.id)).thenReturn(false);
		when(repository.update(expectedKey)).thenReturn(request.id);
		when(repository.findById(request.id)).thenReturn(Optional.of(oldKey));
		when(siteRepository.exists("s1")).thenReturn(true);
		when(siteRepository.exists("s2")).thenReturn(true);
		when(siteRepository.findById("s1")).thenReturn(Optional.of(Site.builder().id("s1").build()));
		when(siteRepository.findById("s2")).thenReturn(Optional.of(Site.builder().id("s2").build()));
		when(userOperationRepository.isUserAdded("s2", "id")).thenReturn(true);
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("id", "s1"))
				.thenReturn(SSHKeyOperationJob.builder().operation(UPDATE).status(SSHKeyOperationStatus.FAILED).build());
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("id", "s1"))
			.thenReturn(SSHKeyOperationJob.builder().operation(UPDATE).status(SSHKeyOperationStatus.FAILED).build());
		when(userOperationRepository.isUserAdded("s2", "id")).thenReturn(true);

		// when
		service.update(request);
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
				.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

		// then
		verify(sshKeyOperationRepository).deleteBySSHKeyIdAndSiteId("id", "s2");
		verify(siteAgentSSHKeyInstallationService, times(1)).updateSSHKey(any(), any());
		verify(siteAgentSSHKeyInstallationService, times(1)).addSSHKey(any(), any());
	}

	@Test
	void shouldNotCreateOnSiteIfUserNotInstalled() {
		// given
		final SSHKey oldKey = getKey("name", Set.of("s1"));

		final SSHKey request = getKey("brandNewName", Set.of("s1"));

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
			.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		when(repository.exists(request.id)).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.name, request.id)).thenReturn(false);
		when(repository.findById(request.id)).thenReturn(Optional.of(oldKey));
		when(siteRepository.exists("s1")).thenReturn(true);
		
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("id", "s1"))
			.thenReturn(SSHKeyOperationJob.builder().operation(ADD).status(SSHKeyOperationStatus.FAILED).build());
		when(userOperationRepository.isUserAdded("s1", "id")).thenReturn(false);
		
		// when + then
		assertThrows(UninstalledUserError.class, () -> service.update(request));
	}

	@Test
	void shouldOnlyCleanStatusWhenDeleteKeyWihtNotTerminedOperation() {
		// given

		final SSHKey key = getKey("key", Set.of("s1"));

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		when(repository.exists(key.id)).thenReturn(true);
		when(repository.findById(key.id)).thenReturn(Optional.of(key));
		when(siteRepository.findById("s1")).thenReturn(Optional.of(Site.builder().id("s1").build()));
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("id", "s1"))
				.thenReturn(SSHKeyOperationJob.builder().operation(ADD).status(SSHKeyOperationStatus.FAILED).build());
		// when
		service.delete("id");

		// then
		verify(sshKeyOperationRepository).deleteBySSHKeyIdAndSiteId("id", "s1");
		verify(siteAgentSSHKeyInstallationService, times(0)).removeSSHKey(any(), any());

	}

	@Test
	void shouldAllowToDeleteSSHKey() {
		// given
		final String id = "id";
		when(repository.exists(id)).thenReturn(true);
		when(repository.findById(id)).thenReturn(Optional.of(SSHKey.builder().id("id").name("name")
				.ownerId(new PersistentId("ownerId")).sites(Set.of("s1")).build()));
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("ownerId"));
		when(usersDAO.findById(new PersistentId("ownerId"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		when(siteRepository.findById("s1")).thenReturn(Optional.of(Site.builder().id("s1").build()));
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("id", "s1"))
				.thenReturn(SSHKeyOperationJob.builder().operation(ADD).status(SSHKeyOperationStatus.DONE).build());

		// when
		service.delete(id);
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
				.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}
		
		verify(siteAgentSSHKeyInstallationService, times(1)).removeSSHKey(any(), any());
	}

	@Test
	void shouldValidateSSHKeyHistoryWhenCreate() {
		final SSHKey key = getKey("key", Set.of("s1"));
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		when(repository.findById("x")).thenReturn(Optional.of(key));
		when(siteRepository.exists("s1")).thenReturn(true);
		when(siteRepository.findById("s1"))
				.thenReturn(Optional.of(Site.builder().id("s1").sshKeyHistoryLength(10).build()));
		when(repository.create(key)).thenReturn("x");
		when(userOperationRepository.isUserAdded("s1", "id")).thenReturn(true);
		// when
		service.create(key);

		// then
		verify(sshKeyHistoryRepository).findBySiteIdAndOwnerIdLimitTo("s1", "id", 10);
	}

	@Test
	void shouldValidateSSHKeyHistoryWhenUpdate() {
		final SSHKey request = getKey("name", Sets.newHashSet("s1"));
		final SSHKey actual = SSHKey.builder().id("id").name("name").value(
				"ssh-dss AAAAB3NzaC1kc3MAAABZAIAB5nrP9OtmQnlVLs0gwztCYEF8u1noe1havIqYqNNtBZp5n"
				+ "O9swNIkIkgaU2trWT3eWXWxT7eCQ+WBLfBTgrm3qLKcd8q4JzBz4DHhywew4VWKMJ/XcOUAAAAVAO8nBt2J"
				+ "WpuQU8hNlXJ7OUdjrlyVAAAAWApF5A+eqDNHMmmqVFpJQI/AreIVb/B6APQuW81TWltf6asvxgzaFhh2MJs"
				+ "S74DqEtyPCAPw+UZ5ovyVXAbEfQEZI7DkHcJFMkPSOrLVT/5xsNJYYXDaJcsAAABYVNcJxBZZI6wktSncZi"
				+ "rXg3Qh7oX2o2R0l+oGSC6fnSZ7OMO1OySpciRozSKI4XFxH5xW3OtAplHII2XIW72g3UBJMFXMPeYPDQBbb"
				+ "3Hv2u18bOPO1R5Sfw==  demo@demo2.pl")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet("s1")).build();
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(repository.exists(request.id)).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.name, request.id)).thenReturn(false);
		when(repository.update(request)).thenReturn(request.id);
		when(repository.findById(request.id)).thenReturn(Optional.of(actual));
		when(siteRepository.exists("s1")).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("id", "s1"))
				.thenReturn(SSHKeyOperationJob.builder().operation(ADD).status(SSHKeyOperationStatus.DONE).build());
		when(siteRepository.findById("s1"))
				.thenReturn(Optional.of(Site.builder().id("s1").sshKeyHistoryLength(10).build()));

		// when
		service.update(request);

		// then
		verify(sshKeyHistoryRepository).findBySiteIdAndOwnerIdLimitTo("s1", "id", 10);
	}

	@Test
	void shouldNotValidateSSHKeyHistoryWhenUpdateOfTheSameKey() {
		final SSHKey request = getKey("name", Sets.newHashSet("s1"));
		final SSHKey actual = SSHKey.builder().id("id").name("name").value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
						+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
						+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
						+ "mb9xUkdFjZk9bL demo@demo2.pl")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet("s1")).build();
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(repository.exists(request.id)).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.name, request.id)).thenReturn(false);
		when(repository.update(request)).thenReturn(request.id);
		when(repository.findById(request.id)).thenReturn(Optional.of(actual));
		when(siteRepository.exists("s1")).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("id", "s1"))
				.thenReturn(SSHKeyOperationJob.builder().operation(ADD).status(SSHKeyOperationStatus.DONE).build());
		when(siteRepository.findById("s1"))
				.thenReturn(Optional.of(Site.builder().id("s1").sshKeyHistoryLength(10).build()));

		// when
		service.update(request);

		// then
		verify(sshKeyHistoryRepository, times(0)).findBySiteIdAndOwnerIdLimitTo("s1", "id", 10);
	}
	
	@Test
	void shouldNotAllowToDeleteSSHKeyDueToKeyNotExists() {
		// given
		final String id = "id";
		when(repository.exists(id)).thenReturn(false);

		// when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id));
		verify(repository, times(0)).delete(id);

	}

	@Test
	void shouldReturnFalseForUniqueName() {
		// given
		final String name = "name";
		when(repository.isNamePresent(name)).thenReturn(false);

		// when
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
		Stream.of(declaredMethods).filter(method -> Modifier.isPublic(method.getModifiers()))
				.forEach(method -> {
					assertThat(method.isAnnotationPresent(FurmsAuthorize.class)).isTrue();
				});
	}

	private SSHKey getKey(String name, Set<String> sites) {
		return SSHKey.builder().id("id").name(name).value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
						+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
						+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
						+ "mb9xUkdFjZk9bL demo@demo.pl")
				.ownerId(new PersistentId("id")).sites(sites).build();
	}

}
