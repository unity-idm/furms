/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

ALTER TABLE project_installation_job ADD COLUMN message VARCHAR(255);
ALTER TABLE project_installation_job ADD COLUMN code VARCHAR(255);
ALTER TABLE project_update_job ADD COLUMN message VARCHAR(255);
ALTER TABLE project_update_job ADD COLUMN code VARCHAR(255);