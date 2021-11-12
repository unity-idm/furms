/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE site_agent_pending_message (
    id UUID PRIMARY KEY NOT NULL,
    site_external_id VARCHAR(255) NOT NULL,
    correlation_id UUID NOT NULL,
    retry_amount INT NOT NULL,
    json_content VARCHAR NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    ack_at TIMESTAMP
);
