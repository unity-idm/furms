/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.appreciated.apexcharts.config.Stroke;
import com.github.appreciated.apexcharts.config.stroke.Curve;

import java.util.List;

class MultiStroke extends Stroke {
	public List<Curve> curve;
	public List<Double> width;

	MultiStroke(List<Curve> curve, List<Double> width) {
		this.curve = curve;
		this.width = width;
	}

	@JsonProperty("curve")
	public List<Curve> getCurves() {
		return curve;
	}

	public void setCurve(List<Curve> curve) {
		this.curve = curve;
	}

	@JsonProperty("width")
	public List<Double> getWidths() {
		return width;
	}

	public void setWidth(List<Double> width) {
		this.width = width;
	}
}
