/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE user_allocation (
	id UUID PRIMARY KEY NOT NULL,
	site_Id UUID NOT NULL,
	project_Id UUID NOT NULL,
	project_allocation_id UUID NOT NULL,
	user_id VARCHAR(255) NOT NULL,
	FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE,
	FOREIGN KEY (project_Id) REFERENCES project(id) ON DELETE CASCADE,
	FOREIGN KEY (project_allocation_Id) REFERENCES project_allocation(id) ON DELETE CASCADE
);