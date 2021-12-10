/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.sites;

import com.vaadin.componentfactory.Tooltip;
import com.vaadin.componentfactory.TooltipAlignment;
import com.vaadin.componentfactory.TooltipPosition;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.users.UserAllocationsService;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.domain.sites.UserProjectsInstallationInfoData;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.user_settings.UserSettingsMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.INFO_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.WARNING;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Route(value = "users/settings/sites", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.sites.page.title")
@CssImport("./styles/views/user/sites/user-sites.css")
public class SitesView extends FurmsViewComponent implements AfterNavigationObserver {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final static int DOUBLECLICK = 2;

	private final UserAllocationsService userAllocationsService;

	private Div connectionInfo;
	private H4 connectionInfoLabel;

	private final DenseGrid<UserSitesGridModel> grid;

	SitesView(UserAllocationsService userAllocationsService) {
		this.userAllocationsService = userAllocationsService;
		this.grid = new DenseGrid<>(UserSitesGridModel.class);

		addTitle();
		addGrid();
		addConnectionInfoPanel();
	}

	private void addTitle() {
		final ViewHeaderLayout layout = new ViewHeaderLayout(getTranslation("view.user-settings.sites.page.title"));
		getContent().add(layout);
	}

	private void addGrid() {
		grid.addColumn(UserSitesGridModel::getSiteName)
				.setHeader(getTranslation("view.user-settings.sites.grid.title.siteName"))
				.setTextAlign(ColumnTextAlign.START)
				.setComparator(comparing(UserSitesGridModel::getSiteName))
				.setSortable(true)
				.setWidth("20%");
		grid.addColumn(UserSitesGridModel::getProjectName)
				.setHeader(getTranslation("view.user-settings.sites.grid.title.projectName"))
				.setTextAlign(ColumnTextAlign.START)
				.setComparator(comparing(UserSitesGridModel::getProjectName))
				.setSortable(true)
				.setWidth("20%");
		grid.addColumn(UserSitesGridModel::getRemoteAccountName)
				.setHeader(getTranslation("view.user-settings.sites.grid.title.siteAccountName"))
				.setComparator(comparing(UserSitesGridModel::getRemoteAccountName))
				.setSortable(true)
				.setWidth("30%");
		grid.addComponentColumn(this::showStatus)
				.setHeader(getTranslation("view.user-settings.sites.grid.title.status"))
				.setComparator(comparing(item -> item.getStatus().name()))
				.setSortable(true)
				.setWidth("15%");
		grid.addComponentColumn(this::showConnectionInfoButton)
				.setHeader(getTranslation("view.user-settings.sites.grid.title.connectionInfo"))
				.setTextAlign(ColumnTextAlign.END)
				.setWidth("15%");
		grid.addItemClickListener(item -> showConnectionInfo(item.getItem()));
		getContent().add(grid);
	}

	private void addConnectionInfoPanel() {
		connectionInfo = new Div();
		connectionInfo.setVisible(false);
		connectionInfo.addClassName("user-sites-connection-info-content");

		connectionInfoLabel = new H4("");
		connectionInfoLabel.setVisible(false);

		final FurmsFormLayout formLayout = new FurmsFormLayout();
		formLayout.addClassName("user-sites-connection-info");
		formLayout.addFormItem(connectionInfo, connectionInfoLabel);

		connectionInfo.addClickListener(event -> {
			if (event.getClickCount() == DOUBLECLICK) {
				connectionInfoLabel.setText("");
				connectionInfo.setText("");
				connectionInfo.setVisible(false);
				connectionInfoLabel.setVisible(false);
			}
		});

		getContent().add(formLayout);
	}

	private Button showConnectionInfoButton(UserSitesGridModel item) {
		final Button button = new Button(INFO_CIRCLE.create());
		button.addThemeVariants(LUMO_TERTIARY);
		button.addClickListener(event -> showConnectionInfo(item));
		return button;
	}
	
	private void showConnectionInfo(UserSitesGridModel item) {
		connectionInfoLabel.setText(getTranslation("view.user-settings.sites.connectionInfo.label", item.getSiteName()));
		connectionInfo.setText(item.getConnectionInfo());
		connectionInfoLabel.setVisible(true);
		connectionInfo.setVisible(true);
	}

	private Div showStatus(UserSitesGridModel item) {
		final Label label = new Label(getTranslation(format("view.user-settings.sites.grid.status.%s", item.getStatus().name())));
		if (item.getStatus().isErrorStatus()) {
			final Button button = new Button(WARNING.create());
			button.addThemeVariants(LUMO_TERTIARY);
			final Tooltip tooltip = new Tooltip(button, TooltipPosition.BOTTOM, TooltipAlignment.RIGHT);
			tooltip.add(item.getErrorMessage());
			tooltip.setThemeName("Light");
			getContent().add(tooltip);

			return new Div(label, button);
		}
		return new Div(label);
	}
	
	private void loadGridContent() {
		grid.setItems(loadItems());
	}

	private List<UserSitesGridModel> loadItems() {
		return userAllocationsService.findCurrentUserSitesInstallations().stream()
				.map(info -> info.getProjects().stream()
						.map(project -> UserSitesGridModel.builder()
								.siteName(info.getSiteName())
								.connectionInfo(info.getConnectionInfo())
								.projectName(project.getName())
								.remoteAccountName(project.getRemoteAccountName())
								.status(project.getStatus())
								.errorMessage(buildErrorMessage(project))
								.build())
						.collect(toList()))
				.flatMap(Collection::parallelStream)
				.collect(toList());
	}

	private String buildErrorMessage(UserProjectsInstallationInfoData project) {
		if (!project.getStatus().isErrorStatus()) {
			return "";
		}
		final String errorMessage = ofNullable(project.getErrorMessage())
				.map(error -> error.message)
				.orElse(getTranslation("view.user-settings.sites.grid.status.error-message.content.unavailable"));

		return getTranslation("view.user-settings.sites.grid.status.error-message.content", errorMessage);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		
		try {
			loadGridContent();
		} catch (UserWithoutFenixIdValidationError e) {
			LOG.debug(e.getMessage(), e);
			showErrorNotification(getTranslation("user.without.fenixid.error.message"));
			setVisible(false);
		}
	}

}
