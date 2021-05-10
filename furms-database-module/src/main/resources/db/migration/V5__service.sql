/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE service (
    id UUID PRIMARY KEY NOT NULL,
    site_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(510),
    CONSTRAINT service_name_unique UNIQUE (name, site_id), 
    FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE
);