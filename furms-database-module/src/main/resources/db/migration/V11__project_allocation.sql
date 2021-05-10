/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE project_allocation (
    id UUID PRIMARY KEY NOT NULL,
    project_id UUID NOT NULL,
    community_allocation_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    amount DECIMAL NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (community_allocation_id) REFERENCES community_allocation(id) ON DELETE RESTRICT
);