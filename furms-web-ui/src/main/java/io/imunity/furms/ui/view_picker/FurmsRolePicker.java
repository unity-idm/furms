/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.view_picker;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.shared.Registration;
import io.imunity.furms.domain.communities.CommunityEvent;
import io.imunity.furms.domain.projects.ProjectEvent;
import io.imunity.furms.domain.sites.SiteEvent;
import io.imunity.furms.domain.users.UserEvent;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.VaadinListener;
import io.imunity.furms.ui.components.LabeledHr;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;

@CssImport("./styles/components/furms-select.css")
public class FurmsRolePicker extends Select<FurmsRolePickerText> {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final FurmsRoleSelectService furmsSelectService;
	private final VaadinBroadcaster vaadinBroadcaster;
	private Registration broadcasterRegistration;

	public FurmsRolePicker(RoleTranslator roleTranslator, VaadinBroadcaster vaadinBroadcaster) {
		this.furmsSelectService = new FurmsRoleSelectService(roleTranslator);
		this.vaadinBroadcaster = vaadinBroadcaster;
		final List<FurmsRolePickerText> items = furmsSelectService.loadItems();

		addItems(items);

		furmsSelectService.loadSelectedItem()
			.ifPresent(userContext -> setValue(new FurmsRolePickerText(userContext)));

		addValueChangeListener(event -> furmsSelectService.manageSelectedItemRedirects(event.getValue()));
	}

	public void reloadComponent(){
		try {
			String currentSelectedContextId = furmsSelectService.loadSelectedItem()
					.orElseThrow(() -> new IllegalStateException("No context found for current user"))
					.id;
			List<FurmsRolePickerText> items = furmsSelectService.loadItems();
			addItems(items);
			items.stream()
				.filter(selectText -> selectText.furmsViewUserContext.id.equals(currentSelectedContextId))
				.findFirst()
				.ifPresent(this::setValue);
		} catch (Exception e) {
			LOG.error("Unable to refresh role-view selector", e);
		}
	}

	public FurmsViewUserContext getCurrent() {
		return furmsSelectService.getCurrent();
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		furmsSelectService.saveOrRestoreUserContext();
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

	private void addItems(final List<FurmsRolePickerText> items) {
		setClassName("furms-select");
		setItems(items);
		setTextRenderer(Text::getText);
		addSeparators(items);
	}

	private void addSeparators(List<FurmsRolePickerText> items) {
		if (items.size() <= 1) {
			return;
		}
		final Map<Integer, List<FurmsRolePickerText>> itemsGroupedByOrder = items.stream()
				.collect(groupingBy(x -> x.furmsViewUserContext.viewMode.order));
		itemsGroupedByOrder.keySet().stream()
				.sorted()
				.forEach(order -> addSeparator(itemsGroupedByOrder, order));
	}

	private void addSeparator(Map<Integer, List<FurmsRolePickerText>> items, Integer order) {
		final List<FurmsRolePickerText> block = items.get(order);
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

	private ViewMode getBlockViewMode(List<FurmsRolePickerText> block) {
		return block.stream().findFirst()
					.map(select -> select.furmsViewUserContext.viewMode)
					.orElse(ViewMode.FENIX);

	}
}
