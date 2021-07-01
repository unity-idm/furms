/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.authz;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.FURMSUserProvider;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserEvent;
import io.imunity.furms.spi.roles.RoleLoader;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.Capability.REST_API_KEY_MANAGEMENT;
import static java.util.Optional.ofNullable;

@Service
public class AuthzServiceImpl implements AuthzService {
	private final RoleLoader roleLoader;

	public AuthzServiceImpl(RoleLoader roleLoader) {
		this.roleLoader = roleLoader;
	}

	@Override
	public FURMSUser getCurrentAuthNUser() {
		return getCurrent();
	}

	@Override
	public Map<ResourceId, Set<Role>> getRoles() {
		return getCurrent().roles;
	}

	@Override
	public boolean isResourceMember(String resourceId, Role role) {
		return getCurrent().roles.entrySet().stream()
			.filter(entry -> resourceId.equals(ofNullable(entry.getKey().id).map(UUID::toString).orElse(null)))
			.anyMatch(entry -> entry.getValue().contains(role));
	}

	@Override
	public boolean hasRESTAPITokensCreationRights() {
		return hasRESTAPITokensCreationRights(getCurrentUserId());
	}

	@Override
	public boolean hasRESTAPITokensCreationRights(PersistentId userId) {
		return roleLoader.loadUserRoles(userId).values().stream()
				.flatMap(Collection::stream)
				.map(role -> role.capabilities)
				.flatMap(Collection::stream)
				.anyMatch(capability -> REST_API_KEY_MANAGEMENT == capability);
	}

	@EventListener
	void onUserEvents(UserEvent event) {
		if(event.getId().equals(getCurrentUserId()))
			reloadRoles();
	}

	@Override
	public void reloadRoles() {
		FURMSUser authentication = getCurrent();
		PersistentId id = authentication.id.orElse(null);
		updateCurrent(new FURMSUser(authentication, roleLoader.loadUserRoles(id)));
	}

	@Override
	public PersistentId getCurrentUserId(){
		return getCurrent().id.orElse(null);
	}

	private static FURMSUser getCurrent() {
		return ((FURMSUserProvider) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getFURMSUser();
	}

	private static void updateCurrent(FURMSUser furmsUser) {
		((FURMSUserProvider) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).updateFURMSUser(furmsUser);
	}
}
