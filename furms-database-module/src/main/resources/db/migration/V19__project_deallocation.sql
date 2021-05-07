/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE project_deallocation (
    id UUID PRIMARY KEY NOT NULL,
    correlation_Id UUID,
    site_Id UUID,
    project_allocation_Id UUID NOT NULL,
    status INT NOT NULL,
    FOREIGN KEY (site_id) REFERENCES site(id)
);