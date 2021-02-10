/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

class ProjectAllocationId {

	final String projectId;
	final String allocationId;

	ProjectAllocationId(String projectId, String allocationId) {
		this.projectId = projectId;
		this.allocationId = allocationId;
	}

}
