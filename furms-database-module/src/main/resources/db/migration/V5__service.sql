/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE service (
    id UUID PRIMARY KEY NOT NULL,
    site_id UUID NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(510),
    FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE
);