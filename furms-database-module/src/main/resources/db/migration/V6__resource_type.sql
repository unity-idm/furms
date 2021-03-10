/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE resource_type (
    id UUID PRIMARY KEY NOT NULL,
    site_id UUID NOT NULL,
    service_id UUID NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    type VARCHAR(255) NOT NULL,
    unit VARCHAR(255) NOT NULL,
    FOREIGN KEY (site_id) REFERENCES site(id),
    FOREIGN KEY (service_id) REFERENCES service(id)
);