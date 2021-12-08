/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.alarms;

import io.imunity.furms.domain.alarms.AlarmWithUserEmails;

class AlarmFormModelMapper {
	static AlarmWithUserEmails map(String projectId, AlarmFormModel alarmFormModel){
		return AlarmWithUserEmails.builder()
			.id(alarmFormModel.id)
			.projectId(projectId)
			.projectAllocationId(alarmFormModel.allocationId)
			.name(alarmFormModel.name)
			.threshold(alarmFormModel.threshold)
			.allUsers(alarmFormModel.allUsers)
			.alarmUser(alarmFormModel.users)
			.build();
	}

	static AlarmFormModel map(AlarmWithUserEmails alarm){
		return AlarmFormModel.builder()
			.alarmId(alarm.id)
			.allocationId(alarm.projectAllocationId)
			.name(alarm.name)
			.threshold(alarm.threshold)
			.allUsers(alarm.allUsers)
			.users(alarm.alarmUserEmails)
			.build();
	}
}
