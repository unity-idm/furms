/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE user_removal (
    id UUID PRIMARY KEY NOT NULL,
    correlation_id UUID,
    site_id UUID NOT NULL,
    project_id UUID NOT NULL,
    user_addition_id UUID NOT NULL,
    user_id VARCHAR(255),
    uid VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (user_addition_id) REFERENCES user_addition(id) ON DELETE CASCADE,
    FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE
);