/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.notifications;


import com.vaadin.flow.component.Component;

import java.util.Objects;

public class NotificationBarElement {
	public final String text;
	public final Class<? extends Component> redirect;

	NotificationBarElement(String text, Class<? extends Component> redirect) {
		this.text = text;
		this.redirect = redirect;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NotificationBarElement that = (NotificationBarElement) o;
		return Objects.equals(text, that.text) &&
			Objects.equals(redirect, that.redirect);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text, redirect);
	}

	@Override
	public String toString() {
		return "NotificationBarElement{" +
			"text='" + text + '\'' +
			", redirect=" + redirect +
			'}';
	}
}
