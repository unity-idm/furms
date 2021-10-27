/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.branding;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.ui.components.Images;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Optional;

public class FurmsLogo extends HorizontalLayout {

	private final Image image;
	private byte[] dynamicImageContent;

	public FurmsLogo(Optional<FurmsImage> logoOpt) {
		setAlignItems(FlexComponent.Alignment.CENTER);
		setMargin(true);

		image = logoOpt
				.filter(logo -> !logo.isEmpty())
				.map(this::createDynamicResource)
				.map(logoResource -> new Image(logoResource, ""))
				.orElse(new Image(Images.FENIX_LOGO.path, ""));
		image.setWidthFull();
		add(image);
	}

	public boolean equalsLogo(FurmsLogo furmsLogo) {
		return furmsLogo != null
				&& image.getSrc() != null && furmsLogo.image.getSrc() != null
				&& (image.getSrc().equals(furmsLogo.image.getSrc())
				|| Arrays.equals(dynamicImageContent, furmsLogo.dynamicImageContent));
	}

	private StreamResource createDynamicResource(FurmsImage furmsImage) {
		dynamicImageContent = furmsImage.getImage();
		return new StreamResource("", () -> new ByteArrayInputStream(furmsImage.getImage()));
	}

}
