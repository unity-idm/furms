/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.List;

class ProjectWithUsers {
	
	final Project project;
	final List<String> userFenixUserIds;

	ProjectWithUsers(Project project, List<String> userFenixUserIds) {
		this.project = project;
		this.userFenixUserIds = userFenixUserIds;
	}
}
