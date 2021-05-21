/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE user_addition_job (
    id UUID PRIMARY KEY NOT NULL,
    correlation_id UUID NOT NULL,
    user_addition_id UUID UNIQUE NOT NULL,
    status INT NOT NULL,
    code VARCHAR(255),
    message VARCHAR(255),
    FOREIGN KEY (user_addition_id) REFERENCES user_addition(id) ON DELETE CASCADE
);