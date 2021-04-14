/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.AttachEvent;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;

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
		String currentSelectedContextId = furmsSelectService.loadSelectedItem().get().id;
		List<FurmsSelectText> items = furmsSelectService.reloadItems();
		addItems(items);
		items.stream()
			.filter(selectText -> selectText.furmsViewUserContext.id.equals(currentSelectedContextId))
			.findFirst()
			.ifPresent(this::setValue);
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
		final Optional<Integer> maxOrder = itemsGroupedByOrder.keySet().stream()
				.max(Comparator.comparingInt(key -> key));
		itemsGroupedByOrder.keySet().stream()
				.sorted()
				.filter(order -> !maxOrder.get().equals(order))
				.forEach(order -> {
					final List<FurmsSelectText> block = itemsGroupedByOrder.get(order);
					addComponents(block.get(block.size()-1), new Hr());
				});
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
}
