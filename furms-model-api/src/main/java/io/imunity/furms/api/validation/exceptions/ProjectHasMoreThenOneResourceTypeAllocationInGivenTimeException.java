/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_types.ResourceTypeId;

import static java.lang.String.format;

public class ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException extends IllegalArgumentException {
    public final ProjectId projectId;
    public final ResourceTypeId resourceTypeId;

    public ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException(ProjectId projectId, ResourceTypeId resourceTypeId) {
        super(format("Project has more than one Resource Type allocation in (projectId: %s, resourceTypeId: %s)",
                projectId.id, resourceTypeId.id));
        this.projectId = projectId;
        this.resourceTypeId = resourceTypeId;
    }
}
