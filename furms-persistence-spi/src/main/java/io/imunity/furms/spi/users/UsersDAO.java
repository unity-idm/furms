/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.users;

import java.util.List;
import java.util.Optional;

import io.imunity.furms.domain.users.User;

public interface UsersDAO {
	List<User> getAdminUsers();
	List<User> getProjectUsers(String communityId, String projectId);
	List<User> getAllUsers();
	Optional<User> findByEmail(String email);
	boolean isProjectMember(String communityId, String projectId, String userId);
	void addProjectMemberRole(String communityId, String projectId, String userId);
	void addFenixAdminRole(String userId);
	void addProjectAdminRole(String communityId, String projectId, String userId);
	void removeFenixAdminRole(String userId);
	void removeProjectMemberRole(String communityId, String projectId, String userId);
	Optional<User> findById(String userId);
}
