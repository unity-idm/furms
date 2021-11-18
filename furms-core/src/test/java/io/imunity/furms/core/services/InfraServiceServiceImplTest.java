/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import io.imunity.furms.core.notification.NotificationService;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.services.CreateServiceEvent;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.RemoveServiceEvent;
import io.imunity.furms.domain.services.UpdateServiceEvent;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class InfraServiceServiceImplTest {
	@Mock
	private InfraServiceRepository infraServiceRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private PolicyDocumentRepository policyDocumentRepository;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private ResourceTypeRepository resourceTypeRepository;
	@Mock
	private ResourceCreditRepository resourceCreditRepository;
	@Mock
	private SiteAgentPolicyDocumentService siteAgentPolicyDocumentService;
	@Mock
	private NotificationService notificationService;


	private InfraServiceServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		InfraServiceServiceValidator validator = new InfraServiceServiceValidator(infraServiceRepository, siteRepository, resourceTypeRepository, resourceCreditRepository);
		service = new InfraServiceServiceImpl(infraServiceRepository, validator, siteAgentPolicyDocumentService, siteRepository, policyDocumentRepository, publisher, notificationService);
		orderVerifier = inOrder(infraServiceRepository, publisher);
	}

	@Test
	void shouldReturnInfraService() {
		//given
		String id = "id";
		when(infraServiceRepository.findById(id)).thenReturn(Optional.of(InfraService.builder()
			.id(id)
			.name("userFacingName")
			.build())
		);

		//when
		Optional<InfraService> byId = service.findById(id, "");

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(id);
	}

	@Test
	void shouldNotReturnInfraService() {
		//given
		String id = "id";
		when(infraServiceRepository.findById(id)).thenReturn(Optional.of(InfraService.builder()
			.id(id)
			.name("userFacingName")
			.build())
		);

		//when
		Optional<InfraService> otherId = service.findById("otherId", "");

		//then
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllInfraServicesIfExistsInRepository() {
		//given
		when(infraServiceRepository.findAll("1")).thenReturn(Set.of(
			InfraService.builder().id("id1").name("userFacingName").build(),
			InfraService.builder().id("id2").name("userFacingName2").build()));

		//when
		Set<InfraService> allInfraServices = service.findAll("1");

		//then
		assertThat(allInfraServices).hasSize(2);
	}

	@Test
	void shouldAllowToCreateInfraService() {
		//given
		InfraService request = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.build();

		when(siteRepository.exists(request.id)).thenReturn(true);
		when(infraServiceRepository.isNamePresent(request.name, request.siteId)).thenReturn(false);
		when(infraServiceRepository.create(request)).thenReturn("id");

		//when
		service.create(request);

		orderVerifier.verify(infraServiceRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new CreateServiceEvent("id")));
	}

	@Test
	void shouldUpdateSiteAgentWhenCreatingInfraService() {
		//given
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
				.id(policyId)
				.name("policyName")
				.revision(1)
				.build();
		SiteExternalId siteExternalId = new SiteExternalId("id");
		InfraService request = InfraService.builder()
				.id("id")
				.siteId("id")
				.name("userFacingName")
				.policyId(policyId)
				.build();

		when(siteRepository.exists(request.id)).thenReturn(true);
		when(infraServiceRepository.isNamePresent(request.name, request.siteId)).thenReturn(false);
		when(infraServiceRepository.create(request)).thenReturn("id");
		when(policyDocumentRepository.findById(policyId)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findByIdExternalId("id")).thenReturn(siteExternalId);

		//when
		service.create(request);

		Mockito.verify(siteAgentPolicyDocumentService).updatePolicyDocument(
			siteExternalId,
			PolicyDocument.builder()
				.id(policyId)
				.name("policyName")
				.revision(1)
				.build(),
			"id"
		);
	}

	@Test
	void shouldSentNotificationWhenPolicyDocumentsHaveBeenChanged() {
		//given
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyId policyId1 = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
				.id(policyId1)
				.name("policyName")
				.revision(1)
				.build();
		SiteExternalId siteExternalId = new SiteExternalId("id");
		InfraService oldService = InfraService.builder()
				.id("id")
				.siteId("id")
				.name("userFacingName")
				.policyId(policyId)
				.build();

		InfraService newService = InfraService.builder()
				.id("id")
				.siteId("id")
				.name("userFacingName")
				.policyId(policyId1)
				.build();

		when(siteRepository.exists(oldService.id)).thenReturn(true);
		when(infraServiceRepository.exists(oldService.id)).thenReturn(true);
		when(infraServiceRepository.isNamePresent(oldService.name, oldService.siteId)).thenReturn(false);
		when(infraServiceRepository.findById(oldService.id)).thenReturn(Optional.of(oldService));
		when(policyDocumentRepository.findById(policyId1)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findByIdExternalId("id")).thenReturn(siteExternalId);

		//when
		service.update(newService);

		Mockito.verify(notificationService).notifyAllUsersAboutPolicyAssignmentChange(newService);
	}

	@Test
	void shouldUpdateSiteAgentWhenUpdatingInfraService() {
		//given
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyId policyId1 = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId1)
			.name("policyName")
			.revision(1)
			.build();
		SiteExternalId siteExternalId = new SiteExternalId("id");
		InfraService oldService = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.policyId(policyId)
			.build();

		InfraService newService = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.policyId(policyId1)
			.build();

		when(siteRepository.exists(oldService.id)).thenReturn(true);
		when(infraServiceRepository.exists(oldService.id)).thenReturn(true);
		when(infraServiceRepository.isNamePresent(oldService.name, oldService.siteId)).thenReturn(false);
		when(infraServiceRepository.findById(oldService.id)).thenReturn(Optional.of(oldService));
		when(policyDocumentRepository.findById(policyId1)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findByIdExternalId("id")).thenReturn(siteExternalId);

		//when
		service.update(newService);

		Mockito.verify(siteAgentPolicyDocumentService).updatePolicyDocument(
			siteExternalId,
			PolicyDocument.builder()
				.id(policyId1)
				.name("policyName")
				.revision(1)
				.build(),
			"id"
		);
	}

	@Test
	void shouldUpdateSiteAgentWhenUpdatingInfraServicePolicyToNull() {
		//given
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.name("policyName")
			.revision(1)
			.build();
		SiteExternalId siteExternalId = new SiteExternalId("id");
		InfraService oldService = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.policyId(policyId)
			.build();

		InfraService newService = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.policyId(null)
			.build();

		when(siteRepository.exists(oldService.id)).thenReturn(true);
		when(infraServiceRepository.exists(oldService.id)).thenReturn(true);
		when(infraServiceRepository.isNamePresent(oldService.name, oldService.siteId)).thenReturn(false);
		when(infraServiceRepository.findById(oldService.id)).thenReturn(Optional.of(oldService));
		when(policyDocumentRepository.findById(policyId)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findByIdExternalId("id")).thenReturn(siteExternalId);

		//when
		service.update(newService);

		Mockito.verify(siteAgentPolicyDocumentService).updatePolicyDocument(
			siteExternalId,
			PolicyDocument.builder()
				.id(policyId)
				.name("policyName")
				.revision(-1)
				.build(),
			"id"
		);
	}

	@Test
	void shouldNotAllowToCreateInfraServiceDueToNonUniqueName() {
		//given
		InfraService request = InfraService.builder()
			.id("id")
			.name("name")
			.build();
		when(infraServiceRepository.isNamePresent(request.name, request.siteId)).thenReturn(true);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		orderVerifier.verify(infraServiceRepository, times(0)).create(eq(request));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new CreateServiceEvent("id")));
	}

	@Test
	void shouldAllowToUpdateInfraService() {
		//given
		InfraService request = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.build();

		when(siteRepository.exists(request.id)).thenReturn(true);
		when(infraServiceRepository.exists(request.id)).thenReturn(true);
		when(infraServiceRepository.isNamePresent(request.name, request.siteId)).thenReturn(false);
		when(infraServiceRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(infraServiceRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new UpdateServiceEvent("id")));
	}

	@Test
	void shouldAllowToDisengagePolicyFromInfraService() {
		//given
		InfraService request = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.build();

		when(siteRepository.exists(request.id)).thenReturn(true);
		when(infraServiceRepository.exists(request.id)).thenReturn(true);
		when(infraServiceRepository.isNamePresent(request.name, request.siteId)).thenReturn(false);
		when(infraServiceRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(infraServiceRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new UpdateServiceEvent("id")));
	}

	@Test
	void shouldAllowToDeleteInfraService() {
		//given
		String id = "id";
		when(infraServiceRepository.exists(id)).thenReturn(true);

		//when
		service.delete(id, "");

		orderVerifier.verify(infraServiceRepository).delete(eq(id));
		orderVerifier.verify(publisher).publishEvent(eq(new RemoveServiceEvent("id")));
	}

	@Test
	void shouldNotAllowToDeleteInfraServiceDueToInfraServiceNotExists() {
		//given
		String id = "id";
		when(infraServiceRepository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id, ""));
		orderVerifier.verify(infraServiceRepository, times(0)).delete(eq(id));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new UpdateServiceEvent("id")));
	}

}