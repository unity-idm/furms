/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE resource_credit (
    id UUID PRIMARY KEY NOT NULL,
    site_id UUID NOT NULL,
    resource_type_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    split BOOLEAN NOT NULL,
    amount DECIMAL NOT NULL,
    create_time TIMESTAMP NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    CONSTRAINT resource_credit_name_unique UNIQUE (name, site_id), 
    FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE RESTRICT,
    FOREIGN KEY (resource_type_id) REFERENCES resource_type(id) ON DELETE CASCADE
);