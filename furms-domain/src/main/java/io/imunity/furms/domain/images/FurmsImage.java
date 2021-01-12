/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.images;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class FurmsImage {
	private final byte[] image;
	private final FurmsImageExtension type;

	public FurmsImage(byte[] logoImage, FurmsImageExtension type) {
		this.image = logoImage;
		this.type = type;
	}

	public FurmsImage(byte[] logoImage, String type) {
		this.image = logoImage;
		this.type = Optional.ofNullable(type)
			.map(String::toUpperCase)
			.map(FurmsImageExtension::valueOf)
			.orElse(null);
	}

	public byte[] getImage() {
		return image;
	}

	public String getType() {
		return Optional.ofNullable(type)
			.map(Enum::name)
			.map(String::toLowerCase)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FurmsImage that = (FurmsImage) o;
		return Arrays.equals(image, that.image) &&
			type == that.type;
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(type);
		result = 31 * result + Arrays.hashCode(image);
		return result;
	}

	@Override
	public String toString() {
		return "FurmsImage{" +
			"logoImage=" + Arrays.toString(image) +
			", type=" + type +
			'}';
	}
}
