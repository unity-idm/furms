/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

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
		ProjectAllocation service = ProjectAllocation.builder()
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectRepository.exists(service.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(service.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullAmount() {
		//given
		ProjectAllocation service = ProjectAllocation.builder()
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.build();

		when(projectRepository.exists(service.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(service.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		ProjectAllocation service = ProjectAllocation.builder()
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectRepository.exists(service.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(service.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonExistingResourceCreditId() {
		//given
		ProjectAllocation service = ProjectAllocation.builder()
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.build();

		when(projectRepository.exists(service.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(service.communityAllocationId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullResourceCreditId() {
		//given
		ProjectAllocation service = ProjectAllocation.builder()
			.name("name")
			.projectId("id")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		final ProjectAllocation service = ProjectAllocation.builder()
			.id("id")
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectRepository.exists(service.projectId)).thenReturn(true);
		when(communityAllocationRepository.exists(service.communityAllocationId)).thenReturn(true);
		when(projectAllocationRepository.exists(service.id)).thenReturn(true);
		when(projectAllocationRepository.isUniqueName(any())).thenReturn(true);
		when(projectAllocationRepository.findById(any())).thenReturn(Optional.of(service));
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(CommunityAllocation.builder().id("id").build()));

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(service));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		ProjectAllocation community = ProjectAllocation.builder()
			.id("id")
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectAllocationRepository.exists(community.id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		ProjectAllocation resourceCredit = ProjectAllocation.builder()
			.id("id")
			.projectId("id")
			.communityAllocationId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		ProjectAllocation resourceCredit2 = ProjectAllocation.builder()
			.id("id")
			.projectId("id")
			.communityAllocationId("id")
			.name("name2")
			.amount(new BigDecimal(2))
			.build();

		when(projectAllocationRepository.findById(any())).thenReturn(Optional.of(resourceCredit2));
		when(projectAllocationRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(resourceCredit));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		String id = "id";

		when(projectAllocationRepository.exists(id)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		String id = "id";

		when(projectAllocationRepository.exists(id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

}