/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class ChartPowerServiceTest {

	@Mock
	private ProjectAllocationService projectAllocationService;
	@Mock
	private AlarmService alarmService;
	@Mock
	private ResourceUsageService resourceUsageService;

	@InjectMocks
	private ChartPowerService chartPowerService;

	@Test
	void shouldGenerateValidDataForChart() {
		String projectId = "projectId";
		String projectAllocationId = "projectAllocationId";
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

		List<Double> YChunkAxis = List.of(20D, 40D, 60D, 90D);
		List<Double> YUsageAxis = List.of(3D, 7D, 20D, 30D, 50D);

		Set<ProjectAllocationChunk> allocationChunks = new HashSet<>();
		Set<ResourceUsage> resourceUsages = new HashSet<>();

		Iterator<LocalDate> XTimeAxisIterator = XTimeAxis.iterator();
		Iterator<Double> YChunkAxisIterator = YChunkAxis.iterator();
		Iterator<Double> YUsageAxisIterator = YUsageAxis.iterator();

		//0 -day
		XTimeAxisIterator.next();

		//3 - day
		allocationChunks.add(createChunk(XTimeAxisIterator.next().atStartOfDay(), valueOf(YChunkAxisIterator.next())));

		//5 & 10 - day
		resourceUsages.add(createUsage(XTimeAxisIterator.next().atStartOfDay(), valueOf(YUsageAxisIterator.next())));
		resourceUsages.add(createUsage(XTimeAxisIterator.next().atStartOfDay(), valueOf(YUsageAxisIterator.next())));

		//13 - day
		LocalDate temp = XTimeAxisIterator.next();
		Double usageTemp = YUsageAxisIterator.next();
		allocationChunks.add(createChunk(temp.atStartOfDay(), valueOf(YChunkAxisIterator.next())));
		resourceUsages.add(createUsage(temp.atStartOfDay(), valueOf(usageTemp - 2)));
		resourceUsages.add(createUsage(temp.atStartOfDay(), valueOf(usageTemp - 5)));
		resourceUsages.add(createUsage(temp.atStartOfDay(), valueOf(usageTemp)));

		//18 & 24 - day
		resourceUsages.add(createUsage(XTimeAxisIterator.next().atStartOfDay(), valueOf(YUsageAxisIterator.next())));
		resourceUsages.add(createUsage(XTimeAxisIterator.next().atStartOfDay(), valueOf(YUsageAxisIterator.next())));

		//25 & 50 - day
		allocationChunks.add(createChunk(XTimeAxisIterator.next().atStartOfDay(), valueOf(YChunkAxisIterator.next())));
		allocationChunks.add(createChunk(XTimeAxisIterator.next().atStartOfDay(), valueOf(YChunkAxisIterator.next())));


		when(projectAllocationService.findByIdValidatingProjectsWithRelatedObjects(projectAllocationId, projectId))
			.thenReturn(Optional.of(ProjectAllocationResolved.builder()
				.name(name)
				.amount(new BigDecimal(100))
				.resourceCredit(ResourceCredit.builder()
					.utcStartTime(startDate.atStartOfDay())
					.utcEndTime(endDate.atStartOfDay())
					.build())
				.resourceType(ResourceType.builder()
					.unit(unit)
					.build())
				.build())
			);
		when(alarmService.find(projectId, projectAllocationId))
			.thenReturn(Optional.of(
					AlarmWithUserEmails.builder()
						.threshold(70)
						.build()
				)
			);
		when(projectAllocationService.findAllChunks(projectId, projectAllocationId))
			.thenReturn(allocationChunks);
		when(resourceUsageService.findAllResourceUsageHistory(projectId, projectAllocationId))
			.thenReturn(resourceUsages);

		ChartData chartData = chartPowerService.generate(projectId, projectAllocationId);

		assertThat(chartData.unit).isEqualTo(unit.getSuffix());
		assertThat(chartData.projectAllocationName).isEqualTo(name);
		assertThat(chartData.threshold).isEqualTo(70);
		assertThat(chartData.endTime).isEqualTo(endDate);
		assertThat(chartData.times).isEqualTo(XTimeAxis);
		assertThat(chartData.chunks).isEqualTo(List.of(
			0D, 20D, 20D, 20D, 40D, 40D, 40D, 60D, 90D
		));
		assertThat(chartData.resourceUsages).isEqualTo(List.of(
			0D, 0D, 3D, 7D, 20D, 30D, 50D, 50D, 50D
		));
		assertThat(chartData.thresholds).isEqualTo(List.of(
			70D, 70D, 70D, 70D, 70D, 70D, 70D, 70D, 70D
		));
	}

	private ResourceUsage createUsage(LocalDateTime startDate, BigDecimal amount) {
		return ResourceUsage.builder()
			.cumulativeConsumption(amount)
			.probedAt(startDate)
			.build();
	}

	private ProjectAllocationChunk createChunk(LocalDateTime startDate, BigDecimal amount) {
		return ProjectAllocationChunk.builder()
			.amount(amount)
			.receivedTime(startDate)
			.build();
	}
}