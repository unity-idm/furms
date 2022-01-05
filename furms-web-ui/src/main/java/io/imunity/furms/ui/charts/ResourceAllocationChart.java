/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.Annotations;
import com.github.appreciated.apexcharts.config.annotations.builder.YAxisAnnotationsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.DataLabelsBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.TitleSubtitleBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.builder.YAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.chart.toolbar.builder.ToolsBuilder;
import com.github.appreciated.apexcharts.config.legend.HorizontalAlign;
import com.github.appreciated.apexcharts.config.series.SeriesType;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.subtitle.Align;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.config.yaxis.builder.TitleBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.vaadin.flow.component.icon.VaadinIcon.MENU;

public class ResourceAllocationChart extends VerticalLayout {
	public final boolean disableThreshold;

	public ResourceAllocationChart(ChartData chartData, Supplier<String> jsonGetter, Supplier<String> csvGetter) {
		this(chartData, jsonGetter, csvGetter, false);
	}

	public ResourceAllocationChart(ChartData chartData, Supplier<String> jsonGetter, Supplier<String> csvGetter, boolean disableThreshold) {
		this.disableThreshold = disableThreshold;
		ApexCharts areaChart = ApexChartsBuilder.get()
			.withChart(ChartBuilder.get()
				.withType(Type.area)
				.withZoom(ZoomBuilder.get()
					.withEnabled(true)
					.build())
				.withToolbar(ToolbarBuilder.get()
					.withTools(ToolsBuilder.get()
						.withDownload("")
						.build())
					.build()
				)
				.build())
			.withDataLabels(DataLabelsBuilder.get()
				.withEnabled(false)
				.build())
			.withStroke(getStroke(chartData))
			.withSeries(createSeries(chartData))
			.withColors("blue", "red", "orange", "green", "yellow", "black", "purple", "coral", "darkmagenta", "pink", "grey", "indigo", "maroon", "olive")
			.withTitle(TitleSubtitleBuilder.get()
				.withText(chartData.projectAllocationName)
				.withAlign(Align.left).build())
			.withLabels(chartData.getFullTimes().stream()
				.map(LocalDate::toString)
				.toArray(String[]::new)
			)
			.withXaxis(XAxisBuilder.get()
				.withType(XAxisType.datetime)
				.build()
			)
			.withYaxis(YAxisBuilder.get()
				.withTitle(TitleBuilder.get()
					.withText(chartData.unit)
					.build()
				)
				.build()
			)
			.withAnnotations(chartData.threshold > 0 ? getAnnotations(chartData.threshold) : null)
			.withLegend(LegendBuilder.get().withHorizontalAlign(HorizontalAlign.left).build())
			.build();

		Component contextMenu = new ChartContextMenu(chartData, jsonGetter, csvGetter);
		add(contextMenu, areaChart);

		setWidth("70%");
		setAlignItems(Alignment.END);
		setSpacing(false);
	}

	private MultiStroke getStroke(ChartData chartData) {
		List<Curve> curves = new ArrayList<>();
		List<Double> widths = new ArrayList<>();

		curves.add(Curve.smooth);
		curves.add(Curve.stepline);
		curves.add(Curve.smooth);

		widths.add(5D);
		widths.add(5D);

		if(!(chartData.threshold < 1 || disableThreshold)) {
			curves.add(Curve.smooth);
			widths.add(1D);
		}
		for(int i = 0; i < chartData.usersUsages.size(); i++) {
			curves.add(Curve.smooth);
			widths.add(1D);
		}
		return new MultiStroke(curves, widths);
	}

	private Series<?>[] createSeries(ChartData chartData) {
		List<Series<Object>> series = new ArrayList<>();

		series.add(new Series<>(getTranslation("chart.series.consumption"), SeriesType.area, chartData.resourceUsages.toArray()));
		if(!chartData.chunks.isEmpty())
			series.add(new Series<>(getTranslation("chart.series.chunk"), SeriesType.area, chartData.chunks.toArray()));
		if(!(chartData.threshold < 1 || disableThreshold))
			series.add(new Series<>(getTranslation("chart.series.threshold"), SeriesType.line, chartData.thresholds.toArray()));
		for(UserUsage userUsage : chartData.usersUsages){
			series.add(new Series<>(userUsage.email, SeriesType.line, userUsage.usages.toArray()));
		}

		return series.toArray(Series[]::new);
	}

	private Annotations getAnnotations(double threshold) {
		Annotations annotations = new Annotations();
		annotations.setYaxis(List.of(
			YAxisAnnotationsBuilder.get()
				.withBorderColor("orange")
				.withY(threshold)
				.withStrokeDashArray(5000D)
				.build()

		));
		return annotations;
	}

	@CssImport("./styles/components/grid-action-menu.css")
	public static class ChartGridActionMenu extends ContextMenu {
		public ChartGridActionMenu() {
			Icon icon = MENU.create();
			icon.setSize("15px");
			setTarget(new Div(icon));
			setOpenOnClick(true);
			addClassName("grid-action-menu");
			getStyle().set("margin-right", "0.6em");
		}
	}

}

