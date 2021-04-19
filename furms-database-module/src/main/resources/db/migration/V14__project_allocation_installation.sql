/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE project_allocation_installation (
    id UUID PRIMARY KEY NOT NULL,
    correlation_Id UUID,
    site_Id UUID NOT NULL,
    project_allocation_Id UUID NOT NULL,
    chunk_id VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    FOREIGN KEY (project_allocation_id) REFERENCES project_allocation_job(id),
    FOREIGN KEY (site_id) REFERENCES site(id)
);