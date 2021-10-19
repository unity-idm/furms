/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

ALTER TABLE project_installation_job
ADD CONSTRAINT project_installation_job_site_id_project_id_unique UNIQUE (site_Id, project_id);
