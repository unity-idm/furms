/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE project (
    id UUID PRIMARY KEY NOT NULL,
    community_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(510),
    logo_image BYTEA,
    logo_type VARCHAR(255),
    acronym VARCHAR(255) NOT NULL,
    research_field VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    CONSTRAINT project_name_unique UNIQUE (name, community_id),
    FOREIGN KEY (community_id) REFERENCES community(id)
);