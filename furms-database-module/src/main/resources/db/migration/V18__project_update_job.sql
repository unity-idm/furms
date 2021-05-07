/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE project_update_job (
    id UUID PRIMARY KEY NOT NULL,
    correlation_Id UUID NOT NULL,
    site_Id UUID NOT NULL,
    project_Id UUID NOT NULL,
    status INT NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (site_id) REFERENCES site(id)
);