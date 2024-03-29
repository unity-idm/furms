/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.DataLabelsBuilder;
import com.github.appreciated.apexcharts.config.builder.FillBuilder;
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
import io.imunity.furms.ui.charts.service.UserResourceUsage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.vaadin.flow.component.icon.VaadinIcon.MENU;

public class ResourceAllocationChart extends VerticalLayout {
	private static final List<String> COLORS = List.of("#0000FF", "#FF0000", "#FFA500", "#008000", "#FFFF00", "#000000", "#800080", "#FF7F50", "#8B008B", "#FFC0CB", "#808080", "#4B0082", "#800000", "#808000");
	public final boolean disableThreshold;

	public ResourceAllocationChart(ChartData chartData, Supplier<String> jsonGetter, Supplier<String> csvGetter) {
		this(chartData, jsonGetter, csvGetter, false);
	}

	public ResourceAllocationChart(ChartData chartData, Supplier<String> jsonGetter, Supplier<String> csvGetter, boolean disableThreshold) {
		this.disableThreshold = disableThreshold;
		ApexCharts areaChart = ApexChartsBuilder.get()
			.withChart(ChartBuilder.get()
				.withType(Type.AREA)
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
			.withFill(FillBuilder.get()
				.withType("solid")
				.withColors(List.of("LightSkyBlue"))
				.withOpacity(0.01D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D)
				.build())
			.withColors(COLORS.toArray(String[]::new))
			.withTitle(TitleSubtitleBuilder.get()
				.withText(chartData.projectAllocationName)
				.withAlign(Align.LEFT).build())
			.withLabels(chartData.getAllXArguments().stream()
				.map(LocalDate::toString)
				.toArray(String[]::new)
			)
			.withXaxis(XAxisBuilder.get()
				.withType(XAxisType.DATETIME)
				.build()
			)
			.withYaxis(YAxisBuilder.get()
				.withTitle(TitleBuilder.get()
					.withText(chartData.unit)
					.build()
				)
				.build()
			)
			.withLegend(LegendBuilder.get().withHorizontalAlign(HorizontalAlign.LEFT).build())
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

		curves.add(Curve.SMOOTH);
		widths.add(5D);

		curves.add(Curve.STEPLINE);
		widths.add(5D);

		if(!(chartData.getThresholdValue() < 1 || disableThreshold)) {
			curves.add(Curve.SMOOTH);
			widths.add(5D);
		}

		for(int i = 0; i < chartData.yUsersUsageLinesValues.size(); i++) {
			curves.add(Curve.SMOOTH);
			widths.add(2D);
		}

		MultiStroke multiStroke = new MultiStroke(curves, widths);
		multiStroke.setColors(COLORS);
		return multiStroke;
	}

	private Series<?>[] createSeries(ChartData chartData) {
		List<Series<Object>> series = new ArrayList<>();

		series.add(new Series<>(getTranslation("chart.series.consumption"), SeriesType.AREA,
			chartData.yResourceUsageLineValues.toArray()));
		if(!chartData.yChunkLineValues.isEmpty())
			series.add(new Series<>(getTranslation("chart.series.chunk"), SeriesType.LINE, chartData.yChunkLineValues.toArray()));
		if(!(chartData.getThresholdValue() < 1 || disableThreshold))
			series.add(new Series<>(getTranslation("chart.series.threshold"), SeriesType.LINE, chartData.yThresholdLineValues.toArray()));
		for(UserResourceUsage userResourceUsage : chartData.yUsersUsageLinesValues){
			series.add(new Series<>(userResourceUsage.userEmail, SeriesType.LINE,
				userResourceUsage.yUserCumulativeUsageValues.toArray()));
		}

		return series.toArray(Series[]::new);
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

