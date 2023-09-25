/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.core.policy_documents.PolicyNotificationService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteCreatedEvent;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.sites.SiteRemovedEvent;
import io.imunity.furms.domain.sites.SiteUpdatedEvent;
import io.imunity.furms.domain.users.AllUsersAndSiteAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserRoleRevokedEvent;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.site.api.site_agent.SiteAgentService;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.sites.SiteGroupDAO;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static io.imunity.furms.domain.authz.roles.Role.SITE_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteServiceImplTest {

	@Mock
	private SiteRepository repository;
	@Mock
	private SiteGroupDAO webClient;
	private SiteServiceImpl service;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private SiteAgentService siteAgentService;
	@Mock
	private SiteAgentStatusService siteAgentStatusService;
	@Mock
	private AuthzService authzService;
	@Mock
	private UserOperationRepository userOperationRepository;
	@Mock
	private PolicyDocumentRepository policyDocumentRepository;
	@Mock
	private SiteAgentPolicyDocumentService siteAgentPolicyDocumentService;
	@Mock
	private CapabilityCollector capabilityCollector;
	@Mock
	private PolicyNotificationService policyNotificationService;
	@Mock
	private InvitatoryService invitatoryService;
	
	@BeforeEach
	void setUp() {
		SiteServiceValidator validator = new SiteServiceValidator(repository, mock(ResourceCreditRepository.class));
		service = new SiteServiceImpl(repository, validator, webClient, usersDAO, publisher, authzService,
				siteAgentService, userOperationRepository, policyDocumentRepository,
				siteAgentPolicyDocumentService, capabilityCollector, policyNotificationService, invitatoryService);
	}

	@Test
	void shouldReturnSiteIfExistsInRepository(  ) {
		//given
		SiteId id = new SiteId(UUID.randomUUID());
		when(repository.findById(id)).thenReturn(Optional.of(Site.builder()
				.id(id)
				.name("name")
				.build()));

		//when
		final Optional<Site> byId = service.findById(id);
		final Optional<Site> otherId = service.findById(new SiteId(UUID.randomUUID()));

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id);
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllSitesIfExistsInRepository() {
		//given
		when(repository.findAll()).thenReturn(Set.of(
				Site.builder().id(new SiteId(UUID.randomUUID())).name("name").build(),
				Site.builder().id(new SiteId(UUID.randomUUID())).name("name").build()));

		//when
		final Set<Site> allSites = service.findAll();

		//then
		assertThat(allSites).hasSize(2);
	}

	@Test
	void shouldReturnOnlyUserSites() {	
		//given
		SiteId id = new SiteId(UUID.randomUUID());
		SiteId id1 = new SiteId(UUID.randomUUID());
		PersistentId pId = new PersistentId("id");
		FenixUserId fId = new FenixUserId("fenixId");
		when(repository.findAll()).thenReturn(Set.of(
				Site.builder().id(id).name("name").build(),
				Site.builder().id(id1).name("name").build()));
		when(usersDAO.getFenixUserId(pId)).thenReturn(fId);
		when(userOperationRepository.isUserAdded(id, fId)).thenReturn(true);
		when(userOperationRepository.isUserAdded(id1, fId)).thenReturn(false);
		
		//when
		final Set<Site> allSites = service.findUserSites(new PersistentId("id"));

		//then
		assertThat(allSites).hasSize(1);
		assertThat(allSites.iterator().next().getId()).isEqualTo(id);
	}

	
	@Test
	void shouldAllowToCreateSite() {
		//given
		final Site request = Site.builder()
				.id(new SiteId(UUID.randomUUID()))
				.name("name")
				.build();
		when(repository.isNamePresent(request.getName())).thenReturn(false);
		when(repository.create(eq(request), any())).thenReturn(request.getId());
		when(repository.findById(request.getId())).thenReturn(Optional.of(request));

		//when
		service.create(request);

		//then
		verify(repository, times(1)).create(eq(request), any());
		verify(webClient, times(1)).create(request);
		verify(publisher, times(1)).publishEvent(new SiteCreatedEvent(request));
	}

	@Test
	void shouldNotAllowToCreateSiteDueToNonUniqueName() {
		//given
		SiteId id = new SiteId(UUID.randomUUID());
		final Site request = Site.builder()
				.id(id)
				.name("name")
				.build();
		when(repository.isNamePresent(request.getName())).thenReturn(true);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		verify(repository, times(0)).create(request, new SiteExternalId("id"));
		verify(webClient, times(0)).create(request);
		verify(publisher, times(0)).publishEvent(new SiteCreatedEvent(null));
	}

	@Test
	void shouldAllowToUpdateSite() {
		//given
		SiteId id = new SiteId(UUID.randomUUID());
		final Site request = Site.builder()
				.id(id)
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
		verify(publisher, times(1)).publishEvent(new SiteUpdatedEvent(request, request));
	}

	@Test
	void shouldSendUpdateToSiteAgent() {
		//given
		SiteId id = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		SiteExternalId siteExternalId = new SiteExternalId("id");
		Site oldSite = Site.builder()
			.id(id)
			.name("name")
			.build();
		Site newSite = Site.builder()
			.id(id)
			.name("name")
			.policyId(policyId)
			.build();
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.name("policyName")
			.revision(1)
			.build();
		final SiteId tempId = new SiteId(UUID.randomUUID());
		when(repository.exists(newSite.getId())).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(any(), any())).thenReturn(false);
		when(repository.findById(eq(oldSite.getId()))).thenReturn(Optional.of(oldSite));
		when(repository.update(any())).thenReturn(tempId);
		when(policyDocumentRepository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		//when
		service.update(newSite);

		//then
		verify(siteAgentPolicyDocumentService).updatePolicyDocument(siteExternalId, PolicyDocument.builder()
			.id(policyId)
			.name("policyName")
			.revision(1)
			.build());
		verify(policyNotificationService, times(1)).notifyAllUsersAboutPolicyAssignmentChange(oldSite.getId());
	}

	@Test
	void shouldSendUpdateToSiteAgentWhenPolicyIdIsSetToNull() {
		//given
		SiteId id = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		Site oldSite = Site.builder()
			.id(id)
			.name("name")
			.policyId(policyId)
			.build();
		Site newSite = Site.builder()
			.id(id)
			.name("name")
			.policyId(null)
			.build();
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.name("policyName")
			.revision(1)
			.build();
		final SiteId tempId = new SiteId(UUID.randomUUID());
		when(repository.exists(newSite.getId())).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(any(), any())).thenReturn(false);
		when(repository.findById(oldSite.getId())).thenReturn(Optional.of(oldSite));
		when(repository.update(any())).thenReturn(tempId);
		when(policyDocumentRepository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		//when
		service.update(newSite);

		//then
		verify(siteAgentPolicyDocumentService).updatePolicyDocument(id.externalId, PolicyDocument.builder()
			.id(policyId)
			.name("policyName")
			.revision(-1)
			.build());
		verify(policyNotificationService, times(0)).notifyAllUsersAboutPolicyAssignmentChange(any(SiteId.class));
	}

	@Test
	void shouldUpdateOnlySentFields() {
		//given
		SiteId id = new SiteId(UUID.randomUUID());
		final Site oldSite = Site.builder()
				.id(id)
				.name("name")
				.logo(new FurmsImage(new byte[0], "png"))
				.connectionInfo("connectionInfo")
				.build();
		final Site request = Site.builder()
				.id(id)
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
		when(repository.findById(request.getId())).thenReturn(Optional.of(oldSite))
			.thenReturn(Optional.of(expectedSite));

		//when
		service.update(request);

		//then
		verify(repository, times(1)).update(expectedSite);
		verify(webClient, times(1)).update(expectedSite);
		verify(publisher, times(1)).publishEvent(new SiteUpdatedEvent(oldSite, expectedSite));
	}

	@Test
	void shouldAllowToDeleteSite() {
		//given
		SiteId id = new SiteId(UUID.randomUUID());
		when(repository.exists(id)).thenReturn(true);
		Site site = Site.builder()
			.id(id)
			.build();
		when(repository.findById(id)).thenReturn(Optional.of(site));

		//when
		service.delete(id);

		verify(repository, times(1)).delete(id);
		verify(webClient, times(1)).delete(id);
		verify(publisher, times(1)).publishEvent(new SiteRemovedEvent(site));
	}

	@Test
	void shouldNotAllowToDeleteSiteDueToSiteNotExists() {
		//given
		SiteId id = new SiteId(UUID.randomUUID());
		when(repository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id));
		verify(repository, times(0)).delete(id);
		verify(webClient, times(0)).delete(id);
		verify(publisher, times(0)).publishEvent(new SiteRemovedEvent(null));
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
		SiteId id = new SiteId(UUID.randomUUID());
		final Site site = Site.builder()
				.id(id)
				.name("name")
				.build();
		when(repository.isNamePresentIgnoringRecord(site.getName(), site.getId())).thenReturn(true);

		//when
		assertThat(service.isNamePresentIgnoringRecord(site.getName(), site.getId())).isTrue();
	}

	@Test
	void shouldReturnFalseIfNamePresentInSpecificRecord() {
		//given
		SiteId id = new SiteId(UUID.randomUUID());
		final Site site = Site.builder()
				.id(id)
				.name("name")
				.build();
		when(repository.isNamePresentIgnoringRecord(site.getName(), site.getId())).thenReturn(false);

		//when
		assertThat(service.isNamePresentIgnoringRecord(site.getName(), site.getId())).isFalse();
	}

	@Test
	void shouldReturnAllSiteAdmins() {
		//given
		SiteId id = new SiteId(UUID.randomUUID());
		when(webClient.getAllUsersAndSiteAdmins(id)).thenReturn(
			new AllUsersAndSiteAdmins(
				List.of(),
				List.of(FURMSUser.builder()
					.id(new PersistentId("id"))
					.firstName("firstName")
					.lastName("lastName")
					.email("email")
					.build()))
	);

		//when
		AllUsersAndSiteAdmins allAdmins = service.findAllUsersAndSiteAdmins(id);

		//then
		assertThat(allAdmins.siteAdmins).hasSize(1);
	}

	@Test
	void shouldThrowExceptionWhenSiteIdIsEmptyForFindAllAdmins() {
		//then
		assertThrows(IllegalArgumentException.class, () -> service.findAllUsersAndSiteAdmins(null));
		assertThrows(IllegalArgumentException.class, () -> service.findAllUsersAndSiteAdmins(new SiteId((UUID) null)));
	}

	@Test
	void shouldAddAdminToSite() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		Site site = Site.builder()
			.id(siteId)
			.name("name")
			.build();
		when(repository.findById(siteId)).thenReturn(Optional.of(site));

		//when
		service.addAdmin(siteId, userId);

		//then
		verify(webClient, times(1)).addSiteUser(siteId, userId, SITE_ADMIN);
	}

	@Test
	void shouldThrowExceptionWhenSiteIdOrUserIdAreEmptyForAddAdmin() {
		//then
		assertThrows(IllegalArgumentException.class, () -> service.addAdmin(null, null));
		assertThrows(IllegalArgumentException.class, () -> service.addAdmin(new SiteId((UUID) null), null));
		assertThrows(IllegalArgumentException.class, () -> service.addAdmin(new SiteId(UUID.randomUUID()), null));
		assertThrows(IllegalArgumentException.class, () -> service.addAdmin(null, new PersistentId("")));
		assertThrows(IllegalArgumentException.class, () -> service.addAdmin(null, new PersistentId("testId")));
		assertThrows(IllegalArgumentException.class, () -> service.addAdmin(new SiteId((UUID) null), new PersistentId("")));
	}

	@Test
	void shouldTryRollbackAndThrowExceptionWhenWebClientFailedForAddAdmin() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		doThrow(UnityFailureException.class).when(webClient).addSiteUser(siteId, userId, SITE_ADMIN);
		when(webClient.get(siteId)).thenReturn(Optional.of(Site.builder().id(siteId).build()));

		//then
		assertThrows(UnityFailureException.class, () -> service.addAdmin(siteId, userId));
		verify(webClient, times(1)).get(siteId);
		verify(webClient, times(1)).removeSiteUser(siteId, userId);
		verify(publisher, times(0)).publishEvent(new UserRoleRevokedEvent(userId, new ResourceId(siteId, SITE), null
			, SITE_ADMIN));
	}

	@Test
	void shouldRemoveAdminFromSite() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");

		Site site = Site.builder()
			.id(siteId)
			.name("name")
			.build();
		when(service.findById(siteId)).thenReturn(Optional.of(site));
		//when
		service.removeSiteUser(siteId, userId);

		//then
		verify(webClient, times(1)).removeSiteUser(siteId, userId);
		verify(publisher, times(1)).publishEvent(new UserRoleRevokedEvent(userId, new ResourceId(siteId, SITE),
			"name", SITE_ADMIN));
	}

	@Test
	void shouldThrowExceptionWhenSiteIdOrUserIdAreEmptyForRemoveAdmin() {
		//then
		assertThrows(IllegalArgumentException.class, () -> service.removeSiteUser(null, null));
		assertThrows(IllegalArgumentException.class, () -> service.removeSiteUser(new SiteId((UUID) null), null));
		assertThrows(IllegalArgumentException.class, () -> service.removeSiteUser(new SiteId(UUID.randomUUID()), null));
		assertThrows(IllegalArgumentException.class, () -> service.removeSiteUser(null, new PersistentId("")));
		assertThrows(IllegalArgumentException.class, () -> service.removeSiteUser(null, new PersistentId("testId")));
		assertThrows(IllegalArgumentException.class, () -> service.removeSiteUser(new SiteId((UUID) null), new PersistentId("")));
	}

	@Test
	void shouldThrowExceptionWhenWebClientFailedForRemoveAdmin() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		Site site = Site.builder()
			.id(siteId)
			.name("name")
			.build();
		when(repository.findById(siteId)).thenReturn(Optional.of(site));
		doThrow(UnityFailureException.class).when(webClient).removeSiteUser(siteId, userId);

		//then
		assertThrows(UnityFailureException.class, () -> service.removeSiteUser(siteId, userId));
		verify(publisher, times(0)).publishEvent(new UserRoleRevokedEvent(userId, new ResourceId(siteId, SITE),
			"name", SITE_ADMIN));
	}

	@Test
	void allPublicMethodsShouldBeSecured() {
		Method[] declaredMethods = SiteServiceImpl.class.getDeclaredMethods();
		Stream.of(declaredMethods)
				.filter(method -> Modifier.isPublic(method.getModifiers()))
				.filter(method -> !method.getName().equals("findAllIds"))
				.forEach(method -> assertThat(method.isAnnotationPresent(FurmsAuthorize.class)).isTrue());
	}

}