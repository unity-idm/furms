/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import io.imunity.furms.api.users.UserAllocationsService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.generic_groups.GroupAccess;
import io.imunity.furms.domain.sites.SiteUser;
import io.imunity.furms.domain.users.AllUsersAndFenixAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.SiteAgentSetUserAccountStatusRequest;
import io.imunity.furms.domain.users.UnknownUserException;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.site.api.status_updater.UserAccountStatusUpdater;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.generic_groups.GenericGroupRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.imunity.furms.domain.authz.roles.Capability.READ_ALL_USERS;
import static io.imunity.furms.domain.authz.roles.Capability.USERS_MAINTENANCE;
import static io.imunity.furms.domain.users.UserAccountStatusUpdateReason.SECURITY_INCIDENT;

@Service
class UserServiceImpl implements UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

	private final UsersDAO usersDAO;
	private final UserAllocationsService userAllocationsService;
	private final GenericGroupRepository genericGroupRepository;
	private final UserAccountStatusUpdater userAccountStatusUpdater;

	public UserServiceImpl(UsersDAO usersDAO,
	                       UserAllocationsService userAllocationsService,
	                       GenericGroupRepository genericGroupRepository,
	                       UserAccountStatusUpdater userAccountStatusUpdater) {
		this.usersDAO = usersDAO;
		this.userAllocationsService = userAllocationsService;
		this.genericGroupRepository = genericGroupRepository;
		this.userAccountStatusUpdater = userAccountStatusUpdater;
	}

	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS)
	public List<FURMSUser> getAllUsers(){
		return usersDAO.getAllUsers();
	}

	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS)
	public AllUsersAndFenixAdmins getAllUsersAndFenixAdmins() {
		return usersDAO.getAllUsersAndFenixAdmins();
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE)
	public void setUserStatus(FenixUserId fenixUserId, UserStatus status) {
		checkNotNull(status);
		checkNotNull(fenixUserId);
		LOG.info("Setting {} status to {}", fenixUserId, status);
		try {
			usersDAO.setUserStatus(fenixUserId, status);
			userAllocationsService.findUserAdditionsByFenixUserId(fenixUserId)
					.forEach(userAddition -> userAccountStatusUpdater.setStatus(
							new SiteAgentSetUserAccountStatusRequest(userAddition, status, SECURITY_INCIDENT)));
		} catch (UnityFailureException e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE)
	public UserStatus getUserStatus(FenixUserId fenixUserId) {
		checkNotNull(fenixUserId);
		try {
			return usersDAO.getUserStatus(fenixUserId);
		} catch (UnityFailureException e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}
	
	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS)
	public Optional<FURMSUser> findById(PersistentId userId) {
		checkNotNull(userId);
		checkNotNull(userId.id);
		return usersDAO.findById(userId);
	}

	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS)
	public Optional<FURMSUser> findByFenixUserId(FenixUserId fenixUserId) {
		checkNotNull(fenixUserId);
		checkNotNull(fenixUserId.id);
		return usersDAO.findById(fenixUserId);
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE)
	public UserRecord getUserRecord(FenixUserId fenixUserId) {
		try {
			FURMSUser user = findByFenixUserId(fenixUserId)
				.orElseThrow(() -> new UnknownUserException(fenixUserId));

			PersistentId userId = usersDAO.getPersistentId(fenixUserId);
			Set<SiteUser> siteUsers = userAllocationsService.findUserSitesInstallations(userId);
			Set<GroupAccess> userGroupsAccesses = genericGroupRepository.findAllBy(fenixUserId);

			return new UserRecord(user, siteUsers, userGroupsAccesses);
		} catch (Exception e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}
}





