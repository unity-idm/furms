/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

ALTER TABLE site ADD COLUMN policy_id UUID;
ALTER TABLE site ADD FOREIGN KEY (policy_id) REFERENCES policy_document(id);

ALTER TABLE service ADD COLUMN policy_id UUID;
ALTER TABLE service ADD FOREIGN KEY (policy_id) REFERENCES policy_document(id);
