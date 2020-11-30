/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.util.List;

class ProjectWithMembers {
	
	final Project project;
	final List<String> memberFenixUserIds;

	ProjectWithMembers(Project project, List<String> memberFenixUserIds) {
		this.project = project;
		this.memberFenixUserIds = memberFenixUserIds;
	}
}
