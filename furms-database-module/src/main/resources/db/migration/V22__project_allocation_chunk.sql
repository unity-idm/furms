/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE project_allocation_chunk (
    id UUID PRIMARY KEY NOT NULL,
    project_allocation_id UUID NOT NULL,
    chunk_id VARCHAR(255),
    valid_from TIMESTAMP,
    valid_to TIMESTAMP,
    received_time TIMESTAMP,
    amount DECIMAL,
    FOREIGN KEY (project_allocation_id) REFERENCES project_allocation(id) ON DELETE CASCADE
);