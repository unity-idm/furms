/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import com.google.common.collect.Sets;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.ssh_keys.SSHKeyAuthzException;
import io.imunity.furms.api.ssh_keys.SSHKeyHistoryException;
import io.imunity.furms.api.validation.exceptions.UninstalledUserError;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.InvalidSSHKeyFromOptionException;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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
	
	@Mock
	private UserOperationRepository userOperationRepository;
	
	
	@InjectMocks
	private SSHKeyServiceValidator validator;

	@BeforeEach
	void setUp() {
		validator = new SSHKeyServiceValidator(sshKeysRepository, authzService, siteRepository,
				sshKeyOperationRepository, usersDAO, sshKeyHistoryRepository, userOperationRepository);
	}

	@Test
	void shouldPassCreateForUniqueName() {
		// given
		final SSHKey key = SSHKey.builder().id(UUID.randomUUID().toString()).name("name").value(
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
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		final SSHKey key = getKey(s1);

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);
		when(siteRepository.exists(s1)).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertDoesNotThrow(() -> validator.validateUpdate(key));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		// given
		final SSHKey key = SSHKey.builder().id(UUID.randomUUID().toString()).name("name").ownerId(new PersistentId("id")).build();

		when(sshKeysRepository.exists(key.id)).thenReturn(false);

		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(key));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		// given
		final SSHKey key = SSHKey.builder().id(UUID.randomUUID().toString()).name("name").ownerId(new PersistentId("id")).build();

		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(true);

		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(key));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		// given
		final SSHKeyId id = new SSHKeyId(UUID.randomUUID());
		final SSHKey key = SSHKey.builder().id(id.id.toString()).name("name").ownerId(new PersistentId("id")).build();

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
		final SSHKeyId id = new SSHKeyId(UUID.randomUUID());

		when(sshKeysRepository.exists(id)).thenReturn(false);

		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

	@Test
	void shouldPassDeleteForOwner() {
		// given
		final SSHKeyId id = new SSHKeyId(UUID.randomUUID());
		final SSHKey key = SSHKey.builder().id(id.id.toString()).name("name").ownerId(new PersistentId("id")).build();

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
		final SSHKeyId id = new SSHKeyId(UUID.randomUUID());
		final SSHKey key = SSHKey.builder().id(id.id.toString()).name("name").ownerId(new PersistentId("id")).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id2"));
		when(sshKeysRepository.findById(id)).thenReturn(Optional.of(key));
		when(sshKeysRepository.exists(id)).thenReturn(true);

		// when+then
		assertThrows(SSHKeyAuthzException.class, () -> validator.validateDelete(id));
	}

	@Test
	void shouldPassForUniqueCombinationIdAndName() {
		// given
		final SSHKey key = SSHKey.builder().id(UUID.randomUUID().toString()).name("name").ownerId(new PersistentId("id")).build();

		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);

		// when+then
		assertDoesNotThrow(() -> validator.validateIsNamePresentIgnoringRecord(key.name, key.id));
	}

	@Test
	void shouldNotPassForNonUniqueCombinationIdAndName() {
		// given
		final SSHKey key = SSHKey.builder().id(UUID.randomUUID().toString()).name("name").ownerId(new PersistentId("id")).build();

		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(true);

		// when+then
		assertThrows(IllegalArgumentException.class,
				() -> validator.validateIsNamePresentIgnoringRecord(key.name, key.id));
	}

	@Test
	void shouldPassCreateForOwner() {
		// given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		final SSHKey key = getKey(s1);

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.isNamePresent(key.name)).thenReturn(false);
		when(siteRepository.exists(s1)).thenReturn(true);
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
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		final SSHKey key = getKey(s1);

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);
		when(siteRepository.exists(s1)).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		
		// when+then
		assertDoesNotThrow(() -> validator.validateUpdate(key));
	}

	@Test
	void shouldNotPassUpdateForNotOwner() {
		// given
		final SSHKey key = SSHKey.builder().id(UUID.randomUUID().toString()).name("name").ownerId(new PersistentId("id")).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id2"));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);

		// when+then
		assertThrows(SSHKeyAuthzException.class, () -> validator.validateUpdate(key));
	}

	@Test
	void shouldNotPassWhenSitesNotExists() {
		// given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		final SSHKey key = getKey(s1);

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(siteRepository.exists(s1)).thenReturn(false);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(key));
	}

	@Test
	void shouldNotPassCreateWhenSitesRequireFromOptionAndKeyWithoutFromOption() {
		// given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		final SSHKey key = getKey(s1);

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(siteRepository.exists(s1)).thenReturn(true);
		when(siteRepository.findAll()).thenReturn(
				Sets.newHashSet(Site.builder().id(s1).sshKeyFromOptionMandatory(true).build()));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(key));
	}
	
	@Test
	void shouldNotPassCreateWhenSitesRequireFromOptionAndKeyWithInvalidFromOption() {
		// given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		final SSHKey key = SSHKey.builder().id(UUID.randomUUID().toString()).name("name").value(
				"from=\"*.com\"  ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/f"
						+ "WMGol4PyhUHgRCn6/Hiaz/pnedckSpgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEX"
						+ "hJD/78OSp/ZY8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tpbb"
						+ "gd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5ALmb9xUkdFjZk9bL "
						+ "demo@demo.pl\n" + "")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet(s1)).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(siteRepository.findAll()).thenReturn(
				Sets.newHashSet(Site.builder().id(s1).sshKeyFromOptionMandatory(true).build()));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertThrows(InvalidSSHKeyFromOptionException.class, () -> validator.validateCreate(key));
	}

	@Test
	void shouldNotPassRemoveWhenUnfinishedOperationsExists() {
		// given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		final SSHKey key = getKey(s1);
		
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.findById(key.id)).thenReturn(Optional.of(key));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeyOperationRepository.findBySSHKey(key.id))
				.thenReturn(List.of(SSHKeyOperationJob.builder().id(UUID.randomUUID().toString()).status(SSHKeyOperationStatus.SEND).build()));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		
		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(key.id));
	}
	
	@Test
	void shouldNotPassUpdateWhenUnfinishedOperationsExists() {
		// given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		final SSHKey key = getKey(s1);
		
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);
		when(siteRepository.exists(s1)).thenReturn(true);
		when(sshKeyOperationRepository.findBySSHKey(key.id)).thenReturn(List
				.of(SSHKeyOperationJob.builder().id(UUID.randomUUID().toString()).status(SSHKeyOperationStatus.SEND).build()));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(key));
	}


	@Test
	void shouldCreateWhenSitesRequireFromOptionAndKeyWithFromOption() {
		// given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		final SSHKey key = SSHKey.builder().id(UUID.randomUUID().toString()).name("name").value(
				"from=\"*.sales.example.net,!pc.sales.example.net\"  ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGo"
						+ "l4PyhUHgRCn6/Hiaz/pnedckSpgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/"
						+ "78OSp/ZY8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tpbbgd1Q2k"
						+ "m5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5ALmb9xUkdFjZk9bL demo@demo.pl\n"
						+ "")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet(s1)).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(siteRepository.exists(s1)).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		
		// when+then
		assertDoesNotThrow(() -> validator.validateCreate(key));
	}

	@Test
	void shouldNotPassUpdateWhenSitesRequireFromOptionAndKeyWitoutFromOption() {
		// given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		final SSHKey key = getKey(s1);

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);
		when(siteRepository.exists(s1)).thenReturn(true);
		when(siteRepository.findAll()).thenReturn(
				Sets.newHashSet(Site.builder().id(s1).sshKeyFromOptionMandatory(true).build()));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		// when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(key));
	}

	@Test
	void shouldUpdateWhenSitesRequireFromOptionAndKeyWithFromOption() {
		// given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		final SSHKey key = SSHKey.builder().id(UUID.randomUUID().toString()).name("name").value(
				"from=\"*.sales.example.net,!pc.sales.example.net\"  ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/f"
						+ "WMGol4PyhUHgRCn6/Hiaz/pnedckSpgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEX"
						+ "hJD/78OSp/ZY8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tpbb"
						+ "gd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5ALmb9xUkdFjZk9bL "
						+ "demo@demo.pl\n" + "")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet(s1)).build();

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(sshKeysRepository.exists(key.id)).thenReturn(true);
		when(sshKeysRepository.isNamePresentIgnoringRecord(key.name, key.id)).thenReturn(false);
		when(siteRepository.exists(s1)).thenReturn(true);
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
				.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		
		// when+then
		assertDoesNotThrow(() -> validator.validateUpdate(key));
	}
	
	@Test
	void shouldNotPassHistoryValidation() {

		//given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		when(sshKeyHistoryRepository.findBySiteIdAndOwnerIdLimitTo(s1, "id", 1))
				.thenReturn(Collections.singletonList(SSHKeyHistory.builder().siteId(s1)
					.sshkeyOwnerId(new PersistentId("id")).sshkeyFingerprint(getKey(s1).getFingerprint()).build()));
		// when+then
		assertThrows(SSHKeyHistoryException.class,
			() -> validator.assertKeyWasNotUsedPreviously(Site.builder().id(s1).sshKeyHistoryLength(1).build(),
				getKey(s1)));
	}
	
	@Test
	void shouldPassHistoryValidationWhenHistoryIsNotActiveOnSite() {

		// when+then
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		assertDoesNotThrow(() -> validator.assertKeyWasNotUsedPreviously(Site.builder().id(new SiteId(UUID.randomUUID())).sshKeyHistoryLength(0).build(),
				getKey(s1)));
	}
	
	@Test
	void shouldNotPassHistoryValidationWhenHistoryForKeyIsEmpty() {

		//given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));

		when(sshKeyHistoryRepository.findBySiteIdAndOwnerIdLimitTo(s1, "id", 1))
				.thenReturn(Collections.emptyList());
		// when+then
		assertDoesNotThrow(() -> validator.assertKeyWasNotUsedPreviously(Site.builder().id(s1).sshKeyHistoryLength(1).build(),
				getKey(s1)));
	}

	@Test
	void shouldNotPassWhenUserIsNotInsalledOnSite() {

		//given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		FenixUserId fenixUserId = new FenixUserId("userId");
		when(userOperationRepository.isUserAdded(s1, fenixUserId)).thenReturn(false);
		// when+then
		assertThrows(UninstalledUserError.class, () -> validator.assertUserIsInstalledOnSites(Sets.newHashSet(s1),
			fenixUserId));
	}

	SSHKey getKey(SiteId siteId) {
		return SSHKey.builder().id(UUID.randomUUID().toString()).name("name").value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedckSpgh+"
						+ "RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/ZY8"
						+ "dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7"
						+ "Tpbbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJT"
						+ "KO5ALmb9xUkdFjZk9bL demo@demo.pl\n" + "")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet(siteId)).build();
	}
}
