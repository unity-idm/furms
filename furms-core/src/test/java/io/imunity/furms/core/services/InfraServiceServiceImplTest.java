/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import io.imunity.furms.core.policy_documents.PolicyNotificationService;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceCreatedEvent;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.services.InfraServiceRemovedEvent;
import io.imunity.furms.domain.services.InfraServiceUpdatedEvent;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
	private PolicyNotificationService policyNotificationService;


	private InfraServiceServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		InfraServiceServiceValidator validator = new InfraServiceServiceValidator(infraServiceRepository, siteRepository, resourceTypeRepository, resourceCreditRepository);
		service = new InfraServiceServiceImpl(infraServiceRepository, validator, siteAgentPolicyDocumentService, siteRepository, policyDocumentRepository, publisher, policyNotificationService);
		orderVerifier = inOrder(infraServiceRepository, publisher);
	}

	@Test
	void shouldReturnInfraService() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId id = new InfraServiceId(UUID.randomUUID());
		when(infraServiceRepository.findById(id)).thenReturn(Optional.of(InfraService.builder()
			.id(id)
			.name("userFacingName")
			.build())
		);

		//when
		Optional<InfraService> byId = service.findById(id, siteId);

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(id);
	}

	@Test
	void shouldNotReturnInfraService() {
		//when
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId id = new InfraServiceId(UUID.randomUUID());
		Optional<InfraService> otherId = service.findById(id, siteId);

		//then
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllInfraServicesIfExistsInRepository() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		InfraServiceId infraServiceId1 = new InfraServiceId(UUID.randomUUID());
		when(infraServiceRepository.findAll(siteId)).thenReturn(Set.of(
			InfraService.builder().id(infraServiceId).name("userFacingName").build(),
			InfraService.builder().id(infraServiceId1).name("userFacingName2").build()));

		//when
		Set<InfraService> allInfraServices = service.findAll(siteId);

		//then
		assertThat(allInfraServices).hasSize(2);
	}

	@Test
	void shouldAllowToCreateInfraService() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		InfraService request = InfraService.builder()
			.id(infraServiceId)
			.siteId(siteId)
			.name("userFacingName")
			.build();

		when(siteRepository.exists(siteId)).thenReturn(true);
		when(infraServiceRepository.findById(infraServiceId)).thenReturn(Optional.of(request));
		when(infraServiceRepository.create(request)).thenReturn(infraServiceId);

		//when
		service.create(request);

		orderVerifier.verify(infraServiceRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new InfraServiceCreatedEvent(request)));
	}

	@Test
	void shouldUpdateSiteAgentWhenCreatingInfraService() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
				.id(policyId)
				.name("policyName")
				.revision(1)
				.build();
		SiteExternalId siteExternalId = new SiteExternalId("id");
		InfraService request = InfraService.builder()
				.id(infraServiceId)
				.siteId(siteId)
				.name("userFacingName")
				.policyId(policyId)
				.build();

		when(infraServiceRepository.create(request)).thenReturn(infraServiceId);
		when(policyDocumentRepository.findById(policyId)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findByIdExternalId(siteId)).thenReturn(siteExternalId);
		when(siteRepository.exists(siteId)).thenReturn(true);
		when(infraServiceRepository.findById(infraServiceId)).thenReturn(Optional.of(request));

		//when
		service.create(request);

		Mockito.verify(siteAgentPolicyDocumentService).updatePolicyDocument(
			siteExternalId,
			PolicyDocument.builder()
				.id(policyId)
				.name("policyName")
				.revision(1)
				.build(),
			Optional.of(infraServiceId)
		);
	}

	@Test
	void shouldSentNotificationWhenPolicyDocumentsHaveBeenChanged() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyId policyId1 = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
				.id(policyId1)
				.name("policyName")
				.revision(1)
				.build();
		SiteExternalId siteExternalId = new SiteExternalId("id");
		InfraService oldService = InfraService.builder()
				.id(infraServiceId)
				.siteId(siteId)
				.name("userFacingName")
				.policyId(policyId)
				.build();

		InfraService newService = InfraService.builder()
				.id(infraServiceId)
				.siteId(siteId)
				.name("userFacingName")
				.policyId(policyId1)
				.build();

		when(siteRepository.exists(oldService.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(oldService.id)).thenReturn(true);
		when(infraServiceRepository.findById(oldService.id)).thenReturn(Optional.of(oldService));
		when(policyDocumentRepository.findById(policyId1)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findByIdExternalId(siteId)).thenReturn(siteExternalId);

		//when
		service.update(newService);

		Mockito.verify(policyNotificationService).notifyAllUsersAboutPolicyAssignmentChange(newService);
	}

	@Test
	void shouldUpdateSiteAgentWhenUpdatingInfraService() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyId policyId1 = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId1)
			.name("policyName")
			.revision(1)
			.build();
		SiteExternalId siteExternalId = new SiteExternalId("id");
		InfraService oldService = InfraService.builder()
			.id(infraServiceId)
			.siteId(siteId)
			.name("userFacingName")
			.policyId(policyId)
			.build();

		InfraService newService = InfraService.builder()
			.id(infraServiceId)
			.siteId(siteId)
			.name("userFacingName")
			.policyId(policyId1)
			.build();

		when(siteRepository.exists(oldService.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(oldService.id)).thenReturn(true);
		when(infraServiceRepository.findById(oldService.id)).thenReturn(Optional.of(oldService));
		when(policyDocumentRepository.findById(policyId1)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findByIdExternalId(siteId)).thenReturn(siteExternalId);

		//when
		service.update(newService);

		Mockito.verify(siteAgentPolicyDocumentService).updatePolicyDocument(
			siteExternalId,
			PolicyDocument.builder()
				.id(policyId1)
				.name("policyName")
				.revision(1)
				.build(),
			Optional.of(infraServiceId)
		);
	}

	@Test
	void shouldUpdateSiteAgentWhenUpdatingInfraServicePolicyToNull() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.name("policyName")
			.revision(1)
			.build();
		SiteExternalId siteExternalId = new SiteExternalId("id");
		InfraService oldService = InfraService.builder()
			.id(infraServiceId)
			.siteId(siteId)
			.name("userFacingName")
			.policyId(policyId)
			.build();

		InfraService newService = InfraService.builder()
			.id(infraServiceId)
			.siteId(siteId)
			.name("userFacingName")
			.policyId(null)
			.build();

		when(siteRepository.exists(oldService.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(oldService.id)).thenReturn(true);
		when(infraServiceRepository.findById(oldService.id)).thenReturn(Optional.of(oldService));
		when(policyDocumentRepository.findById(policyId)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findByIdExternalId(siteId)).thenReturn(siteExternalId);

		//when
		service.update(newService);

		Mockito.verify(siteAgentPolicyDocumentService).updatePolicyDocument(
			siteExternalId,
			PolicyDocument.builder()
				.id(policyId)
				.name("policyName")
				.revision(-1)
				.build(),
			Optional.of(infraServiceId)
		);
	}

	@Test
	void shouldNotAllowToCreateInfraServiceDueToNonUniqueName() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		InfraService request = InfraService.builder()
			.id(infraServiceId)
			.siteId(siteId)
			.name("name")
			.build();

		when(siteRepository.exists(siteId)).thenReturn(true);
		when(infraServiceRepository.isNamePresent(request.name, request.siteId)).thenReturn(true);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		orderVerifier.verify(infraServiceRepository, times(0)).create(eq(request));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new InfraServiceCreatedEvent(null)));
	}

	@Test
	void shouldAllowToUpdateInfraService() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		InfraService request = InfraService.builder()
			.id(infraServiceId)
			.siteId(siteId)
			.name("userFacingName")
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(request.id)).thenReturn(true);
		when(infraServiceRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(infraServiceRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new InfraServiceUpdatedEvent(request, request)));
	}

	@Test
	void shouldAllowToDisengagePolicyFromInfraService() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		InfraService request = InfraService.builder()
			.id(infraServiceId)
			.siteId(siteId)
			.name("userFacingName")
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(request.id)).thenReturn(true);
		when(infraServiceRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(infraServiceRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new InfraServiceUpdatedEvent(request, request)));
	}

	@Test
	void shouldAllowToDeleteInfraService() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId id = new InfraServiceId(UUID.randomUUID());
		when(infraServiceRepository.exists(id)).thenReturn(true);
		InfraService infraService = InfraService.builder().build();
		when(infraServiceRepository.findById(id)).thenReturn(Optional.of(infraService));

		//when
		service.delete(id, siteId);

		orderVerifier.verify(infraServiceRepository).delete(eq(id));
		orderVerifier.verify(publisher).publishEvent(eq(new InfraServiceRemovedEvent(infraService)));
	}

	@Test
	void shouldNotAllowToDeleteInfraServiceDueToInfraServiceNotExists() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId id = new InfraServiceId(UUID.randomUUID());
		when(infraServiceRepository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id, siteId));
		orderVerifier.verify(infraServiceRepository, times(0)).delete(eq(id));
		orderVerifier.verify(publisher, times(0)).publishEvent(any());
	}

}