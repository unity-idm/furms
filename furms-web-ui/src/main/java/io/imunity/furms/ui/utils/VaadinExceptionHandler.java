/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;
import static java.util.Collections.emptyMap;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.imunity.furms.api.validation.exceptions.CommunityAllocationHasProjectAllocationsRemoveValidationError;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.InfraServiceHasIndirectlyResourceCreditsRemoveValidationError;
import io.imunity.furms.api.validation.exceptions.RemovingCommunityException;
import io.imunity.furms.api.validation.exceptions.ResourceTypeHasResourceCreditsRemoveValidationError;
import io.imunity.furms.domain.site_agent.SiteAgentException;

public class VaadinExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Map<Class<? extends Exception>, String> GENERIC_KNOWN_EXCEPTIONS = Map.of(
			DuplicatedNameValidationError.class, "name.duplicated.error.message",
			RemovingCommunityException.class, "community.removing.error.message",
			InfraServiceHasIndirectlyResourceCreditsRemoveValidationError.class, "service.removing.error.message",
			ResourceTypeHasResourceCreditsRemoveValidationError.class, "resource-type.removing.error.message",
			SiteAgentException.class, "site-agent.error.message",
			CommunityAllocationHasProjectAllocationsRemoveValidationError.class, "community-allocation.removing.error.message"
			); 
	
	public static <T> Optional<T> handleExceptions(Supplier<T> supplier) {
		return handleExceptions(supplier, emptyMap());
	}

	public static <T> Optional<T> handleExceptions(Supplier<T> supplier, 
			Map<Class<? extends Exception>, String> extraMappings) {
		try {
			return Optional.ofNullable(supplier.get());
		} catch (Exception e) {
			OptionalException<Object> frontError = mapExceptionToFrontError(e, extraMappings);
			showErrorNotification(getTranslation(frontError.getException().get().getMessage()));
			return Optional.empty();
		}
	}
	
	public static void handleExceptions(Runnable runnable, 
			Map<Class<? extends Exception>, String> extraMappings){
		handleExceptions(() -> {
			runnable.run();
			return null;
		}, extraMappings);
	}
	
	public static void handleExceptions(Runnable runnable){
		handleExceptions(runnable, emptyMap());
	}

	public static <T> OptionalException<T> getResultOrException(Supplier<T> supplier){
		return getResultOrException(supplier, emptyMap());
	}

	public static <T> OptionalException<T> getResultOrException(Supplier<T> supplier, 
			Map<Class<? extends Exception>, String> extraMappings){
		try {
			return OptionalException.of(supplier.get());
		} catch (Exception e){
			return mapExceptionToFrontError(e, extraMappings);
		}
	}
	
	public static OptionalException<Void> getResultOrException(Runnable runnable){
		return getResultOrException(() -> {
			runnable.run();
			return null;
		});
	}

	public static OptionalException<Void> getResultOrException(Runnable runnable, 
			Map<Class<? extends Exception>, String> extraMappings){
		return getResultOrException(() -> {
			runnable.run();
			return null;
		}, extraMappings);
	}

	private static <T> OptionalException<T> mapExceptionToFrontError(Exception e, 
			Map<Class<? extends Exception>, String> extraMappings){
		
		String knownErrorMsg = extraMappings.getOrDefault(e.getClass(), GENERIC_KNOWN_EXCEPTIONS.get(e.getClass()));
		if (knownErrorMsg != null) { 
			LOG.debug("Handled user error: {} {}", e.getClass().getName(), e.getMessage());
			LOG.trace("Exeption caouse", e);
			return OptionalException.of(new FrontException(knownErrorMsg, e));
		} else {
			LOG.warn(e.getMessage(), e);
			return OptionalException.of(new FrontException("base.error.message", e));
		}
	}
}
