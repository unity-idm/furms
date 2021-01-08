/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.components;

import com.vaadin.flow.component.Text;
import io.imunity.furms.domain.authz.UserScopeContent;

import java.util.Objects;

public class FurmsSelectText extends Text {
	public final UserScopeContent userScopeContent;

	public FurmsSelectText(UserScopeContent userScopeContent) {
		super(userScopeContent.name);
		this.userScopeContent = userScopeContent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FurmsSelectText furmsSelectText = (FurmsSelectText) o;
		return Objects.equals(userScopeContent, furmsSelectText.userScopeContent);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userScopeContent);
	}
}
