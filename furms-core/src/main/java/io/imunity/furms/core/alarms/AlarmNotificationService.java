/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.alarms;

import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.notification.UserAlarmListChangedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.notifications.EmailNotificationSender;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.imunity.furms.core.utils.AfterCommitLauncher.runAfterCommit;
import static java.util.stream.Collectors.toList;

@Service
class AlarmNotificationService {

	private final ProjectGroupsDAO projectGroupsDAO;
	private final ProjectRepository projectRepository;
	private final ProjectAllocationRepository projectAllocationRepository;
	private final EmailNotificationSender emailNotificationSender;
	private final UsersDAO usersDAO;
	private final ApplicationEventPublisher publisher;

	AlarmNotificationService(ProjectGroupsDAO projectGroupsDAO,
	                         ProjectRepository projectRepository, ProjectAllocationRepository projectAllocationRepository,
	                         EmailNotificationSender emailNotificationSender,
	                         UsersDAO usersDAO, ApplicationEventPublisher publisher) {
		this.projectGroupsDAO = projectGroupsDAO;
		this.projectRepository = projectRepository;
		this.projectAllocationRepository = projectAllocationRepository;
		this.emailNotificationSender = emailNotificationSender;
		this.usersDAO = usersDAO;
		this.publisher = publisher;
	}


	void sendNotification(AlarmWithUserIds alarm) {
		String projectAllocationName = projectAllocationRepository.findById(alarm.projectAllocationId).get().name;
		String communityId = projectRepository.findById(alarm.projectId).get().getCommunityId();
		getAlarmUserStream(alarm, communityId)
			.distinct()
			.filter(userNotificationWrapper -> userNotificationWrapper.user.id.isPresent())
			.filter(userNotificationWrapper -> userNotificationWrapper.user.fenixUserId.isPresent())
			.forEach(userNotificationWrapper -> {
				if(userNotificationWrapper.isProjectAdmin())
					emailNotificationSender.notifyProjectAdminAboutResourceUsage(
							userNotificationWrapper.user.id.get(), 
							alarm.projectId, 
							alarm.projectAllocationId, 
							projectAllocationName, 
							alarm.name);
				else if(userNotificationWrapper.isProjectUser())
					emailNotificationSender.notifyProjectUserAboutResourceUsage(
							userNotificationWrapper.user.id.get(), 
							alarm.projectId, 
							alarm.projectAllocationId, 
							projectAllocationName, 
							alarm.name);
				else
					emailNotificationSender.notifyUserAboutResourceUsage(
							userNotificationWrapper.user.id.get(), 
							alarm.projectId, 
							alarm.projectAllocationId, 
							projectAllocationName, 
							alarm.name);

				runAfterCommit(() ->
					publisher.publishEvent(new UserAlarmListChangedEvent(
							userNotificationWrapper.user.fenixUserId.get()))
				);
			});
	}

	void cleanNotification(AlarmWithUserIds alarm) {
		String communityId = projectRepository.findById(alarm.projectId).get().getCommunityId();
		getAlarmUserStream(alarm, communityId)
			.distinct()
			.filter(userNotificationWrapper -> userNotificationWrapper.user.fenixUserId.isPresent())
			.forEach(userNotificationWrapper -> runAfterCommit(() ->
				publisher.publishEvent(new UserAlarmListChangedEvent(userNotificationWrapper.user.fenixUserId.get()))
			));
	}

	private Stream<UserNotificationWrapper> getAlarmUserStream(AlarmWithUserIds alarm, String communityId) {
		Map<FenixUserId, FURMSUser> furmsUserMap = usersDAO.getAllUsers().stream()
			.filter(user -> user.fenixUserId.isPresent())
			.collect(Collectors.toMap(user -> user.fenixUserId.get(), Function.identity()));
		List<FURMSUser> furmsUserStream = alarm.alarmUser.stream()
			.map(id -> Optional.ofNullable(furmsUserMap.get(id)))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(toList());

		List<FURMSUser> allAdmins = projectGroupsDAO.getAllAdmins(communityId, alarm.projectId);
		List<FURMSUser> allUsers = projectGroupsDAO.getAllUsers(communityId, alarm.projectId);

		Stream<UserNotificationWrapper> wrapperStream = furmsUserStream.stream()
			.map(user -> getUserNotificationWrapper(allAdmins, allUsers, user));
		if(!alarm.allUsers)
			return wrapperStream;

		allAdmins.removeAll(furmsUserStream);
		return Stream.concat(
			wrapperStream,
			allAdmins.stream().map(x -> new UserNotificationWrapper(x, Role.PROJECT_ADMIN))
		);
	}

	private UserNotificationWrapper getUserNotificationWrapper(List<FURMSUser> allAdmins, List<FURMSUser> allUsers, FURMSUser x) {
		if (allAdmins.contains(x))
			return new UserNotificationWrapper(x, Role.PROJECT_ADMIN);
		else if (allUsers.contains(x))
			return new UserNotificationWrapper(x, Role.PROJECT_USER);
		else
			return new UserNotificationWrapper(x, null);
	}

	static class UserNotificationWrapper {
		public final FURMSUser user;
		public final Role role;

		private UserNotificationWrapper(FURMSUser user, Role role) {
			this.user = user;
			this.role = role;
		}

		boolean isProjectAdmin(){
			return Role.PROJECT_ADMIN.equals(role);
		}

		boolean isProjectUser(){
			return Role.PROJECT_USER.equals(role);
		}
	}
}
