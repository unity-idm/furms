/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.alarms;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.validation.exceptions.AlarmAlreadyExceedThresholdException;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.EmailNotPresentException;
import io.imunity.furms.api.validation.exceptions.FiredAlarmThresholdReduceException;
import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.layout.BreadCrumbParameter;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

@Route(value = "project/admin/alarms/form", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.alarms.page.title")
public class AlarmFormView extends FurmsViewComponent {
	private final AlarmService alarmService;
	private final ProjectAllocationService allocationService;

	private final EmailValidator emailValidator = new EmailValidator("Not valid email");
	private final Binder<AlarmFormModel> binder = new BeanValidationBinder<>(AlarmFormModel.class);
	private final Div buttonLayout = new Div();
	private final ProjectId projectId;
	private final ComboBox<ProjectAllocationId> allocationComboBox =  new ComboBox<>();

	private BreadCrumbParameter breadCrumbParameter;

	AlarmFormView(AlarmService alarmService, ProjectAllocationService allocationService, ProjectService projectService) {
		this.alarmService = alarmService;
		this.allocationService = allocationService;
		this.projectId = new ProjectId(getCurrentResourceId());
		FormLayout formLayout = new FurmsFormLayout();

		TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(25);
		formLayout.addFormItem(nameField, getTranslation("view.project-admin.alarms.form.layout.name"));

		Set<ProjectAllocationId> occupiedAllocationIds = alarmService.findAll(projectId)
			.stream()
			.map(alarm -> alarm.projectAllocationId)
			.collect(Collectors.toSet());
		Map<ProjectAllocationId, String> allocationIdToName = allocationService.findAllWithRelatedObjects(projectId).stream()
			.filter(allocation -> !occupiedAllocationIds.contains(allocation.id))
			.collect(Collectors.toMap(allocation -> allocation.id, allocation -> allocation.name));
		allocationComboBox.setItems(allocationIdToName.keySet());
		allocationComboBox.setItemLabelGenerator(key -> allocationIdToName.getOrDefault(key, ""));
		formLayout.addFormItem(allocationComboBox, getTranslation("view.project-admin.alarms.form.layout.allocation.combo-box"));

		IntegerField thresholdField = new IntegerField();
		thresholdField.setValue(1);
		thresholdField.setHasControls(true);
		thresholdField.setMin(1);
		thresholdField.setMax(100);
		thresholdField.setSuffixComponent(new Label("%"));
		thresholdField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);

		formLayout.addFormItem(thresholdField, getTranslation("view.project-admin.alarms.form.layout.threshold"));

		Checkbox checkbox = new Checkbox();
		checkbox.setLabel(getTranslation("view.project-admin.alarms.form.layout.notification.all"));
		formLayout.addFormItem(checkbox, "");

		MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();
		multiselectComboBox.setAllowCustomValues(true);
		multiselectComboBox.setItems(projectService.findAllUsers(projectId).stream().map(user -> user.email));
		multiselectComboBox.addCustomValuesSetListener(x -> {
			HashSet<String> values = new HashSet<>(multiselectComboBox.getValue());
			values.add(x.getDetail());
			multiselectComboBox.setValue(values);
		});
		formLayout.addFormItem(multiselectComboBox, getTranslation("view.project-admin.alarms.form.layout.notification.users"));

		prepareValidator(nameField, allocationComboBox, thresholdField, checkbox, multiselectComboBox);

		getContent().add(formLayout, buttonLayout);
	}

	private void addCreateButtons() {
		FormButtons buttons = new FormButtons(
			createCloseButton(),
			createSaveButton(getTranslation("view.project-admin.alarms.form.button.save"))
		);
		buttonLayout.removeAll();
		buttonLayout.add(buttons);
	}

	private void prepareValidator(TextField nameField,
	                              ComboBox<ProjectAllocationId> allocationComboBox,
	                              IntegerField thresholdField,
	                              Checkbox checkbox, MultiselectComboBox<String> multiselectComboBox) {
		binder.forField(nameField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.project-admin.alarms.form.error.name")
			)
			.bind(model -> model.name, (model, name) -> model.name = name);
		binder.forField(allocationComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.project-admin.alarms.form.error.allocation")
			)
			.bind(model -> model.allocationId, (model, id) -> model.allocationId = id);
		binder.forField(thresholdField)
			.withValidator(
				threshold -> threshold >= 1.0 &&  threshold <= 100.0,
				getTranslation("view.project-admin.alarms.form.error.threshold")
			)
			.bind(model -> model.threshold, (model, threshold) -> model.threshold = threshold);
		binder.forField(checkbox)
			.bind(model -> model.allUsers, (model, value) -> model.allUsers = value);
		binder.forField(multiselectComboBox)
			.withValidator(
				emails -> emails.stream().noneMatch(email -> emailValidator.apply(email, new ValueContext()).isError()),
				getTranslation("view.project-admin.alarms.form.error.emails")
			)
			.bind(model -> model.users, (model, policyFile) -> model.users = multiselectComboBox.getSelectedItems());
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.project-admin.alarms.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(AlarmsView.class));
		return closeButton;
	}

	private Button createSaveButton(String text) {
		Button saveButton = new Button(text);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid()) {
				saveAlarm();
			}
		});
		return saveButton;
	}

	private void saveAlarm() {
		AlarmFormModel alarmFormModel = binder.getBean();
		AlarmWithUserEmails alarm = AlarmFormModelMapper.map(projectId, alarmFormModel);
		try {
			if (alarmFormModel.id == null)
				alarmService.create(alarm);
			else
				alarmService.update(alarm);
			UI.getCurrent().navigate(AlarmsView.class);
		} catch (DuplicatedNameValidationError e) {
			showErrorNotification(getTranslation("name.duplicated.error.message"));
		} catch (EmailNotPresentException e) {
			showErrorNotification(getTranslation("alarm.wrong.user", e.email));
		} catch (AlarmAlreadyExceedThresholdException e) {
			showErrorNotification(getTranslation("alarm.wrong.threshold"));
		} catch (FiredAlarmThresholdReduceException e) {
			showErrorNotification(getTranslation("fired.alarm.wrong.threshold"));
		} catch (Exception e) {
			showErrorNotification(getTranslation("base.error.message"));
			throw e;
		}
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		AlarmFormModel alarm = ofNullable(parameter)
			.map(UUID::fromString)
			.flatMap(id -> handleExceptions(() -> alarmService.find(projectId, new AlarmId(id))))
			.flatMap(Function.identity())
			.map(AlarmFormModelMapper::map)
			.orElseGet(AlarmFormModel::new);

		String trans = parameter == null
			? "view.project-admin.alarms.form.parameter.new"
			: "view.project-admin.alarms.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		binder.setBean(alarm);
		addCreateButtons();
		if(alarm.id != null) {
			setComboBoxInReadOnlyMode(alarm);
		}

	}

	private void setComboBoxInReadOnlyMode(AlarmFormModel alarm) {
		allocationComboBox.setReadOnly(true);
		ProjectAllocation projectAllocation = allocationService.findByProjectIdAndId(projectId, alarm.allocationId).get();
		allocationComboBox.setItems(projectAllocation.id);
		allocationComboBox.setItemLabelGenerator(item -> projectAllocation.name);
		allocationComboBox.setValue(projectAllocation.id);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}

}
