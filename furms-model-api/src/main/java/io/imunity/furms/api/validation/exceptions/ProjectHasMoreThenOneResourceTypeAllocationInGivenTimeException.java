/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

import static java.lang.String.format;

public class ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException extends IllegalArgumentException {
    public final String projectId;
    public final String resourceTypeId;

    public ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException(String projectId, String resourceTypeId) {
        super(format("Project has more than one Resource Type allocation in (projectId: %s, resourceTypeId: %s)",
                projectId, resourceTypeId));
        this.projectId = projectId;
        this.resourceTypeId = resourceTypeId;
    }
}
