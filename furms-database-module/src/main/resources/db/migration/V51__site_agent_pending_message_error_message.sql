/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

ALTER TABLE site_agent_pending_message ADD COLUMN error_code VARCHAR(255);
ALTER TABLE site_agent_pending_message ADD COLUMN error_message VARCHAR(255);