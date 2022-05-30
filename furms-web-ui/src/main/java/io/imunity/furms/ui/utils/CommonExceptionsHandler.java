/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import io.imunity.furms.api.validation.exceptions.ApplicationNotExistingException;
import io.imunity.furms.api.validation.exceptions.AssignedPolicyRemovingException;
import io.imunity.furms.api.validation.exceptions.DuplicatedInvitationError;
import io.imunity.furms.api.validation.exceptions.InvalidEmailException;
import io.imunity.furms.api.validation.exceptions.ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException;
import io.imunity.furms.api.validation.exceptions.UserAlreadyHasRoleError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

public class CommonExceptionsHandler {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static Map<Class<? extends RuntimeException>, String> exceptionToNotificationMessageKey = Map.of(
		DuplicatedInvitationError.class, "invite.error.duplicate",
		UserAlreadyHasRoleError.class, "invite.error.role.own",
		InvalidEmailException.class, "invite.error.email",
		AssignedPolicyRemovingException.class, "policy.document.assigned.removing",
		ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException.class, "project.allocation.resource.type.unique.message",
		ApplicationNotExistingException.class, "application.already.not.existing"
	);

	public static void handleInDefaultWay(RuntimeException e) {
		String message = exceptionToNotificationMessageKey.get(e);
		if(message == null) {
			message = "base.error.message";
			LOG.error("Could not handle exception. ", e);
		}
		showErrorNotification(getTranslation(message));
	}
}
