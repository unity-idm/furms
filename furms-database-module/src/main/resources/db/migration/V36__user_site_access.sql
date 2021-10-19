/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE user_site_access (
    id UUID PRIMARY KEY NOT NULL,
    site_id UUID NOT NULL,
    project_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    CONSTRAINT user_id_site_id_project_id_unique UNIQUE (site_id, project_id, user_id),
    FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);
