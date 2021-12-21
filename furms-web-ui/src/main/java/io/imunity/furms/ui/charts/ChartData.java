/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChartData {
	public final double threshold;
	public final String unit;
	public final String projectAllocationName;
	public final LocalDate endTime;
	public final List<Double> resourceUsages;
	public final List<Double> chunks;
	public final List<Double> thresholds;
	public final List<LocalDate> times;
	public final List<UserUsage> usersUsages;

	private ChartData(double threshold, String unit, String projectAllocationName, LocalDate endTime,
	                  List<Double> resourceUsages, List<Double> chunks, List<Double> thresholds, List<LocalDate> times,
	                  List<UserUsage> usersUsages) {
		this.threshold = threshold;
		this.unit = unit;
		this.projectAllocationName = projectAllocationName;
		this.endTime = endTime;
		this.resourceUsages = resourceUsages;
		this.chunks = chunks;
		this.thresholds = thresholds;
		this.times = times;
		this.usersUsages = usersUsages;
	}

	List<LocalDate> getFullTimes() {
		List<LocalDate> times =  new ArrayList<>(this.times);
		times.add(endTime);
		return times;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ChartData chartData = (ChartData) o;
		return Double.compare(chartData.threshold, threshold) == 0 &&
			Objects.equals(unit, chartData.unit) &&
			Objects.equals(projectAllocationName, chartData.projectAllocationName) &&
			Objects.equals(endTime, chartData.endTime) &&
			Objects.equals(resourceUsages, chartData.resourceUsages) &&
			Objects.equals(chunks, chartData.chunks) &&
			Objects.equals(thresholds, chartData.thresholds) &&
			Objects.equals(usersUsages, chartData.usersUsages) &&
			Objects.equals(times, chartData.times);
	}

	@Override
	public int hashCode() {
		return Objects.hash(threshold, unit, projectAllocationName, endTime, resourceUsages, chunks, thresholds, times, usersUsages);
	}

	@Override
	public String toString() {
		return "ChartData{" +
			"threshold=" + threshold +
			", unit='" + unit + '\'' +
			", projectAllocationName='" + projectAllocationName + '\'' +
			", endTime=" + endTime +
			", resourceUsages=" + resourceUsages +
			", chunks=" + chunks +
			", thresholds=" + thresholds +
			", times=" + times +
			", usersUsages=" + usersUsages +
			'}';
	}

	public static ChartDataBuilder builder() {
		return new ChartDataBuilder();
	}

	public static final class ChartDataBuilder {
		private double threshold;
		private String unit;
		private String projectAllocationName;
		private LocalDate endTime;
		private List<Double> resourceUsages = List.of();
		private List<Double> chunks = List.of();
		private List<Double> thresholds = List.of();
		private List<LocalDate> times = List.of();
		public List<UserUsage> usersUsages = List.of();


		private ChartDataBuilder() {
		}

		public ChartDataBuilder threshold(double threshold) {
			this.threshold = threshold;
			return this;
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

		public ChartDataBuilder resourceUsages(List<Double> resourceUsages) {
			this.resourceUsages = resourceUsages;
			return this;
		}

		public ChartDataBuilder chunks(List<Double> chunks) {
			this.chunks = chunks;
			return this;
		}

		public ChartDataBuilder thresholds(List<Double> thresholds) {
			this.thresholds = thresholds;
			return this;
		}

		public ChartDataBuilder times(List<LocalDate> times) {
			this.times = times;
			return this;
		}

		public ChartDataBuilder usersUsages(List<UserUsage> usersUsages) {
			this.usersUsages = usersUsages;
			return this;
		}

		public ChartData build() {
			return new ChartData(threshold, unit, projectAllocationName, endTime, resourceUsages, chunks, thresholds, times, usersUsages);
		}
	}
}
