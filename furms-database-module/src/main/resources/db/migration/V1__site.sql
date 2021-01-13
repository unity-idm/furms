/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE site (
    id   UUID PRIMARY KEY    NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE community (
    id   UUID PRIMARY KEY    NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(510),
    logo_image BYTEA,
    logo_type VARCHAR(255)
);