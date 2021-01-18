/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.vaadin.flow.component.notification.Notification.Position.TOP_END;

public abstract class FurmsViewComponent extends Composite<Div> implements HasUrlParameter<String>, HasDynamicTitle {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public FurmsViewComponent() {
		getContent().setClassName("furms-view");
	}

	public Optional<BreadCrumbParameter> getParameter(){
		return Optional.empty();
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {}

	@Override
	public String getPageTitle() {
		return getTranslation(getClass().getAnnotation(PageTitle.class).key());
	}

	protected void showErrorNotification(String message) {
		Notification error = new Notification(message, 5000, TOP_END);
		error.setThemeName("error");
		error.setOpened(true);
	}

	protected SerializablePredicate<? super String> getNotEmptyStringValidator() {
		return value -> value != null && !value.isBlank();
	}

	protected <T> void doAction(Consumer<T> consumer, T value){
		doAction(t -> {
				consumer.accept(t);
				return null;
			}, value
		);
	}

	protected <T> Optional<T> doAction(Supplier<T> supplier){
		return doAction((Function<?, T>) t -> supplier.get(), null);
	}

	protected <T,R> Optional<R> doAction(Function<T,R> function, T t){
		try {
			return Optional.of(function.apply(t));
		}catch (Exception e){
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("base.error.message"));
			return Optional.empty();
		}
	}
}
