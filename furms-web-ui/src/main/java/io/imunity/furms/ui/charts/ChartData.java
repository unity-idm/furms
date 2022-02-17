/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import io.imunity.furms.ui.charts.service.UserResourceUsage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ChartData {
	public final String unit;
	public final String projectAllocationName;
	public final LocalDate endTime;
	public final List<Double> yResourceUsageLineValues;
	public final List<Double> yChunkLineValues;
	public final List<Double> yThresholdLineValues;
	public final List<UserResourceUsage> yUsersUsageLinesValues;
	public final List<LocalDate> xArguments;

	private ChartData(String unit, String projectAllocationName, LocalDate endTime,
	                  List<Double> yResourceUsageLineValues, List<Double> yChunkLineValues, List<Double> yThresholdLineValues, List<LocalDate> xArguments,
	                  List<UserResourceUsage> yUsersUsageLinesValues) {
		this.unit = unit;
		this.projectAllocationName = projectAllocationName;
		this.endTime = endTime;
		this.yResourceUsageLineValues = Collections.unmodifiableList(yResourceUsageLineValues);
		this.yChunkLineValues = Collections.unmodifiableList(yChunkLineValues);
		this.yThresholdLineValues = Collections.unmodifiableList(yThresholdLineValues);
		this.yUsersUsageLinesValues = Collections.unmodifiableList(yUsersUsageLinesValues);
		this.xArguments = xArguments;
	}

	double getThresholdValue() {
		Iterator<Double> iterator = yThresholdLineValues.iterator();
		if(iterator.hasNext())
			return iterator.next();
		return 0;
	}

	List<LocalDate> getAllXArguments() {
		List<LocalDate> times =  new ArrayList<>(this.xArguments);
		times.add(endTime);
		return times;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ChartData chartData = (ChartData) o;
		return Objects.equals(unit, chartData.unit) &&
			Objects.equals(projectAllocationName, chartData.projectAllocationName) &&
			Objects.equals(endTime, chartData.endTime) &&
			Objects.equals(yResourceUsageLineValues, chartData.yResourceUsageLineValues) &&
			Objects.equals(yChunkLineValues, chartData.yChunkLineValues) &&
			Objects.equals(yThresholdLineValues, chartData.yThresholdLineValues) &&
			Objects.equals(yUsersUsageLinesValues, chartData.yUsersUsageLinesValues) &&
			Objects.equals(xArguments, chartData.xArguments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(unit, projectAllocationName, endTime, yResourceUsageLineValues, yChunkLineValues, yThresholdLineValues, xArguments, yUsersUsageLinesValues);
	}

	@Override
	public String toString() {
		return "ChartData{" +
			", unit='" + unit + '\'' +
			", projectAllocationName='" + projectAllocationName + '\'' +
			", endTime=" + endTime +
			", yResourceUsageLineValues=" + yResourceUsageLineValues +
			", yChunkLineValues=" + yChunkLineValues +
			", yThresholdLineValues=" + yThresholdLineValues +
			", yUsersUsageLinesValues=" + yUsersUsageLinesValues +
			", xArguments=" + xArguments +
			'}';
	}

	public static ChartDataBuilder builder() {
		return new ChartDataBuilder();
	}

	public static final class ChartDataBuilder {
		private String unit;
		private String projectAllocationName;
		private LocalDate endTime;
		private List<Double> yResourceUsageLineValues = List.of();
		private List<Double> yChunkLineValues = List.of();
		private List<Double> yThresholdLineValues = List.of();
		private List<UserResourceUsage> yUsersUsagesValues = List.of();
		private List<LocalDate> xArguments = List.of();


		private ChartDataBuilder() {
		}

		public ChartDataBuilder unit(String unit) {
			this.unit = unit;
			return this;
		}

		public ChartDataBuilder projectAllocationName(String projectAllocationName) {
			this.projectAllocationName = projectAllocationName;
			return this;
		}

		public ChartDataBuilder endTime(LocalDate endTime) {
			this.endTime = endTime;
			return this;
		}

		public ChartDataBuilder yResourceUsageLineValues(List<Double> resourceUsages) {
			this.yResourceUsageLineValues = resourceUsages;
			return this;
		}

		public ChartDataBuilder yChunkLineValues(List<Double> chunks) {
			this.yChunkLineValues = chunks;
			return this;
		}

		public ChartDataBuilder yThresholdLineValues(List<Double> thresholds) {
			this.yThresholdLineValues = thresholds;
			return this;
		}

		public ChartDataBuilder xArguments(List<LocalDate> times) {
			this.xArguments = times;
			return this;
		}

		public ChartDataBuilder yUsersUsagesValues(List<UserResourceUsage> usersUsages) {
			this.yUsersUsagesValues = usersUsages;
			return this;
		}

		public ChartData build() {
			return new ChartData(unit, projectAllocationName, endTime, yResourceUsageLineValues, yChunkLineValues, yThresholdLineValues, xArguments, yUsersUsagesValues);
		}
	}
}
