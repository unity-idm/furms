/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

CREATE TABLE policy_document (
    id UUID PRIMARY KEY NOT NULL,
    site_id UUID NOT NULL,
    name VARCHAR(255),
    workflow INT CONSTRAINT policy_document_workflow_range CHECK (workflow = 0 OR workflow = 1),
    revision INT,
    content_type INT CONSTRAINT policy_document_content_type_range CHECK (content_type = 0 OR content_type = 1),
    html_text TEXT,
    file BYTEA,
    file_type VARCHAR(255),
    CONSTRAINT policy_document_consistency CHECK (
        (content_type = 0 AND html_text IS NOT NULL AND file IS NULL AND file_type IS NULL) OR
        (content_type = 1 AND html_text IS NULL AND file IS NOT NULL AND file_type IS NOT NULL)
    ),
    FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE
);