/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.vaadin.flow.component.Text;

import io.imunity.furms.ui.user_context.FurmsViewUserContext;

public class FurmsSelectText extends Text {
	public final FurmsViewUserContext furmsViewUserContext;

	public FurmsSelectText(FurmsViewUserContext furmsViewUserContext) {
		super(furmsViewUserContext.name);
		this.furmsViewUserContext = furmsViewUserContext;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FurmsSelectText furmsSelectText = (FurmsSelectText) o;
		return Objects.equals(furmsViewUserContext, furmsSelectText.furmsViewUserContext);
	}

	@Override
	public int hashCode() {
		return Objects.hash(furmsViewUserContext);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("super", super.toString())
				.add("furmsViewUserContext", furmsViewUserContext).toString();
	}
}
