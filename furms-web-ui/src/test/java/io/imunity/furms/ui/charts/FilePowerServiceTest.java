/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilePowerServiceTest {
	@Mock
	private ProjectAllocationService projectAllocationService;
	@Mock
	private CommunityAllocationService communityAllocationService;
	@Mock
	private ResourceUsageService resourceUsageService;

	@InjectMocks
	private FilePowerService filePowerService;

	@Test
	void shouldGenerateValidDataForCommunityAllocCsv() {
		String communityId = "communityId";
		String communityAllocationId = "projectAllocationId";
		String projectAllocationId1 = "projectAllocationId1";
		String projectAllocationId2 = "projectAllocationId2";
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
		communityAllocationUsage.add(createUsage(projectAllocationId2, temp.atStartOfDay(), valueOf(usageTemp - 2)));
		communityAllocationUsage.add(createUsage(projectAllocationId2, temp.atStartOfDay(), valueOf(usageTemp - 5)));
		communityAllocationUsage.add(createUsage(projectAllocationId2, temp.atStartOfDay(), valueOf(usageTemp)));

		//18 & 24 - day
		communityAllocationUsage.add(createUsage(projectAllocationId1, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation1Iterator.next())));
		communityAllocationUsage.add(createUsage(projectAllocationId1, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation1Iterator.next())));

		//25 & 50 - day
		communityAllocationUsage.add(createUsage(projectAllocationId2, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation2Iterator.next())));
		communityAllocationUsage.add(createUsage(projectAllocationId2, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation2Iterator.next())));


		when(communityAllocationService.findByIdWithRelatedObjects(communityAllocationId))
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
		when(resourceUsageService.findAllResourceUsageHistoryByCommunity(communityId, communityAllocationId))
			.thenReturn(communityAllocationUsage);

		byte[] csvFileForCommunity = filePowerService.getCsvFileForCommunity(communityId, communityAllocationId);

		String header = "Allocation,Consumption until,Amount,Unit" + "\r\n";
		String row1 = name + "," + XTimeAxis.get(1).atStartOfDay() +  ",50.0," +  unit.getSuffix() + "\r\n";
		String row2 = name + "," + XTimeAxis.get(2).atStartOfDay() +  ",70.0," +  unit.getSuffix() + "\r\n";
		String row3 = name + "," + XTimeAxis.get(3).atStartOfDay() +  ",100.0," + unit.getSuffix() + "\r\n";
		String row4 = name + "," + XTimeAxis.get(4).atStartOfDay() +  ",120.0," + unit.getSuffix() + "\r\n";
		String row5 = name + "," + XTimeAxis.get(5).atStartOfDay() +  ",150.0," + unit.getSuffix() + "\r\n";
		String row6 = name + "," + XTimeAxis.get(6).atStartOfDay() +  ",160.0," + unit.getSuffix() + "\r\n";
		String row7 = name + "," + XTimeAxis.get(7).atStartOfDay() +  ",180.0," + unit.getSuffix() + "\r\n";
		String row8 = name + "," + XTimeAxis.get(8).atStartOfDay() +  ",185.0," + unit.getSuffix();

		assertThat(new String(csvFileForCommunity)).isEqualTo((header + row1 + row2 + row3 + row4 + row5 + row6 + row7 + row8));
	}

	@Test
	void shouldGenerateValidDataForCommunityAllocJson() throws IOException {
		String communityId = "communityId";
		String communityAllocationId = "projectAllocationId";
		String projectAllocationId1 = "projectAllocationId1";
		String projectAllocationId2 = "projectAllocationId2";
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
		communityAllocationUsage.add(createUsage(projectAllocationId2, temp.atStartOfDay(), valueOf(usageTemp - 2)));
		communityAllocationUsage.add(createUsage(projectAllocationId2, temp.atStartOfDay(), valueOf(usageTemp - 5)));
		communityAllocationUsage.add(createUsage(projectAllocationId2, temp.atStartOfDay(), valueOf(usageTemp)));

		//18 & 24 - day
		communityAllocationUsage.add(createUsage(projectAllocationId1, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation1Iterator.next())));
		communityAllocationUsage.add(createUsage(projectAllocationId1, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation1Iterator.next())));

		//25 & 50 - day
		communityAllocationUsage.add(createUsage(projectAllocationId2, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation2Iterator.next())));
		communityAllocationUsage.add(createUsage(projectAllocationId2, XTimeAxisIterator.next().atStartOfDay(), valueOf(projectAllocation2Iterator.next())));


		when(communityAllocationService.findByIdWithRelatedObjects(communityAllocationId))
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
		when(resourceUsageService.findAllResourceUsageHistoryByCommunity(communityId, communityAllocationId))
			.thenReturn(communityAllocationUsage);

		byte[] jsonFileForCommunity = filePowerService.getJsonFileForCommunity(communityId, communityAllocationId);
		ObjectMapper andRegisterModules = new ObjectMapper().findAndRegisterModules();
		CommunityResourceUsage communityResourceUsage = andRegisterModules.readValue(jsonFileForCommunity, CommunityResourceUsage.class);

		assertThat(communityResourceUsage.consumption).isEqualTo(List.of(
			new Consumption(XTimeAxis.get(1).atStartOfDay(), new BigDecimal("50.0")),
			new Consumption(XTimeAxis.get(2).atStartOfDay(), new BigDecimal("70.0")),
			new Consumption(XTimeAxis.get(3).atStartOfDay(), new BigDecimal("100.0")),
			new Consumption(XTimeAxis.get(4).atStartOfDay(), new BigDecimal("120.0")),
			new Consumption(XTimeAxis.get(5).atStartOfDay(), new BigDecimal("150.0")),
			new Consumption(XTimeAxis.get(6).atStartOfDay(), new BigDecimal("160.0")),
			new Consumption(XTimeAxis.get(7).atStartOfDay(), new BigDecimal("180.0")),
			new Consumption(XTimeAxis.get(8).atStartOfDay(), new BigDecimal("185.0"))
		));
	}

	private ResourceUsage createUsage(String projectAllocId, LocalDateTime startDate, BigDecimal amount) {
		return ResourceUsage.builder()
			.projectAllocationId(projectAllocId)
			.cumulativeConsumption(amount)
			.probedAt(startDate)
			.build();
	}

}