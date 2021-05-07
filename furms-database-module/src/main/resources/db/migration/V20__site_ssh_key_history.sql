/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

ALTER TABLE site ADD COLUMN SSH_KEY_HISTORY_LENGTH INT;

CREATE TABLE ssh_key_history (
	id UUID PRIMARY KEY NOT NULL,
	sshkey_owner_id VARCHAR(255) NOT NULL,
	site_Id UUID NOT NULL,
	sshkey_fingerprint VARCHAR(255) NOT NULL,
	origination_time TIMESTAMP NOT NULL,
	FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE
);