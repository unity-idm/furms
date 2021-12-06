/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.groups;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.generic_groups.GenericGroupService;
import io.imunity.furms.api.validation.exceptions.UserAlreadyIsInGroupError;
import io.imunity.furms.api.validation.exceptions.UserNotPresentException;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.components.layout.BreadCrumbParameter;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.user_context.UIContext;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

@Route(value = "community/admin/groups/members", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.groups.members.page.title")
public class GroupMembersView extends FurmsViewComponent {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final GenericGroupService genericGroupService;
	private final CommunityService communityService;
	private final String communityId;
	private final ZoneId browserZoneId;

	private BreadCrumbParameter breadCrumbParameter;
	private UsersGridComponent grid;

	protected GroupMembersView(GenericGroupService genericGroupService, CommunityService communityService) {
		this.genericGroupService = genericGroupService;
		this.communityService = communityService;
		this.communityId = getCurrentResourceId();
		this.browserZoneId = UIContext.getCurrent().getZone();
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter){
		GenericGroupId groupId = new GenericGroupId(parameter);
		Optional<GenericGroup> group = genericGroupService.findBy(communityId, groupId);
		if(group.isPresent()) {
			this.breadCrumbParameter = new BreadCrumbParameter(
				group.get().id.id.toString(), group.get().name,
				getTranslation("view.community-admin.groups.members.bread-cramb"));
			fillPage(communityId, groupId, group.get().name);
		}
		else {
			showErrorNotification(getTranslation("view.community-admin.groups.members.wrong.id"));
		}
	}

	private void fillPage(String communityId, GenericGroupId groupId, String groupName) {
		getContent().removeAll();
		ViewHeaderLayout viewHeaderLayout = new ViewHeaderLayout(groupName + " " + getTranslation("view.community-admin.groups.members.half.header"));

		Supplier<List<FURMSUser>> fetchCurrentUsersAction = () -> genericGroupService.findAll(communityId, groupId).stream()
			.map(x -> x.furmsUser)
			.collect(Collectors.toList());
		AddGroupComponent addGroupComponent = new AddGroupComponent(
			() -> communityService.findAllUsers(communityId).stream()
				.filter(user -> user.fenixUserId.isPresent())
				.collect(Collectors.toList()),
			fetchCurrentUsersAction
		);
		addGroupComponent.addAddingAction(event -> doAddAction(addGroupComponent, groupId));

		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
		.addCustomContextMenuItem(
				UserGroupGridItem -> new MenuButton(getTranslation("view.community-admin.groups.members.menu.delete"), TRASH),
				(UserGroupGridItem userGridItem) -> {
					genericGroupService.deleteMembership(communityId, groupId, userGridItem.getFenixUserId().get());
					addGroupComponent.reload();
					grid.reloadGrid();
					showSuccessNotification(getTranslation("group.membership.successful.removed"));
				}
			)
			.build();

		UserGrid.Builder userGrid = UserGrid.builder()
			.withFullNameColumn()
			.withEmailColumn()
			.withCustomColumn((UserGroupGridItem x) -> x.getMemberSince().format(dateTimeFormatter),
				getTranslation("view.community-admin.groups.members.member.since")
			)
			.withContextMenuColumn(userContextMenuFactory);

		grid = UsersGridComponent.init(
			() -> genericGroupService.findAll(communityId, groupId).stream()
				.map(groupAssignmentWithUser ->
					new UserGroupGridItem(
						groupAssignmentWithUser.furmsUser,
						convertToZoneTime(groupAssignmentWithUser.membership.utcMemberSince, browserZoneId)
					)
				).collect(Collectors.toList()),
			userGrid
		);

		getContent().add(viewHeaderLayout, addGroupComponent, grid);
	}

	private void doAddAction(AddGroupComponent inviteUserComponent, GenericGroupId groupId) {
		try {
			inviteUserComponent.getFenixUserId().ifPresent(
				id -> genericGroupService.createMembership(communityId, groupId, id)
			);
			inviteUserComponent.reload();
			showSuccessNotification(getTranslation("group.membership.successful.added"));
			grid.reloadGrid();
		} catch (UserAlreadyIsInGroupError e) {
			showErrorNotification(getTranslation("group.membership.error.user.duplicate"));
		} catch (UserNotPresentException e) {
			showErrorNotification(getTranslation("group.membership.error.user.note.exist"));
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("group.membership.error.unexpected"));
			LOG.error("Could not invite user to group. ", e);
		}
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}

}
