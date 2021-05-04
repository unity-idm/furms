/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.Sets;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.ssh_keys.SSHKeyAuthzException;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.users.UsersDAO;

@ExtendWith(MockitoExtension.class)
class SSHKeyServiceValidatorTest {

	@Mock
	private SSHKeyRepository sshKeysRepository;

	@Mock
	private SiteRepository siteRepository;

	@Mock
	private SSHKeyOperationRepository sshKeyOperationRepository;

	@Mock
	private AuthzService authzService;
	
	@Mock
	private UsersDAO usersDAO;
	
	@Mock
	private SSHKeyHistoryRepository sshKeyHistoryRepository;
	
	@InjectMocks
	private SSHKeyServiceValidator validator;

	@BeforeEach
	void setUp() {
		validator = new SSHKeyServiceValidator(sshKeysRepository, authzService, siteRepository,
				sshKeyOperationRepository, usersDAO, sshKeyHistoryRepository);
	}

	@Test
	void shouldPassCreateForUniqueName() {
		// given
		final SSHKey key = SSHKey.builder().id("id").name("name").value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedckSp"
						+ "gh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9"
						+ "DM7Tpbbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5Kzc"
						+ "L8sJTKO5ALmb9xUkdFjZk9bL demo@demo.pl\n" + "")
				.ownerId(new PersistentId("id")).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.isNamePresent(key.name)).thenReturn(false);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		
		// when+then
		assertDoesNotThrow(() -> validator.validateCreate(key));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		// given
		final SSHKey key = SSHKey.builder().name("name").ownerId(new PersistentId("id")).build();

		when(sshKeysRepository.isNamePresent(key.name)).thenReturn(true);

		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(key));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		// given
		final SSHKey key = getKey();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);
		when(siteRepository.exists("s1")).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertDoesNotThrow(() -> validator.validateUpdate(key));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		// given
		final SSHKey key = SSHKey.builder().id("id").name("name").ownerId(new PersistentId("id")).build();

		when(sshKeysRepository.exists(key.id)).thenReturn(false);

		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(key));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		// given
		final SSHKey key = SSHKey.builder().id("id").name("name").ownerId(new PersistentId("id")).build();

		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(true);

		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(key));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		// given
		final String id = "id";
		final SSHKey key = SSHKey.builder().id("id").name("name").ownerId(new PersistentId("id")).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.findById(id)).thenReturn(Optional.of(key));
		when(sshKeysRepository.exists(id)).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		// given
		final String id = "id";

		when(sshKeysRepository.exists(id)).thenReturn(false);

		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

	@Test
	void shouldPassDeleteForOwner() {
		// given
		final String id = "id";
		final SSHKey key = SSHKey.builder().id("id").name("name").ownerId(new PersistentId("id")).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.findById(id)).thenReturn(Optional.of(key));
		when(sshKeysRepository.exists(id)).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNotOwner() {
		// given
		final String id = "id";
		final SSHKey key = SSHKey.builder().id("id").name("name").ownerId(new PersistentId("id")).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id2"));
		when(sshKeysRepository.findById(id)).thenReturn(Optional.of(key));
		when(sshKeysRepository.exists(id)).thenReturn(true);

		// when+then
		assertThrows(SSHKeyAuthzException.class, () -> validator.validateDelete(id));
	}

	@Test
	void shouldPassForUniqueCombinationIdAndName() {
		// given
		final SSHKey key = SSHKey.builder().id("id").name("name").ownerId(new PersistentId("id")).build();

		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);

		// when+then
		assertDoesNotThrow(() -> validator.validateIsNamePresentIgnoringRecord(key.name, key.id));
	}

	@Test
	void shouldNotPassForNonUniqueCombinationIdAndName() {
		// given
		final SSHKey key = SSHKey.builder().id("id").name("name").ownerId(new PersistentId("id")).build();

		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(true);

		// when+then
		assertThrows(IllegalArgumentException.class,
				() -> validator.validateIsNamePresentIgnoringRecord(key.name, key.id));
	}

	@Test
	void shouldPassCreateForOwner() {
		// given
		final SSHKey key = getKey();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.isNamePresent(key.name)).thenReturn(false);
		when(siteRepository.exists("s1")).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		
		// when+then
		assertDoesNotThrow(() -> validator.validateCreate(key));
	}

	@Test
	void shouldNotPassCreateForNotOwner() {
		// given
		final SSHKey key = SSHKey.builder().name("name").ownerId(new PersistentId("id")).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id2"));
		when(sshKeysRepository.isNamePresent(key.name)).thenReturn(false);

		// when+then
		assertThrows(SSHKeyAuthzException.class, () -> validator.validateCreate(key));
	}

	@Test
	void shouldPassUpdateForOwner() {
		// given
		final SSHKey key = getKey();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);
		when(siteRepository.exists("s1")).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		
		// when+then
		assertDoesNotThrow(() -> validator.validateUpdate(key));
	}

	@Test
	void shouldNotPassUpdateForNotOwner() {
		// given
		final SSHKey key = SSHKey.builder().id("id").name("name").ownerId(new PersistentId("id")).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id2"));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);

		// when+then
		assertThrows(SSHKeyAuthzException.class, () -> validator.validateUpdate(key));
	}

	@Test
	void shouldNotPassWhenSitesNotExists() {
		// given
		final SSHKey key = getKey();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(siteRepository.exists("s1")).thenReturn(false);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(key));
	}

	@Test
	void shouldNotPassCreateWhenSitesRequireFromOptionAndKeyWitoutFromOption() {
		// given
		final SSHKey key = getKey();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(siteRepository.exists("s1")).thenReturn(true);
		when(siteRepository.findAll()).thenReturn(
				Sets.newHashSet(Site.builder().id("s1").sshKeyFromOptionMandatory(true).build()));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(key));
	}

	@Test
	void shouldNotPassRemoveWhenUnfinishedOperationsExists() {
		// given
		final SSHKey key = getKey();
		
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.findById(key.id)).thenReturn(Optional.of(key));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeyOperationRepository.findBySSHKey(key.id))
				.thenReturn(List.of(SSHKeyOperationJob.builder().id("id").status(SSHKeyOperationStatus.SEND).build()));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		
		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(key.id));
	}
	
	@Test
	void shouldNotPassUpdateWhenUnfinishedOperationsExists() {
		// given
		final SSHKey key = getKey();
		
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);
		when(siteRepository.exists("s1")).thenReturn(true);
		when(sshKeyOperationRepository.findBySSHKey(key.id)).thenReturn(List
				.of(SSHKeyOperationJob.builder().id("id").status(SSHKeyOperationStatus.SEND).build()));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(key));
	}


	@Test
	void shouldCreateWhenSitesRequireFromOptionAndKeyWithFromOption() {
		// given
		final SSHKey key = SSHKey.builder().id("id").name("name").value(
				"from=\"*.sales.example.net,!pc.sales.example.net\"  ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGo"
						+ "l4PyhUHgRCn6/Hiaz/pnedckSpgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/"
						+ "78OSp/ZY8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tpbbgd1Q2k"
						+ "m5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5ALmb9xUkdFjZk9bL demo@demo.pl\n"
						+ "")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet("s1")).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(siteRepository.exists("s1")).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		
		// when+then
		assertDoesNotThrow(() -> validator.validateCreate(key));
	}

	@Test
	void shouldNotPassUpdateWhenSitesRequireFromOptionAndKeyWitoutFromOption() {
		// given
		final SSHKey key = getKey();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);
		when(siteRepository.exists("s1")).thenReturn(true);
		when(siteRepository.findAll()).thenReturn(
				Sets.newHashSet(Site.builder().id("s1").sshKeyFromOptionMandatory(true).build()));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(key));
	}

	@Test
	void shouldUpdateWhenSitesRequireFromOptionAndKeyWithFromOption() {
		// given
		final SSHKey key = SSHKey.builder().id("id").name("name").value(
				"from=\"*.sales.example.net,!pc.sales.example.net\"  ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/f"
						+ "WMGol4PyhUHgRCn6/Hiaz/pnedckSpgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEX"
						+ "hJD/78OSp/ZY8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tpbb"
						+ "gd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5ALmb9xUkdFjZk9bL "
						+ "demo@demo.pl\n" + "")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet("s1")).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);
		when(siteRepository.exists("s1")).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		
		// when+then
		assertDoesNotThrow(() -> validator.validateUpdate(key));
	}

	SSHKey getKey() {
		return SSHKey.builder().id("id").name("name").value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedckSpgh+"
						+ "RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/ZY8"
						+ "dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7"
						+ "Tpbbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJT"
						+ "KO5ALmb9xUkdFjZk9bL demo@demo.pl\n" + "")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet("s1")).build();
	}
}
