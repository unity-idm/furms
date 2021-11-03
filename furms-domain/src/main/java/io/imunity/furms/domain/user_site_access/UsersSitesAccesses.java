/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_site_access;

import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public class UsersSitesAccesses {
	private final List<FURMSUser> users;
	private final Map<String, Set<FenixUserId>> userSiteAccesses;
	private final Map<String, Map<FenixUserId, UserSiteAccessStatusWithMessage>> userInstallationBySite;

	public UsersSitesAccesses(List<FURMSUser> allUsers, Map<String, Set<FenixUserId>> allUserGroupedBySiteId, Set<UserAddition> userAdditions) {
		this.users = allUsers.stream()
			.filter(usr -> usr.fenixUserId.isPresent())
			.collect(Collectors.toList());
		this.userSiteAccesses = Map.copyOf(allUserGroupedBySiteId);

		this.userInstallationBySite = userAdditions.stream()
			.collect(groupingBy(x -> x.siteId.id,
				toMap(x -> new FenixUserId(x.userId), x -> new UserSiteAccessStatusWithMessage(x.errorMessage.map(z -> z.message).orElse(null), getStatus(x.status))))
			);
	}

	private UserSiteAccessStatus getStatus(UserStatus x) {
		switch (x){
			case ADDING_PENDING:
			case ADDING_ACKNOWLEDGED:
				return UserSiteAccessStatus.ENABLING_PENDING;
			case ADDED:
				return UserSiteAccessStatus.ENABLED;
			case REMOVAL_PENDING:
			case REMOVAL_ACKNOWLEDGED:
				return UserSiteAccessStatus.DISABLING_PENDING;
			case ADDING_FAILED:
				return UserSiteAccessStatus.ENABLING_FAILED;
			case REMOVAL_FAILED:
				return UserSiteAccessStatus.DISABLING_FAILED;
			case REMOVED:
			default:
				return UserSiteAccessStatus.DISABLED;

		}
	}

	public List<FURMSUser> getUsersInstalledOnSite(){
		return users;
	}

	public UserSiteAccessStatusWithMessage getStatus(String siteId, FenixUserId userId){
		return Optional.ofNullable(userInstallationBySite.get(siteId))
			.flatMap(map -> Optional.ofNullable(map.get(userId)))
			.orElse(
				userSiteAccesses.getOrDefault(siteId, Set.of()).contains(userId) ?
					new UserSiteAccessStatusWithMessage(UserSiteAccessStatus.ENABLING_PENDING) :
					new UserSiteAccessStatusWithMessage(UserSiteAccessStatus.DISABLED)
			);
	}
}
