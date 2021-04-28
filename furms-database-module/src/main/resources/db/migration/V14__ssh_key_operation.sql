/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE ssh_key_operation_job (
    id UUID PRIMARY KEY NOT NULL,
	correlation_Id UUID NOT NULL,
    site_Id UUID NOT NULL,
    sshkey_Id UUID NOT NULL,
	status VARCHAR(255) NOT NULL,
	operation VARCHAR(255) NOT NULL,
	error VARCHAR(255),
	operation_time TIMESTAMP NOT NULL,
	UNIQUE (sshkey_id, site_id),
	FOREIGN KEY (sshkey_id) REFERENCES sshkey(id) ,
	FOREIGN KEY (site_id) REFERENCES site(id)
);