/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

class GroupDefinitionRequest {
	
	final String name;
	final String description;

	GroupDefinitionRequest(String name, String description) {
		this.name = name;
		this.description = description;
	}

}
