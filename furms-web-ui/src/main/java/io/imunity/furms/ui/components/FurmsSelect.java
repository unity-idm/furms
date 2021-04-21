/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.shared.Registration;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.communities.CommunityEvent;
import io.imunity.furms.domain.projects.ProjectEvent;
import io.imunity.furms.domain.sites.SiteEvent;
import io.imunity.furms.domain.users.UserEvent;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.VaadinListener;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;

@CssImport("./styles/components/furms-select.css")
public class FurmsSelect extends Select<FurmsSelectText> {
	private final FurmsSelectService furmsSelectService;
	private final VaadinBroadcaster vaadinBroadcaster;
	private Registration broadcasterRegistration;

	public FurmsSelect(RoleTranslator roleTranslator, AuthzService authzService, VaadinBroadcaster vaadinBroadcaster) {
		this.furmsSelectService = new FurmsSelectService(roleTranslator, authzService.getCurrentUserId());
		this.vaadinBroadcaster = vaadinBroadcaster;
		final List<FurmsSelectText> items = furmsSelectService.loadItems();

		addItems(items);

		furmsSelectService.loadSelectedItem()
			.ifPresent(userContext -> setValue(new FurmsSelectText(userContext)));

		addValueChangeListener(event -> furmsSelectService.manageSelectedItemRedirects(event.getValue()));
	}

	void reloadComponent(){
		String currentSelectedContextId = furmsSelectService.loadSelectedItem()
				.orElseThrow(() -> new IllegalStateException("No context found for current user"))
				.id;
		List<FurmsSelectText> items = furmsSelectService.reloadItems();
		addItems(items);
		items.stream()
			.filter(selectText -> selectText.furmsViewUserContext.id.equals(currentSelectedContextId))
			.findFirst()
			.ifPresent(this::setValue);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		UI ui = attachEvent.getUI();
		broadcasterRegistration = vaadinBroadcaster.register(
			VaadinListener.builder()
				.consumer(event -> ui.access(this::reloadComponent))
				.predicate(event -> event instanceof UserEvent)
				.orPredicate(event -> event instanceof SiteEvent)
				.orPredicate(event -> event instanceof CommunityEvent)
				.orPredicate(event -> event instanceof ProjectEvent)
				.build()
		);
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		broadcasterRegistration.remove();
		broadcasterRegistration = null;
	}

	private void addItems(final List<FurmsSelectText> items) {
		setClassName("furms-select");
		setItems(items);
		setTextRenderer(Text::getText);
		addSeparators(items);
	}

	private void addSeparators(List<FurmsSelectText> items) {
		final Map<Integer, List<FurmsSelectText>> itemsGroupedByOrder = items.stream()
				.collect(groupingBy(x -> x.furmsViewUserContext.viewMode.order));
		itemsGroupedByOrder.keySet().stream()
				.sorted()
				.forEach(order -> addSeparator(itemsGroupedByOrder, order));
	}

	private void addSeparator(Map<Integer, List<FurmsSelectText>> items, Integer order) {
		final List<FurmsSelectText> block = items.get(order);
		final ViewMode blockViewMode = getBlockViewMode(block);
		if (shouldHaveSeparator(blockViewMode.order)) {
			final Component separator = blockViewMode.hasHeader()
					? new LabeledHr(getTranslation(format("component.furms.select.role.%s", blockViewMode.name())))
					: new Hr();

			prependComponents(block.get(0), separator);
		}
	}

	private boolean shouldHaveSeparator(int order) {
		return order != 1;
	}

	private ViewMode getBlockViewMode(List<FurmsSelectText> block) {
		return block.stream().findFirst()
					.map(select -> select.furmsViewUserContext.viewMode)
					.orElse(ViewMode.FENIX);

	}
}
