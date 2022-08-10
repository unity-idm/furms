/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.api.validation.exceptions.CommunityIsNotRelatedWithCommunityAllocation;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationDecreaseBeyondUsageException;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationIncreaseInExpiredProjectException;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationIsNotInTerminalStateException;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationWrongAmountException;
import io.imunity.furms.api.validation.exceptions.ProjectExpiredException;
import io.imunity.furms.api.validation.exceptions.ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException;
import io.imunity.furms.api.validation.exceptions.ProjectIsNotRelatedWithCommunity;
import io.imunity.furms.api.validation.exceptions.ProjectIsNotRelatedWithProjectAllocation;
import io.imunity.furms.api.validation.exceptions.ProjectNotInTerminalStateException;
import io.imunity.furms.api.validation.exceptions.ResourceCreditExpiredException;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAllocationServiceImplValidatorTest {
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private CommunityAllocationRepository communityAllocationRepository;
	@Mock
	private ProjectAllocationRepository projectAllocationRepository;
	@Mock
	private ProjectInstallationService projectInstallationService;
	@Mock
	private ResourceCreditRepository resourceCreditRepository;
	@Mock
	private ResourceUsageRepository resourceUsageRepository;
	@Mock
	private ProjectAllocationInstallationRepository projectAllocationInstallationRepository;

	@InjectMocks
	private ProjectAllocationServiceValidator validator;

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());

		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(
			Optional.of(CommunityAllocation.builder()
				.resourceCreditId(resourceCreditId)
				.build()
			));
		when(resourceCreditRepository.findById(resourceCreditId)).thenReturn(
			Optional.of(ResourceCredit.builder()
				.resourceTypeId(resourceTypeId)
				.build()
			));
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(true);
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1L))
				.build()));

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(communityId, projectAllocation));
	}

	@Test
	void shouldThrowWhenResourceTypeReservedException() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());

		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(
			Optional.of(CommunityAllocation.builder()
				.resourceCreditId(resourceCreditId)
				.build()
			));
		when(resourceCreditRepository.findById(resourceCreditId)).thenReturn(
			Optional.of(ResourceCredit.builder()
				.resourceTypeId(resourceTypeId)
				.utcStartTime(LocalDateTime.now().minusDays(3))
				.utcEndTime(LocalDateTime.now().plusDays(3))
				.build()
			));
		when(projectAllocationRepository.findAllWithRelatedObjects(projectAllocation.projectId)).thenReturn(Set.of(
			ProjectAllocationResolved.builder()
				.resourceType(ResourceType.builder().id(resourceTypeId).build())
				.resourceCredit(ResourceCredit.builder()
					.utcStartTime(LocalDateTime.now().minusDays(1))
					.utcEndTime(LocalDateTime.now().plusDays(2))
					.build())
				.build()
		));

		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
			.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1L))
			.build()));

		//when+then
		assertThrows(ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException.class,
			() -> validator.validateCreate(communityId, projectAllocation));
	}

	@Test
	void shouldNotPassCreateForNullAmount() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());

		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.projectId(new ProjectId(UUID.randomUUID()))
			.communityAllocationId(new CommunityAllocationId(UUID.randomUUID()))
			.name("name")
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(true);
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1L))
				.build()));
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(
			Optional.of(CommunityAllocation.builder()
				.resourceCreditId(resourceCreditId)
				.build()
			));
		when(resourceCreditRepository.findById(resourceCreditId)).thenReturn(
			Optional.of(ResourceCredit.builder()
				.resourceTypeId(new ResourceTypeId(UUID.randomUUID()))
				.build()
			));
		//when+then
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> validator.validateCreate(communityId, projectAllocation));
		assertThat(ex.getMessage()).contains("amount cannot be null");
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());

		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.projectId(new ProjectId(UUID.randomUUID()))
			.communityAllocationId(new CommunityAllocationId(UUID.randomUUID()))
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(false);
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1L))
				.build()));
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(
			Optional.of(CommunityAllocation.builder()
				.resourceCreditId(resourceCreditId)
				.build()
			));
		when(resourceCreditRepository.findById(resourceCreditId)).thenReturn(
			Optional.of(ResourceCredit.builder()
				.resourceTypeId(resourceTypeId)
				.build()
			));
		//when+then
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> validator.validateCreate(communityId, projectAllocation));
		assertThat(ex.getMessage()).contains("name has to be unique");
	}

	@Test
	void shouldNotPassCreateForNonExistingResourceCreditId() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());

		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.projectId(new ProjectId(UUID.randomUUID()))
			.communityAllocationId(new CommunityAllocationId(UUID.randomUUID()))
			.name("name")
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(false);
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1L))
				.build()));
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(
			Optional.of(CommunityAllocation.builder()
				.resourceCreditId(resourceCreditId)
				.build()
			));
		when(resourceCreditRepository.findById(resourceCreditId)).thenReturn(
			Optional.of(ResourceCredit.builder()
				.resourceTypeId(new ResourceTypeId(UUID.randomUUID()))
				.build()
			));
		//when+then
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> validator.validateCreate(communityId, projectAllocation));
		assertThat(ex.getMessage()).contains("CommunityAllocation with declared ID does not exist");
	}

	@Test
	void shouldNotPassCreateForNullResourceCreditId() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.name("name")
			.projectId(projectId)
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(communityId, projectAllocation));
	}

	@Test
	void shouldNotPassCreateDueToExpiredResourceCredit() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());

		final ProjectAllocation projectAllocation = ProjectAllocation.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("name")
				.amount(new BigDecimal(1))
				.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1L))
				.build()));
		when(communityAllocationRepository.findByIdWithRelatedObjects(projectAllocation.communityAllocationId))
				.thenReturn(Optional.of(CommunityAllocationResolved.builder()
						.resourceCredit(ResourceCredit.builder()
								.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).minusMinutes(1L))
								.build())
						.build()));
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(
			Optional.of(CommunityAllocation.builder()
				.resourceCreditId(resourceCreditId)
				.build()
			));
		when(resourceCreditRepository.findById(resourceCreditId)).thenReturn(
			Optional.of(ResourceCredit.builder()
				.resourceTypeId(new ResourceTypeId(UUID.randomUUID()))
				.build()
			));

		//when+then
		assertThrows(ResourceCreditExpiredException.class, () -> validator.validateCreate(communityId, projectAllocation));
	}

	@Test
	void shouldNotPassUpdateWhenProjectIsExpiredAndAmountIsTryingToBeIncreased() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		final ProjectAllocation existing = ProjectAllocation.builder()
				.id(projectAllocationId)
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("name")
				.amount(new BigDecimal(10))
				.build();
		final ProjectAllocation projectAllocation = ProjectAllocation.builder()
				.id(projectAllocationId)
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("name2")
				.amount(new BigDecimal(12))
				.build();

		when(projectAllocationRepository.findById(existing.id)).thenReturn(Optional.of(existing));
		when(projectAllocationRepository.exists(projectAllocation.id)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(true);
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(Optional.of(
				CommunityAllocation.builder().id(projectAllocation.communityAllocationId).build()));
		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).minusDays(1L))
				.build()));

		//when+then
		assertThrows(ProjectAllocationIncreaseInExpiredProjectException.class, () -> validator.validateUpdate(communityId, projectAllocation));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());

		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id(projectAllocationId)
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(1))
			.build();
		ProjectAllocation updatedProjectAllocation = ProjectAllocation.builder()
			.id(projectAllocationId)
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(BigDecimal.valueOf(3))
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.exists(projectAllocation.id)).thenReturn(true);
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(true);
		when(projectAllocationRepository.findById(projectAllocation.id)).thenReturn(Optional.of(projectAllocation));
		when(projectAllocationRepository.getAvailableAmount(projectAllocation.communityAllocationId)).thenReturn(BigDecimal.TEN);
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(CommunityAllocation.builder().id(communityAllocationId).build()));
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(resourceUsageRepository.findCurrentResourceUsage(projectAllocation.id)).thenReturn(Optional.of(ResourceUsage.builder()
			.cumulativeConsumption(BigDecimal.ONE)
			.build())
		);
		when(projectAllocationInstallationRepository.findByProjectAllocationId(projectAllocation.id)).thenReturn(ProjectAllocationInstallation.builder()
			.status(ProjectAllocationInstallationStatus.PROJECT_INSTALLATION_FAILED)
			.build()
		);

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(communityId, updatedProjectAllocation));
	}

	@Test
	void shouldNotPassUpdateWhenAllocationIsNotInTerminalState() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());

		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id(projectAllocationId)
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(1))
			.build();
		ProjectAllocation updatedProjectAllocation = ProjectAllocation.builder()
			.id(projectAllocationId)
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(BigDecimal.valueOf(3))
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.exists(projectAllocation.id)).thenReturn(true);
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(true);
		when(projectAllocationRepository.findById(projectAllocation.id)).thenReturn(Optional.of(projectAllocation));
		when(projectAllocationRepository.getAvailableAmount(projectAllocation.communityAllocationId)).thenReturn(BigDecimal.TEN);
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(CommunityAllocation.builder().id(communityAllocationId).build()));
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(resourceUsageRepository.findCurrentResourceUsage(projectAllocation.id)).thenReturn(Optional.of(ResourceUsage.builder()
			.cumulativeConsumption(BigDecimal.ONE)
			.build())
		);
		when(projectAllocationInstallationRepository.findByProjectAllocationId(projectAllocation.id)).thenReturn(ProjectAllocationInstallation.builder()
			.status(ProjectAllocationInstallationStatus.INSTALLING)
			.build()
		);


		//when+then
		assertThrows(ProjectAllocationIsNotInTerminalStateException.class, () -> validator.validateUpdate(communityId,
			updatedProjectAllocation));
	}

	@Test
	void shouldNotPassUpdateWhenConsumptionIsBiggerThenAllocationAmount() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());

		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id(projectAllocationId)
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(5))
			.build();
		ProjectAllocation updatedProjectAllocation = ProjectAllocation.builder()
			.id(projectAllocationId)
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(BigDecimal.valueOf(3))
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.exists(projectAllocation.id)).thenReturn(true);
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(true);
		when(projectAllocationRepository.findById(projectAllocation.id)).thenReturn(Optional.of(projectAllocation));
		when(projectAllocationRepository.getAvailableAmount(projectAllocation.communityAllocationId)).thenReturn(BigDecimal.TEN);
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(CommunityAllocation.builder().id(communityAllocationId).build()));
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(resourceUsageRepository.findCurrentResourceUsage(projectAllocation.id)).thenReturn(Optional.of(ResourceUsage.builder()
			.cumulativeConsumption(BigDecimal.valueOf(4))
			.build())
		);

		//when+then
		assertThrows(ProjectAllocationDecreaseBeyondUsageException.class, () -> validator.validateUpdate(communityId,
			updatedProjectAllocation));
	}

	@Test
	void shouldNotPassUpdateWhenAllocationAmountIsBiggerThenCommunityAllocationAmount() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());

		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id(projectAllocationId)
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(BigDecimal.valueOf(9))
			.build();
		ProjectAllocation updatedProjectAllocation = ProjectAllocation.builder()
			.id(projectAllocationId)
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(BigDecimal.valueOf(11))
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.exists(projectAllocation.id)).thenReturn(true);
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(true);
		when(projectAllocationRepository.findById(projectAllocation.id)).thenReturn(Optional.of(projectAllocation));
		when(projectAllocationRepository.getAvailableAmount(projectAllocation.communityAllocationId)).thenReturn(BigDecimal.ONE);
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(CommunityAllocation.builder().id(communityAllocationId).build()));
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);


		//when+then
		String message = assertThrows(ProjectAllocationWrongAmountException.class, () -> validator.validateUpdate(communityId,
			updatedProjectAllocation)).getMessage();
		assertEquals("Allocation amount have to be less then community allocation limit", message);
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id(projectAllocationId)
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectAllocationRepository.exists(projectAllocation.id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(communityId, projectAllocation));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());

		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id(projectAllocationId)
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		ProjectAllocation projectAllocation1 = ProjectAllocation.builder()
			.id(projectAllocationId)
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name2")
			.amount(new BigDecimal(2))
			.build();

		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(Optional.of(
				CommunityAllocation.builder().id(projectAllocation.communityAllocationId).build()));
		when(projectAllocationRepository.exists(projectAllocation.id)).thenReturn(true);
		when(projectAllocationRepository.findById(any())).thenReturn(Optional.of(projectAllocation1));
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(false);
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);

		//when+then
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> validator.validateUpdate(communityId, projectAllocation));
		assertThat(ex.getMessage()).contains("ProjectAllocation name has to be unique");
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id(new ProjectAllocationId(UUID.randomUUID()))
			.projectId(new ProjectId(UUID.randomUUID()))
			.communityAllocationId(new CommunityAllocationId(UUID.randomUUID()))
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectAllocationRepository.findById(projectAllocation.id)).thenReturn(Optional.of(projectAllocation));
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1L))
				.build()));
		when(projectInstallationService.isProjectInTerminalState(projectAllocation.projectId)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(communityId, projectAllocation.id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		ProjectAllocationId id = new ProjectAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());

		when(projectAllocationRepository.findById(id)).thenReturn(Optional.empty());

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(communityId, id));
	}

	@Test
	void shouldNotPassWhenCommunityIdAndCommunityAllocationIdAreNotRelated() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		CommunityAllocationId id = new CommunityAllocationId(UUID.randomUUID());

		when(communityAllocationRepository.findById(id)).thenReturn(Optional.empty());

		//when+then
		assertThrows(CommunityIsNotRelatedWithCommunityAllocation.class, () -> validator.validateCommunityIdAndCommunityAllocationId(communityId, id));
	}

	@Test
	void shouldNotPassWhenProjectIdAndProjectAllocationIdAreNotRelated() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId id = new ProjectAllocationId(UUID.randomUUID());

		when(projectAllocationRepository.findById(id)).thenReturn(Optional.empty());

		//when+then
		assertThrows(ProjectIsNotRelatedWithProjectAllocation.class, () -> validator.validateProjectIdAndProjectAllocationId(projectId, id));
	}

	@Test
	void shouldPassWhenProjectIdAndProjectAllocationIdAreRelated() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId id = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.projectId(projectId)
			.build();

		//when
		when(projectAllocationRepository.findById(id)).thenReturn(Optional.of(projectAllocation));

		//then
		validator.validateProjectIdAndProjectAllocationId(projectId, id);
	}

	@Test
	void shouldPassWhenCommunityIdAndCommunityAllocationIdAreRelated() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		CommunityAllocationId id = new CommunityAllocationId(UUID.randomUUID());
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.communityId(communityId)
			.build();

		//when
		when(communityAllocationRepository.findById(id)).thenReturn(Optional.of(communityAllocation));

		//then
		validator.validateCommunityIdAndCommunityAllocationId(communityId, id);
	}

	@Test
	void shouldNotPassDeleteWhenCommunityAndProjectAreNotRelated() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id(new ProjectAllocationId(UUID.randomUUID()))
			.projectId(new ProjectId(UUID.randomUUID()))
			.communityAllocationId(new CommunityAllocationId(UUID.randomUUID()))
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectAllocationRepository.findById(projectAllocation.id)).thenReturn(Optional.of(projectAllocation));
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(false);

		//when+then
		assertThrows(ProjectIsNotRelatedWithCommunity.class, () -> validator.validateDelete(communityId, projectAllocation.id));
	}

	@Test
	void shouldNotPassDeleteWhenProjectIsExpired() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
				.id(new ProjectAllocationId(UUID.randomUUID()))
				.projectId(new ProjectId(UUID.randomUUID()))
				.communityAllocationId(new CommunityAllocationId(UUID.randomUUID()))
				.name("name")
				.amount(new BigDecimal(1))
				.build();

		when(projectAllocationRepository.findById(projectAllocation.id)).thenReturn(Optional.of(projectAllocation));
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).minusMinutes(1L))
				.build()));

		//when+then
		assertThrows(ProjectExpiredException.class, () -> validator.validateDelete(communityId, projectAllocation.id));
	}

	@Test
	void shouldNotPassDeleteWhenProjectIsNotInTerminalState() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
				.id(new ProjectAllocationId(UUID.randomUUID()))
				.projectId(new ProjectId(UUID.randomUUID()))
				.communityAllocationId(new CommunityAllocationId(UUID.randomUUID()))
				.name("name")
				.amount(new BigDecimal(1))
				.build();

		when(projectAllocationRepository.findById(projectAllocation.id)).thenReturn(Optional.of(projectAllocation));
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1L))
				.build()));
		when(projectInstallationService.isProjectInTerminalState(projectAllocation.projectId)).thenReturn(false);

		//when+then
		assertThrows(ProjectNotInTerminalStateException.class, () -> validator.validateDelete(communityId, projectAllocation.id));
	}

}