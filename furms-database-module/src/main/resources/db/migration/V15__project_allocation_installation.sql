/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE project_allocation_installation (
    id UUID PRIMARY KEY NOT NULL,
    correlation_Id UUID,
    site_Id UUID,
    project_allocation_Id UUID NOT NULL,
    chunk_id VARCHAR(255),
    valid_from TIMESTAMP,
    valid_to TIMESTAMP,
    received_time TIMESTAMP,
    amount DECIMAL,
    status VARCHAR(255) NOT NULL,
    FOREIGN KEY (project_allocation_id) REFERENCES project_allocation(id) ON DELETE CASCADE,
    FOREIGN KEY (site_id) REFERENCES site(id)
);