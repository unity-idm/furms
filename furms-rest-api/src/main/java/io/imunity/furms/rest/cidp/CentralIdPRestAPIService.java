/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.cidp;

import static java.util.stream.Collectors.toSet;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.UnknownUserException;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.rest.error.exceptions.RestNotFoundException;

@Service
class CentralIdPRestAPIService {

	private final UserService userService;

	CentralIdPRestAPIService(UserService userService) {
		this.userService = userService;
	}

	UserRecordJson findUserRecordByFenixId(String fenixUserId) {
		return new UserRecordJson(userService.getUserRecord(new FenixUserId(fenixUserId)));
	}

	UserRecordJson findUserRecordByFenixIdAndSiteId(String fenixUserId, String oauthClientId) {
		if (StringUtils.isEmpty(fenixUserId) || StringUtils.isEmpty(oauthClientId)) {
			throw new RestNotFoundException("Incorrect userId or siteId format.");
		}
		final UserRecord userRecord = userService.getUserRecord(new FenixUserId(fenixUserId));
		return new UserRecordJson(new UserRecord(userRecord.userStatus,
				userRecord.attributes,
				userRecord.resourceAttributes,
				userRecord.siteInstallations.stream()
					.filter(siteInstallation -> siteInstallation.siteOauthClientId != null)
					.filter(siteInstallation -> siteInstallation.siteOauthClientId.equals(oauthClientId))
					.collect(toSet())));
	}
	
	void setUserStatus(String fenixUserId, UserStatusHolder userStatus) {
		userService.setUserStatus(new FenixUserId(fenixUserId), userStatus.status);
	}

	UserStatusHolder findUserStatusByFenixId(String fenixUserId) {
		try {
			return new UserStatusHolder(userService.getUserStatus(new FenixUserId(fenixUserId)));
		} catch (UnknownUserException e) {
			throw new ResponseStatusException(
				           HttpStatus.NOT_FOUND, "User " + fenixUserId + " not found", e);
		}
	}
}
