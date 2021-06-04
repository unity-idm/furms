/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE resource_usage (
    id UUID PRIMARY KEY NOT NULL,
    project_id UUID NOT NULL,
    project_allocation_id UUID NOT NULL,
    cumulative_consumption DECIMAL,
    probed_at TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (project_allocation_id) REFERENCES project_allocation(id) ON DELETE CASCADE
);

CREATE TABLE user_resource_usage (
    id UUID PRIMARY KEY NOT NULL,
    project_id UUID NOT NULL,
    project_allocation_id UUID NOT NULL,
    fenix_user_id VARCHAR(255) NOT NULL,
    cumulative_consumption DECIMAL,
    consumed_until TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (project_allocation_id) REFERENCES project_allocation(id) ON DELETE CASCADE
);