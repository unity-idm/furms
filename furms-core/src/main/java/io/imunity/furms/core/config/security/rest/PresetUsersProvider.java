/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.core.config.security.rest;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

class PresetUsersProvider extends InMemoryUserDetailsManager {
	protected final Log logger = LogFactory.getLog(getClass());

	private final Map<String, PresetUser> users;

	public PresetUsersProvider(Collection<PresetUser> users) {
		this.users = users.stream().collect(Collectors.toUnmodifiableMap(user -> user.getUsername(), user -> user));
	}

	@Override
	public void createUser(UserDetails user) {
		throw new IllegalStateException("This is an immutable users store");
	}
	
	@Override
	public void deleteUser(String username) {
		throw new IllegalStateException("This is an immutable users store");
	}
	
	@Override
	public void updateUser(UserDetails user) {
		throw new IllegalStateException("This is an immutable users store");
	}
	
	@Override
	public boolean userExists(String username) {
		return users.containsKey(username.toLowerCase());
	}

	@Override
	public void changePassword(String oldPassword, String newPassword) {
		throw new IllegalStateException("This is an immutable users store");
	}

	@Override
	public UserDetails updatePassword(UserDetails user, String newPassword) {
		throw new IllegalStateException("This is an immutable users store");
	}
	
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		PresetUser user = users.get(username.toLowerCase());

		if (user == null) {
			throw new UsernameNotFoundException(username);
		}

		return new PresetUser(user);
	}
	
	@Override
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
	}
}
