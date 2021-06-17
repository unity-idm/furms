/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.api.validation.exceptions.*;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
	private ResourceCreditRepository resourceCreditRepository;

	@InjectMocks
	private ProjectAllocationServiceValidator validator;

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(
			Optional.of(CommunityAllocation.builder()
				.resourceCreditId("id")
				.build()
			));
		when(resourceCreditRepository.findById("id")).thenReturn(
			Optional.of(ResourceCredit.builder()
				.resourceTypeId("id")
				.build()
			));
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(true);
		when(projectRepository.isProjectRelatedWithCommunity("communityId", projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1l))
				.build()));

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate("communityId", projectAllocation));
	}

	@Test
	void shouldThrowWhenResourceTypeReservedException() {
		//given
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(
			Optional.of(CommunityAllocation.builder()
				.resourceCreditId("id")
				.build()
			));
		when(resourceCreditRepository.findById("id")).thenReturn(
			Optional.of(ResourceCredit.builder()
				.resourceTypeId("id")
				.utcStartTime(LocalDateTime.now().minusDays(3))
				.utcEndTime(LocalDateTime.now().plusDays(3))
				.build()
			));
		when(projectAllocationRepository.findAllWithRelatedObjects(projectAllocation.projectId)).thenReturn(Set.of(
			ProjectAllocationResolved.builder()
				.resourceType(ResourceType.builder().id("id").build())
				.resourceCredit(ResourceCredit.builder()
					.utcStartTime(LocalDateTime.now().minusDays(1))
					.utcEndTime(LocalDateTime.now().plusDays(2))
					.build())
				.build()
		));

		when(projectRepository.isProjectRelatedWithCommunity("communityId", projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
			.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1l))
			.build()));

		//when+then
		assertThrows(ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException.class, () -> validator.validateCreate("communityId", projectAllocation));
	}

	@Test
	void shouldNotPassCreateForNullAmount() {
		//given
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(true);
		when(projectRepository.isProjectRelatedWithCommunity("communityId", projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1l))
				.build()));
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(
			Optional.of(CommunityAllocation.builder()
				.resourceCreditId("id")
				.build()
			));
		when(resourceCreditRepository.findById("id")).thenReturn(
			Optional.of(ResourceCredit.builder()
				.resourceTypeId("id")
				.build()
			));
		//when+then
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> validator.validateCreate("communityId", projectAllocation));
		assertThat(ex.getMessage()).contains("amount cannot be null");
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(false);
		when(projectRepository.isProjectRelatedWithCommunity("communityId", projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1l))
				.build()));
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(
			Optional.of(CommunityAllocation.builder()
				.resourceCreditId("id")
				.build()
			));
		when(resourceCreditRepository.findById("id")).thenReturn(
			Optional.of(ResourceCredit.builder()
				.resourceTypeId("id")
				.build()
			));
		//when+then
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> validator.validateCreate("communityId", projectAllocation));
		assertThat(ex.getMessage()).contains("name has to be unique");
	}

	@Test
	void shouldNotPassCreateForNonExistingResourceCreditId() {
		//given
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(false);
		when(projectRepository.isProjectRelatedWithCommunity("communityId", projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1l))
				.build()));
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(
			Optional.of(CommunityAllocation.builder()
				.resourceCreditId("id")
				.build()
			));
		when(resourceCreditRepository.findById("id")).thenReturn(
			Optional.of(ResourceCredit.builder()
				.resourceTypeId("id")
				.build()
			));
		//when+then
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> validator.validateCreate("communityId", projectAllocation));
		assertThat(ex.getMessage()).contains("CommunityAllocation with declared ID does not exist");
	}

	@Test
	void shouldNotPassCreateForNullResourceCreditId() {
		//given
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.name("name")
			.projectId("id")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate("communityId", projectAllocation));
	}

	@Test
	void shouldNotPassCreateDueToExpiredResourceCredit() {
		//given
		final String communityId = "communityId";
		final ProjectAllocation projectAllocation = ProjectAllocation.builder()
				.projectId("id")
				.communityAllocationId("id")
				.name("name")
				.amount(new BigDecimal(1))
				.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.findById(projectAllocation.projectId)).thenReturn(Optional.of(Project.builder()
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1l))
				.build()));
		when(communityAllocationRepository.findByIdWithRelatedObjects(projectAllocation.communityAllocationId))
				.thenReturn(Optional.of(CommunityAllocationResolved.builder()
						.resourceCredit(ResourceCredit.builder()
								.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).minusMinutes(1l))
								.build())
						.build()));
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(
			Optional.of(CommunityAllocation.builder()
				.resourceCreditId("id")
				.build()
			));
		when(resourceCreditRepository.findById("id")).thenReturn(
			Optional.of(ResourceCredit.builder()
				.resourceTypeId("id")
				.build()
			));

		//when+then
		assertThrows(ResourceCreditExpiredException.class, () -> validator.validateCreate(communityId, projectAllocation));
	}

	@Test
	void shouldNotPassUpdateWhenProjectIsExpiredAndAmountIsTryingToBeIncreased() {
		//given
		final String communityId = "communityId";
		final ProjectAllocation existing = ProjectAllocation.builder()
				.id("id")
				.projectId("id")
				.communityAllocationId("id")
				.name("name")
				.amount(new BigDecimal(10))
				.build();
		final ProjectAllocation projectAllocation = ProjectAllocation.builder()
				.id("id")
				.projectId("id")
				.communityAllocationId("id")
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
				.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).minusDays(1l))
				.build()));

		//when+then
		assertThrows(ProjectAllocationWrongAmountException.class, () -> validator.validateUpdate(communityId, projectAllocation));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		final ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id("id")
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.exists(projectAllocation.id)).thenReturn(true);
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(true);
		when(projectAllocationRepository.findById(any())).thenReturn(Optional.of(projectAllocation));
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(CommunityAllocation.builder().id("id").build()));
		when(projectRepository.isProjectRelatedWithCommunity("communityId", projectAllocation.projectId)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate("communityId", projectAllocation));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id("id")
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectAllocationRepository.exists(projectAllocation.id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate("communityId", projectAllocation));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id("id")
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		ProjectAllocation projectAllocation1 = ProjectAllocation.builder()
			.id("id")
			.projectId("id")
			.communityAllocationId("id")
			.name("name2")
			.amount(new BigDecimal(2))
			.build();

		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(communityAllocationRepository.findById(projectAllocation.communityAllocationId)).thenReturn(Optional.of(
				CommunityAllocation.builder().id(projectAllocation.communityAllocationId).build()));
		when(projectAllocationRepository.exists(projectAllocation.id)).thenReturn(true);
		when(projectAllocationRepository.findById(any())).thenReturn(Optional.of(projectAllocation1));
		when(projectAllocationRepository.isNamePresent(any(), any())).thenReturn(false);
		when(projectRepository.isProjectRelatedWithCommunity("communityId", projectAllocation.projectId)).thenReturn(true);
		when(projectRepository.exists(projectAllocation.projectId)).thenReturn(true);

		//when+then
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> validator.validateUpdate("communityId", projectAllocation));
		assertThat(ex.getMessage()).contains("ProjectAllocation name has to be unique");
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		String communityId = "communityId";
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id("id")
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectAllocationRepository.findById(projectAllocation.id)).thenReturn(Optional.of(projectAllocation));
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(communityId, projectAllocation.id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		String id = "id";

		when(projectAllocationRepository.findById(id)).thenReturn(Optional.empty());

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete("communityId", id));
	}

	@Test
	void shouldNotPassWhenCommunityIdAndCommunityAllocationIdAreNotRelated() {
		//given
		String id = "id";
		String communityId = "id";

		when(communityAllocationRepository.findById(id)).thenReturn(Optional.empty());

		//when+then
		assertThrows(CommunityIsNotRelatedWithCommunityAllocation.class, () -> validator.validateCommunityIdAndCommunityAllocationId(communityId, id));
	}

	@Test
	void shouldNotPassWhenProjectIdAndProjectAllocationIdAreNotRelated() {
		//given
		String id = "id";
		String projectId = "id";

		when(projectAllocationRepository.findById(id)).thenReturn(Optional.empty());

		//when+then
		assertThrows(ProjectIsNotRelatedWithProjectAllocation.class, () -> validator.validateProjectIdAndProjectAllocationId(projectId, id));
	}

	@Test
	void shouldPassWhenProjectIdAndProjectAllocationIdAreRelated() {
		//given
		String id = "id";
		String projectId = "id";
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
		String id = "id";
		String communityId = "id";
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
		String communityId = "communityId";
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id("id")
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectAllocationRepository.findById(projectAllocation.id)).thenReturn(Optional.of(projectAllocation));
		when(projectRepository.isProjectRelatedWithCommunity(communityId, projectAllocation.projectId)).thenReturn(false);

		//when+then
		assertThrows(ProjectIsNotRelatedWithCommunity.class, () -> validator.validateDelete("communityId", projectAllocation.id));
	}

}