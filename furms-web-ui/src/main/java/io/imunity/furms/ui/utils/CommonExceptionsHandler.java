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

	private final static Map<Class<? extends RuntimeException>, String> exceptionToNotificationMessageKey = Map.of(
		DuplicatedInvitationError.class, "invite.error.duplicate",
		UserAlreadyHasRoleError.class, "invite.error.role.own",
		InvalidEmailException.class, "invite.error.email",
		AssignedPolicyRemovingException.class, "policy.document.assigned.removing",
		ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException.class, "project.allocation.resource.type.unique.message",
		ApplicationNotExistingException.class, "application.already.not.existing"
	);

	/**
	 * @return true if exception handled by default way, otherwise false
	 */
	public static boolean showExceptionBasedNotificationError(RuntimeException e, String errorDescription) {
		boolean handled = true;
		String message = exceptionToNotificationMessageKey.get(e.getClass());
		if(message == null) {
			message = "base.error.message";
			LOG.error(errorDescription);
			LOG.warn("No human readable message defined for exception. ", e);
			handled = false;
		}
		showErrorNotification(getTranslation(message));
		return handled;
	}
}
