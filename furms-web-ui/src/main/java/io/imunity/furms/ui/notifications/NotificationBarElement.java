/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.notifications;


import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.user_context.ViewMode;

import java.util.Objects;
import java.util.Optional;

public class NotificationBarElement {
	public final String text;
	public final ViewMode viewMode;
	public final Optional<Class<? extends FurmsViewComponent>> redirect;
	public final Optional<String> resourceId;
	public final Optional<String> parameter;

	private NotificationBarElement(String text, ViewMode viewMode, Class<? extends FurmsViewComponent> redirect, String resourceId, String parameter) {
		this.text = text;
		this.viewMode = viewMode;
		this.redirect = Optional.ofNullable(redirect);
		this.resourceId = Optional.ofNullable(resourceId);
		this.parameter = Optional.ofNullable(parameter);
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

	public static NotificationBarElementBuilder builder() {
		return new NotificationBarElementBuilder();
	}

	public static final class NotificationBarElementBuilder {
		private String text;
		private ViewMode viewMode;
		private Class<? extends FurmsViewComponent> redirect;
		private String resourceId;
		private String parameter;

		private NotificationBarElementBuilder() {
		}

		public NotificationBarElementBuilder text(String text) {
			this.text = text;
			return this;
		}

		public NotificationBarElementBuilder viewMode(ViewMode viewMode) {
			this.viewMode = viewMode;
			return this;
		}

		public NotificationBarElementBuilder redirect(Class<? extends FurmsViewComponent> redirect) {
			this.redirect = redirect;
			return this;
		}

		public NotificationBarElementBuilder resourceId(String resourceId) {
			this.resourceId = resourceId;
			return this;
		}

		public void parameter(String parameter) {
			this.parameter = parameter;
		}

		public NotificationBarElement build() {
			return new NotificationBarElement(text, viewMode, redirect, resourceId, parameter);
		}
	}
}
