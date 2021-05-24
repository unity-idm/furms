/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui;

import io.imunity.furms.domain.FurmsEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class VaadinListener {
	private final SecurityContext securityContext;
	private final Consumer<FurmsEvent> consumer;
	private final Predicate<FurmsEvent> predicate;

	public VaadinListener(SecurityContext securityContext,
	                      Consumer<FurmsEvent> consumer,
	                      Predicate<FurmsEvent> predicate) {
		this.securityContext = securityContext;
		this.consumer = consumer;
		this.predicate = predicate;
	}

	public boolean isApplicable(FurmsEvent event){
		return predicate.test(event);
	}

	public void run(FurmsEvent event){
		try {
			SecurityContextHolder.setContext(securityContext);
			consumer.accept(event);
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VaadinListener that = (VaadinListener) o;
		return Objects.equals(securityContext, that.securityContext) &&
				Objects.equals(consumer, that.consumer) &&
				Objects.equals(predicate, that.predicate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(securityContext, consumer, predicate);
	}

	@Override
	public String toString() {
		return "VaadinListener{" +
				"securityContext=" + securityContext +
				", consumer=" + consumer +
				", predicate=" + predicate +
				'}';
	}

	public static VaadinListenerBuilder builder() {
		return new VaadinListenerBuilder();
	}

	public static final class VaadinListenerBuilder {
		private SecurityContext securityContext;
		private Consumer<FurmsEvent> consumer;
		private Predicate<FurmsEvent> predicate = event -> true;

		private VaadinListenerBuilder() {
		}

		public VaadinListenerBuilder securityContext(SecurityContext securityContext) {
			this.securityContext = securityContext;
			return this;
		}

		public VaadinListenerBuilder consumer(Consumer<FurmsEvent> runnable) {
			this.consumer = runnable;
			return this;
		}

		public VaadinListenerBuilder predicate(Predicate<FurmsEvent> predicate) {
			this.predicate = predicate;
			return this;
		}

		public VaadinListenerBuilder andPredicate(Predicate<FurmsEvent> predicate) {
			this.predicate = this.predicate.and(predicate);
			return this;
		}

		public VaadinListenerBuilder orPredicate(Predicate<FurmsEvent> predicate) {
			this.predicate = this.predicate.or(predicate);
			return this;
		}

		public VaadinListener build() {
			if (securityContext == null) {
				throw new IllegalArgumentException("Security Context has to be specific in Vaadin Listener scope.");
			}
			return new VaadinListener(securityContext, consumer, predicate);
		}
	}
}
