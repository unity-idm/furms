/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException extends IllegalArgumentException {
    public final String projectId;
    public final String resourceTypeId;

    public ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException(String projectId, String resourceTypeId) {
        super("projectId: " + projectId + " resourceTypeId: " + resourceTypeId);
        this.projectId = projectId;
        this.resourceTypeId = resourceTypeId;
    }
}
