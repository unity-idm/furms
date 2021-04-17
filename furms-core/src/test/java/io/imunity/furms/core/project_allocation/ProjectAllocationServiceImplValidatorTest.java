/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.api.validation.exceptions.ProjectIsNotRelatedWithCommunity;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

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
		when(communityAllocationRepository.exists(projectAllocation.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.isUniqueName(any())).thenReturn(true);
		when(projectRepository.isProjectRelatedWithCommunity("communityId", projectAllocation.projectId)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate("communityId", projectAllocation));
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
		when(projectAllocationRepository.isUniqueName(any())).thenReturn(true);
		when(projectRepository.isProjectRelatedWithCommunity("communityId", projectAllocation.projectId)).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate("communityId", projectAllocation));
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
		when(projectAllocationRepository.isUniqueName(any())).thenReturn(false);
		when(projectRepository.isProjectRelatedWithCommunity("communityId", projectAllocation.projectId)).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate("communityId", projectAllocation));
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

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate("communityId", projectAllocation));
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
		when(projectAllocationRepository.isUniqueName(any())).thenReturn(true);
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
		when(projectRepository.isProjectRelatedWithCommunity("communityId", projectAllocation.projectId)).thenReturn(true);

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

		when(projectAllocationRepository.exists(projectAllocation.id)).thenReturn(true);
		when(projectAllocationRepository.findById(any())).thenReturn(Optional.of(projectAllocation1));
		when(projectAllocationRepository.isUniqueName(any())).thenReturn(false);
		when(projectRepository.isProjectRelatedWithCommunity("communityId", projectAllocation.projectId)).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate("communityId", projectAllocation));
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