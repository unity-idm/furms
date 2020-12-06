/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE site (
    id          BIGSERIAL PRIMARY KEY,
    site_id     VARCHAR(255) UNIQUE NOT NULL,
    name        VARCHAR(255) UNIQUE NOT NULL
);