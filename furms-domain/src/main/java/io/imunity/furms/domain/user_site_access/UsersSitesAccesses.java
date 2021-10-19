/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_site_access;

import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class UsersSitesAccesses {
	private final Map<String, List<FURMSUser>> usersGroupedBySiteId;
	private final Map<String, Map<FenixUserId, UserSiteAccessStatusWithMessage>> userInstallations;
	private final Map<String, Map<FenixUserId, AccessStatus>> userResourceAccess;

	public UsersSitesAccesses(Map<String, Set<FenixUserId>> allUserGroupedBySiteId, List<FURMSUser> allUsers, Set<UserAddition> userAdditions, Set<GrantAccess> usersGrants) {
		Map<FenixUserId, FURMSUser> collect = allUsers.stream()
			.collect(toMap(x -> x.fenixUserId.get(), x -> x));

		usersGroupedBySiteId = allUserGroupedBySiteId.entrySet().stream()
			.collect(toMap(Map.Entry::getKey, x -> x.getValue().stream().map(collect::get).collect(toList())));

		this.userInstallations = userAdditions.stream()
			.collect(groupingBy(x -> x.siteId.id, toMap(x -> new FenixUserId(x.userId), x -> new UserSiteAccessStatusWithMessage(x.errorMessage.map(z -> z.message).orElse(null) ,getStatus(x.status)))));
		this.userResourceAccess = usersGrants.stream()
			.collect(groupingBy(x -> x.siteId.id, toMap(x -> x.fenixUserId, x -> x.status)));
	}

	private UserSiteAccessStatus getStatus(UserStatus x) {
		switch (x){
			case ADDING_PENDING:
			case ADDING_ACKNOWLEDGED:
				return UserSiteAccessStatus.APPLYING_PENDING;
			case ADDED:
				return UserSiteAccessStatus.APPLIED;
			case REMOVAL_PENDING:
			case REMOVAL_ACKNOWLEDGED:
				return UserSiteAccessStatus.DISABLING_PENDING;
			case ADDING_FAILED:
				return UserSiteAccessStatus.APPLYING_FAILED;
			case REMOVAL_FAILED:
				return UserSiteAccessStatus.DISABLING_FAILED;
			case REMOVED:
			default:
				return UserSiteAccessStatus.DISABLED;

		}
	}

	public List<FURMSUser> getUsersInstalledOnSite(String siteId){
		return usersGroupedBySiteId.getOrDefault(siteId, List.of());
	}

	public UserSiteAccessStatusWithMessage getStatus(String siteId, FenixUserId userId){
		return Optional.ofNullable(userInstallations.get(siteId))
			.flatMap(map -> Optional.ofNullable(map.get(userId)))
			.orElse(
				Optional.ofNullable(userResourceAccess.get(siteId))
					.map(map -> Optional.ofNullable(map.get(userId)))
					.map(status -> new UserSiteAccessStatusWithMessage(UserSiteAccessStatus.APPLYING_PENDING))
					.orElseGet(() -> new UserSiteAccessStatusWithMessage(UserSiteAccessStatus.DISABLED))
			);
	}
}
