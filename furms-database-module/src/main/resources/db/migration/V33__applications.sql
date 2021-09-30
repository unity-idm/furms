/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE application (
    id UUID PRIMARY KEY NOT NULL,
    project_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    CONSTRAINT user_id_project_id_unique UNIQUE (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);
