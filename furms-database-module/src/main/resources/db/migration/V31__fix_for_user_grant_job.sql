/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

ALTER TABLE user_grant_job
	ADD COLUMN status_int INT;
UPDATE user_grant_job
	SET status_int = CAST(status AS INT);
ALTER TABLE user_grant_job
	DROP COLUMN status;
ALTER TABLE user_grant_job
	RENAME COLUMN status_int TO status;
ALTER TABLE user_grant_job
	ALTER COLUMN status SET NOT NULL;