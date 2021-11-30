/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.alarms;

import io.imunity.furms.api.alarms.ActiveAlarmsService;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.alarms.ActiveAlarm;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.spi.alarms.AlarmRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;

@Service
class ActiveAlarmsServiceImpl implements ActiveAlarmsService {
	private final AlarmRepository alarmRepository;
	private final AuthzService authzService;

	ActiveAlarmsServiceImpl(AlarmRepository alarmRepository, AuthzService authzService) {
		this.alarmRepository = alarmRepository;
		this.authzService = authzService;
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public Set<ActiveAlarm> findAllActiveAlarmsAssignToCurrentUser() {
		FURMSUser currentUser = authzService.getCurrentAuthNUser();
		if(currentUser.fenixUserId.isEmpty())
			return Set.of();
		return alarmRepository.findAll(currentUser.fenixUserId.get());
	}
}
