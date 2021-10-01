/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.applications;

import io.imunity.furms.domain.users.FenixUserId;

import java.util.Set;

public interface ApplicationRepository {
	Set<FenixUserId> findAllApplyingUsers(String projectId);
	Set<String> findAllAppliedProjectsIds(FenixUserId userId);
	void create(String projectId, FenixUserId userId);
	void remove(String projectId, FenixUserId id);
	boolean existsBy(String projectId, FenixUserId id);
}
