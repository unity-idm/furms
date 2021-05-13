/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE user_allocation_job (
	id UUID PRIMARY KEY NOT NULL,
	correlation_id UUID NOT NULL,
	user_allocation_id UUID UNIQUE NOT NULL,
	status VARCHAR(255) NOT NULL,
	message VARCHAR(255),
	FOREIGN KEY (user_allocation_id) REFERENCES user_allocation(id) ON DELETE CASCADE
);