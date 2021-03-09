/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui;

import io.imunity.furms.domain.FurmsEvent;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class VaadinListener {
	private final Consumer<FurmsEvent> consumer;
	private final Predicate<FurmsEvent> predicate;

	public VaadinListener(Consumer<FurmsEvent> consumer, Predicate<FurmsEvent> predicate) {
		this.consumer = consumer;
		this.predicate = predicate;
	}

	public boolean isApplicable(FurmsEvent event){
		return predicate.test(event);
	}

	public void run(FurmsEvent event){
		consumer.accept(event);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VaadinListener that = (VaadinListener) o;
		return Objects.equals(consumer, that.consumer) &&
			Objects.equals(predicate, that.predicate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(consumer, predicate);
	}

	@Override
	public String toString() {
		return "VaadinListener{" +
			"runnable=" + consumer +
			", predicate=" + predicate +
			'}';
	}

	public static VaadinListenerBuilder builder() {
		return new VaadinListenerBuilder();
	}

	public static final class VaadinListenerBuilder {
		private Consumer<FurmsEvent> consumer;
		private Predicate<FurmsEvent> predicate = event -> true;

		private VaadinListenerBuilder() {
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
			return new VaadinListener(consumer, predicate);
		}
	}
}
