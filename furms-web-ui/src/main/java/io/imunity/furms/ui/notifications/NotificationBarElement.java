/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.notifications;


import com.vaadin.flow.component.Component;
import io.imunity.furms.ui.user_context.ViewMode;

import java.util.Objects;
import java.util.Optional;

public class NotificationBarElement {
	public final String text;
	public final ViewMode viewMode;
	public final Class<? extends Component> redirect;
	public final Optional<String> resourceId;

	NotificationBarElement(String text, ViewMode viewMode, Class<? extends Component> redirect) {
		this.text = text;
		this.viewMode = viewMode;
		this.redirect = redirect;
		this.resourceId = Optional.empty();
	}

	NotificationBarElement(String text, ViewMode viewMode, Class<? extends Component> redirect, String resourceId) {
		this.text = text;
		this.viewMode = viewMode;
		this.redirect = redirect;
		this.resourceId = Optional.of(resourceId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NotificationBarElement that = (NotificationBarElement) o;
		return Objects.equals(text, that.text) &&
			Objects.equals(viewMode, that.viewMode) &&
			Objects.equals(redirect, that.redirect);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text, redirect, viewMode);
	}

	@Override
	public String toString() {
		return "NotificationBarElement{" +
			"text='" + text + '\'' +
			", viewMode=" + viewMode +
			", redirect=" + redirect +
			'}';
	}
}
