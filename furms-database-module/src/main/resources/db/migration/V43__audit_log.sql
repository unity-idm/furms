/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */


CREATE TABLE audit_log (
    id UUID PRIMARY KEY NOT NULL,
    creation_time TIMESTAMP NOT NULL,
    originator_id VARCHAR(255) NOT NULL,
    operation_category INT NOT NULL,
    operation_action INT NOT NULL,
    operation_subject VARCHAR(255) NOT NULL,
    data_json TEXT
);