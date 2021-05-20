/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE user_addition (
    id UUID PRIMARY KEY NOT NULL,
    site_id UUID NOT NULL,
    project_id UUID NOT NULL,
    user_id VARCHAR(255),
    uid VARCHAR(255),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE
);