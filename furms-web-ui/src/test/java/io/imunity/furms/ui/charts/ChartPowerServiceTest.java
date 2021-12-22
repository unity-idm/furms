/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.users.FenixUserId;
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
	private CommunityAllocationService communityAllocationService;
	@Mock
	private AlarmService alarmService;
	@Mock
	private ResourceUsageService resourceUsageService;
	@Mock
	private UserService userService;

	@InjectMocks
	private ChartPowerService chartPowerService;

	@Test
	void shouldGenerateValidDataForProjectAllocChart() {
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
		resourceUsages.add(createUsage(temp.atStartOfDay().plusMinutes(10), valueOf(usageTemp - 2)));
		resourceUsages.add(createUsage(temp.atStartOfDay().plusMinutes(20), valueOf(usageTemp - 5)));
		resourceUsages.add(createUsage(temp.atStartOfDay().plusMinutes(30), valueOf(usageTemp)));

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

		ChartData chartData = chartPowerService.getChartDataForProjectAlloc(projectId, projectAllocationId);

		assertThat(chartData.unit).isEqualTo(unit.getSuffix());
		assertThat(chartData.projectAllocationName).isEqualTo(name);
		assertThat(chartData.threshold).isEqualTo(70);
		assertThat(chartData.endTime).isEqualTo(endDate);
		assertThat(chartData.times).isEqualTo(XTimeAxis);
		assertThat(chartData.chunks).isEqualTo(List.of(
			0D, 20D, 20D, 20D, 60D, 60D, 60D, 120D, 210D
		));
		assertThat(chartData.resourceUsages).isEqualTo(List.of(
			0D, 0D, 3D, 7D, 20D, 30D, 50D, 50D, 50D
		));
		assertThat(chartData.thresholds).isEqualTo(List.of(
			70D, 70D, 70D, 70D, 70D, 70D, 70D, 70D, 70D
		));
	}

	@Test
	void shouldGenerateValidDataForProjectAllocChartWithUserUsages() {
		String projectId = "projectId";
		String projectAllocationId = "projectAllocationId";
		String name = "name";
		ResourceMeasureUnit unit = ResourceMeasureUnit.KILO;
		FenixUserId user1 = new FenixUserId("user1");
		FenixUserId user2 = new FenixUserId("user2");
		FenixUserId user3 = new FenixUserId("user3");

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
		List<Double> YUsersUsageAxis = List.of(1D, 2D, 3D, 4D, 10D, 8D, 13D, 20D, 15D, 19D);

		Set<ProjectAllocationChunk> allocationChunks = new HashSet<>();
		Set<ResourceUsage> resourceUsages = new HashSet<>();
		Set<UserResourceUsage> usersResourceUsages = new HashSet<>();


		Iterator<LocalDate> XTimeAxisIterator = XTimeAxis.iterator();
		Iterator<Double> YChunkAxisIterator = YChunkAxis.iterator();
		Iterator<Double> YUsageAxisIterator = YUsageAxis.iterator();
		Iterator<Double> YUsersUsageAxisIterator = YUsersUsageAxis.iterator();

		//0 -day
		XTimeAxisIterator.next();

		//3 - day
		LocalDate temp = XTimeAxisIterator.next();
		allocationChunks.add(createChunk(temp.atStartOfDay(), valueOf(YChunkAxisIterator.next())));
		usersResourceUsages.add(createUserUsage(projectAllocationId, temp.atStartOfDay(), valueOf(YUsersUsageAxisIterator.next()), user1));
		usersResourceUsages.add(createUserUsage(projectAllocationId, temp.atStartOfDay(), valueOf(YUsersUsageAxisIterator.next()), user3));

		//5 & 10 - day
		temp = XTimeAxisIterator.next();
		resourceUsages.add(createUsage(temp.atStartOfDay(), valueOf(YUsageAxisIterator.next())));
		usersResourceUsages.add(createUserUsage(projectAllocationId, temp.atStartOfDay(), valueOf(YUsersUsageAxisIterator.next()), user2));

		temp = XTimeAxisIterator.next();
		resourceUsages.add(createUsage(temp.atStartOfDay(), valueOf(YUsageAxisIterator.next())));
		usersResourceUsages.add(createUserUsage(projectAllocationId, temp.atStartOfDay(), valueOf(YUsersUsageAxisIterator.next()), user3));

		//13 - day
		temp = XTimeAxisIterator.next();
		Double usageTemp = YUsageAxisIterator.next();
		allocationChunks.add(createChunk(temp.atStartOfDay(), valueOf(YChunkAxisIterator.next())));
		resourceUsages.add(createUsage(temp.atStartOfDay().plusMinutes(10), valueOf(usageTemp - 2)));
		resourceUsages.add(createUsage(temp.atStartOfDay().plusMinutes(20), valueOf(usageTemp - 5)));
		resourceUsages.add(createUsage(temp.atStartOfDay().plusMinutes(30), valueOf(usageTemp)));
		Double tmp = YUsersUsageAxisIterator.next();
		usersResourceUsages.add(createUserUsage(projectAllocationId, temp.atStartOfDay(), valueOf(tmp - 2), user1));
		usersResourceUsages.add(createUserUsage(projectAllocationId, temp.atStartOfDay().plusMinutes(10), valueOf(tmp), user1));
		usersResourceUsages.add(createUserUsage(projectAllocationId, temp.atStartOfDay(), valueOf(YUsersUsageAxisIterator.next()), user2));

		//18 & 24 - day
		temp = XTimeAxisIterator.next();
		resourceUsages.add(createUsage(temp.atStartOfDay(), valueOf(YUsageAxisIterator.next())));
		usersResourceUsages.add(createUserUsage(projectAllocationId, temp.atStartOfDay(), valueOf(YUsersUsageAxisIterator.next()), user3));

		temp = XTimeAxisIterator.next();
		resourceUsages.add(createUsage(temp.atStartOfDay(), valueOf(YUsageAxisIterator.next())));
		usersResourceUsages.add(createUserUsage(projectAllocationId, temp.atStartOfDay(), valueOf(YUsersUsageAxisIterator.next()), user1));

		//25 & 50 - day
		temp = XTimeAxisIterator.next();
		allocationChunks.add(createChunk(temp.atStartOfDay(), valueOf(YChunkAxisIterator.next())));
		usersResourceUsages.add(createUserUsage(projectAllocationId, temp.atStartOfDay(), valueOf(YUsersUsageAxisIterator.next()), user2));

		temp = XTimeAxisIterator.next();
		allocationChunks.add(createChunk(temp.atStartOfDay(), valueOf(YChunkAxisIterator.next())));
		usersResourceUsages.add(createUserUsage(projectAllocationId, temp.atStartOfDay(), valueOf(YUsersUsageAxisIterator.next()), user3));

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
		when(resourceUsageService.findAllUserUsagesHistory(projectId, projectAllocationId))
			.thenReturn(usersResourceUsages);

		ChartData chartData = chartPowerService.getChartDataForProjectAllocWithUserUsages(projectId, projectAllocationId);

		assertThat(chartData.unit).isEqualTo(unit.getSuffix());
		assertThat(chartData.projectAllocationName).isEqualTo(name);
		assertThat(chartData.threshold).isEqualTo(70);
		assertThat(chartData.endTime).isEqualTo(endDate);
		assertThat(chartData.times).isEqualTo(XTimeAxis);
		assertThat(chartData.chunks).isEqualTo(List.of(
			0D, 20D, 20D, 20D, 60D, 60D, 60D, 120D, 210D
		));
		assertThat(chartData.resourceUsages).isEqualTo(List.of(
			0D, 0D, 3D, 7D, 20D, 30D, 50D, 50D, 50D
		));
		assertThat(chartData.thresholds).isEqualTo(List.of(
			70D, 70D, 70D, 70D, 70D, 70D, 70D, 70D, 70D
		));
		assertThat(chartData.usersUsages).isEqualTo(List.of(
			new UserUsage("user1", List.of(0D, 1D, 1D, 1D, 10D, 10D, 20D, 20D, 20D)),
			new UserUsage("user2", List.of(0D, 0D, 3D, 3D, 8D, 8D, 8D, 15D, 15D)),
			new UserUsage("user3", List.of(0D, 2D, 2D, 4D, 4D, 13D, 13D, 13D, 19D))
		));
	}

	@Test
	void shouldGenerateValidDataForCommunityAllocChart() {
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
		communityAllocationUsage.add(createUsage(projectAllocationId2, temp.atStartOfDay().plusMinutes(30), valueOf(usageTemp)));

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

		ChartData chartData = chartPowerService.getChartDataForCommunityAlloc(communityId, communityAllocationId);

		assertThat(chartData.unit).isEqualTo(unit.getSuffix());
		assertThat(chartData.projectAllocationName).isEqualTo(name);
		assertThat(chartData.threshold).isEqualTo(0);
		assertThat(chartData.endTime).isEqualTo(endDate);
		assertThat(chartData.times).isEqualTo(XTimeAxis);
		assertThat(chartData.chunks).isEqualTo(List.of());
		assertThat(chartData.thresholds).isEqualTo(List.of());
		assertThat(chartData.resourceUsages).isEqualTo(List.of(
			0D, 50D, 70D, 100D, 120D, 150D, 160D, 180D, 185D
		));
	}

	private ResourceUsage createUsage(LocalDateTime startDate, BigDecimal amount) {
		return ResourceUsage.builder()
			.cumulativeConsumption(amount)
			.probedAt(startDate)
			.build();
	}

	private ResourceUsage createUsage(String projectAllocId, LocalDateTime startDate, BigDecimal amount) {
		return ResourceUsage.builder()
			.projectAllocationId(projectAllocId)
			.cumulativeConsumption(amount)
			.probedAt(startDate)
			.build();
	}

	private UserResourceUsage createUserUsage(String projectAllocId, LocalDateTime startDate, BigDecimal amount, FenixUserId userId) {
		return UserResourceUsage.builder()
			.projectAllocationId(projectAllocId)
			.cumulativeConsumption(amount)
			.consumedUntil(startDate)
			.fenixUserId(userId)
			.build();
	}

	private ProjectAllocationChunk createChunk(LocalDateTime startDate, BigDecimal amount) {
		return ProjectAllocationChunk.builder()
			.amount(amount)
			.validFrom(startDate)
			.build();
	}
}