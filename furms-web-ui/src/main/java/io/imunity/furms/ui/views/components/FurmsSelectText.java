/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.components;

import com.vaadin.flow.component.Text;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;

import java.util.Objects;

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
}
