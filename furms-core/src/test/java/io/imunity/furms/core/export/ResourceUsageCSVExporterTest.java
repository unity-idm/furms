/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.export.ResourceUsageJSONExporter;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceUsageCSVExporterTest {
	@Mock
	private ProjectAllocationRepository projectAllocationRepository;
	@Mock
	private CommunityAllocationRepository communityAllocationRepository;
	@Mock
	private ResourceUsageRepository resourceUsageRepository;

	private ResourceUsageJSONExporter exporter;

	@BeforeEach
	void setUp() {
		exporter = new ResourceUsageJSONExporterImpl(projectAllocationRepository, communityAllocationRepository,
			resourceUsageRepository, new ResourceUsageExportHelper(resourceUsageRepository, projectAllocationRepository, communityAllocationRepository));
	}

	@Test
	void shouldGenerateValidDataForCommunityAllocJson() throws IOException {
		String communityId = UUID.randomUUID().toString();
		String communityAllocationId = UUID.randomUUID().toString();
		String projectAllocationId1 = UUID.randomUUID().toString();
		String projectAllocationId2 = UUID.randomUUID().toString();
		String name = "name";
		ResourceMeasureUnit unit = ResourceMeasureUnit.KILO;

		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusDays(60);

		List<LocalDate> XTimeAxis = List.of(
			startDate,
			startDate.plusDays(3),
			startDate.plusDays(5),
			startDate.plusDays(10),
			startDate.plusDays(13),
			startDate.plusDays(18),
			startDate.plusDays(24),
			startDate.plusDays(25),
			startDate.plusDays(50)
		);

		List<Double> projectAllocations1 = List.of(20D, 40D,  50D, 80D, 90D);
		List<Double> projectAllocations2 = List.of(30D, 60D,  70D, 90D, 95D);

		Set<ResourceUsage> communityAllocationUsage = new HashSet<>();

		Iterator<LocalDate> XTimeAxisIterator = XTimeAxis.iterator();
		Iterator<Double> projectAllocation1Iterator = projectAllocations1.iterator();
		Iterator<Double> projectAllocation2Iterator = projectAllocations2.iterator();

		simulateResourceUsageInTime(projectAllocationId1, projectAllocationId2, communityAllocationUsage, XTimeAxisIterator, projectAllocation1Iterator, projectAllocation2Iterator);

		when(communityAllocationRepository.findByIdWithRelatedObjects(communityAllocationId))
			.thenReturn(Optional.of(CommunityAllocationResolved.builder()
				.name(name)
				.amount(new BigDecimal(200))
				.resourceCredit(ResourceCredit.builder()
					.utcStartTime(startDate.atStartOfDay())
					.utcEndTime(endDate.atStartOfDay())
					.build())
				.resourceType(ResourceType.builder()
					.unit(unit)
					.build())
				.build())
			);
		when(resourceUsageRepository.findResourceUsagesHistoryByCommunityAllocationId(UUID.fromString(communityAllocationId)))
			.thenReturn(communityAllocationUsage);
		when(communityAllocationRepository.findById(communityAllocationId)).thenReturn(Optional.of(
			CommunityAllocation.builder()
				.communityId(communityId)
				.build()
		));

		String jsonForCommunity = exporter.getJsonForCommunityAllocation(communityId, communityAllocationId);
		ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		CommunityResourceUsage communityResourceUsage = objectMapper.readValue(jsonForCommunity, CommunityResourceUsage.class);

		assertThat(communityResourceUsage.consumption).isEqualTo(List.of(
			new Consumption(XTimeAxis.get(1).atStartOfDay(), new BigDecimal("50.0")),
			new Consumption(XTimeAxis.get(2).atStartOfDay(), new BigDecimal("70.0")),
			new Consumption(XTimeAxis.get(3).atStartOfDay(), new BigDecimal("100.0")),
			new Consumption(XTimeAxis.get(4).atStartOfDay(), new BigDecimal("115.0")),
			new Consumption(XTimeAxis.get(4).atStartOfDay().plusMinutes(30), new BigDecimal("120.0")),
			new Consumption(XTimeAxis.get(5).atStartOfDay(), new BigDecimal("150.0")),
			new Consumption(XTimeAxis.get(6).atStartOfDay(), new BigDecimal("160.0")),
			new Consumption(XTimeAxis.get(7).atStartOfDay(), new BigDecimal("180.0")),
			new Consumption(XTimeAxis.get(8).atStartOfDay(), new BigDecimal("185.0"))
		));
	}

	private void simulateResourceUsageInTime(String projectAllocationId1, String projectAllocationId2, Set<ResourceUsage> communityAllocationUsage, Iterator<LocalDate> XTimeAxisIterator, Iterator<Double> projectAllocation1Iterator, Iterator<Double> projectAllocation2Iterator) {
		//0 -day
		XTimeAxisIterator.next();

		//3 - day
		LocalDate temp = XTimeAxisIterator.next();
		communityAllocationUsage.add(createUsage(projectAllocationId1, temp.atStartOfDay(), valueOf(projectAllocation1Iterator.next())));
		communityAllocationUsage.add(createUsage(projectAllocationId2, temp.atStartOfDay(), valueOf(projectAllocation2Iterator.next())));

		//5 & 10 - day
		communityAllocationUsage.add(createUsage(projectAllocationId1, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation1Iterator.next())));
		communityAllocationUsage.add(createUsage(projectAllocationId2, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation2Iterator.next())));

		//13 - day
		temp = XTimeAxisIterator.next();
		Double usageTemp = projectAllocation2Iterator.next();
		communityAllocationUsage.add(createUsage(projectAllocationId1, temp.atStartOfDay(), valueOf(projectAllocation1Iterator.next())));
		communityAllocationUsage.add(createUsage(projectAllocationId2, temp.atStartOfDay(), valueOf(usageTemp - 5)));
		communityAllocationUsage.add(createUsage(projectAllocationId2, temp.atStartOfDay().plusMinutes(30), valueOf(usageTemp)));

		//18 & 24 - day
		communityAllocationUsage.add(createUsage(projectAllocationId1, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation1Iterator.next())));
		communityAllocationUsage.add(createUsage(projectAllocationId1, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation1Iterator.next())));

		//25 & 50 - day
		communityAllocationUsage.add(createUsage(projectAllocationId2, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation2Iterator.next())));
		communityAllocationUsage.add(createUsage(projectAllocationId2, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation2Iterator.next())));
	}

	private ResourceUsage createUsage(String projectAllocId, LocalDateTime startDate, BigDecimal amount) {
		return ResourceUsage.builder()
			.projectAllocationId(projectAllocId)
			.cumulativeConsumption(amount)
			.probedAt(startDate)
			.build();
	}

}