/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE site (
    id   UUID PRIMARY KEY    NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL
);