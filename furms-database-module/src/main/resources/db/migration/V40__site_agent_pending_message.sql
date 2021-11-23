/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE site_agent_pending_message (
    id UUID PRIMARY KEY NOT NULL,
    site_id UUID NOT NULL,
    site_external_id VARCHAR(255) NOT NULL,
    correlation_id UUID UNIQUE NOT NULL,
    retry_count INT NOT NULL,
    json_content VARCHAR NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    ack_at TIMESTAMP,
    FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE
);

CREATE INDEX site_external_id_index ON site_agent_pending_message (site_external_id);
CREATE INDEX correlation_id_index ON site_agent_pending_message (correlation_id);
