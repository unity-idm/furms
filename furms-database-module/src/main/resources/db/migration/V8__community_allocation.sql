/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE community_allocation (
    id UUID PRIMARY KEY NOT NULL,
    community_id UUID NOT NULL,
    resource_credit_id UUID NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    amount DECIMAL NOT NULL,
    FOREIGN KEY (community_id) REFERENCES community(id),
    FOREIGN KEY (resource_credit_id) REFERENCES resource_credit(id) ON DELETE RESTRICT
);