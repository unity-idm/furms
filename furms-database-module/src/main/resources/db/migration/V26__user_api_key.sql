/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE user_api_key (
    id      BIGSERIAL PRIMARY KEY NOT NULL,
    api_key UUID                  NOT NULL,
    user_id VARCHAR(255)          NOT NULL,
    CONSTRAINT user_api_key_id_unique UNIQUE (api_key, user_id)
);