/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE project (
    id UUID PRIMARY KEY NOT NULL,
    community_id UUID NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(510),
    logo_image BYTEA,
    logo_type VARCHAR(255),
    acronym VARCHAR(255),
    research_field VARCHAR(255),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    FOREIGN KEY (community_id) REFERENCES community(id)
);