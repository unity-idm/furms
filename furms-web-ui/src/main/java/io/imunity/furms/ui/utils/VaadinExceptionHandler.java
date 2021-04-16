/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import io.imunity.furms.api.validation.exceptions.*;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Supplier;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

public class VaadinExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static <T> Optional<T> handleExceptions(Supplier<T> supplier){
		try {
			return Optional.ofNullable(supplier.get());
		}catch (DuplicatedNameValidationError e){
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("name.duplicated.error.message"));
			return Optional.empty();
		}catch (RemovingCommunityException e){
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("community.removing.error.message"));
			return Optional.empty();
		}
		catch (InfraServiceHasIndirectlyResourceCreditsRemoveValidationError e){
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("service.removing.error.message"));
			return Optional.empty();
		}
		catch (ResourceTypeHasResourceCreditsRemoveValidationError e){
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("resource-type.removing.error.message"));
			return Optional.empty();
		}
		catch (SiteAgentException e){
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("site-agent.error.message"));
			return Optional.empty();
		}
		catch (CommunityAllocationHasProjectAllocationsRemoveValidationError e){
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("community-allocation.removing.error.message"));
			return Optional.empty();
		}
		catch (Exception e){
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("base.error.message"));
			return Optional.empty();
		}
	}

	public static void handleExceptions(Runnable runnable){
		handleExceptions(() -> {
			runnable.run();
			return null;
		});
	}

	public static <T> OptionalException<T> getResultOrException(Supplier<T> supplier){
		try {
			return OptionalException.of(supplier.get());
		}catch (DuplicatedNameValidationError e){
			LOG.error(e.getMessage(), e);
			return OptionalException.of(new FrontException("name.duplicated.error.message", e));
		}catch (RemovingCommunityException e){
			LOG.error(e.getMessage(), e);
			return OptionalException.of(new FrontException("community.removing.error.message", e));
		}
		catch (InfraServiceHasIndirectlyResourceCreditsRemoveValidationError e){
			LOG.error(e.getMessage(), e);
			return OptionalException.of(new FrontException("service.removing.error.message", e));
		}
		catch (ResourceTypeHasResourceCreditsRemoveValidationError e){
			LOG.error(e.getMessage(), e);
			return OptionalException.of(new FrontException("resource-type.removing.error.message", e));
		}
		catch (CommunityAllocationHasProjectAllocationsRemoveValidationError e){
			LOG.error(e.getMessage(), e);
			return OptionalException.of(new FrontException("community-allocation.removing.error.message", e));
		}
		catch (Exception e){
			LOG.error(e.getMessage(), e);
			return OptionalException.of(new FrontException("base.error.message", e));
		}
	}

	public static OptionalException<Void> getResultOrException(Runnable runnable){
		return getResultOrException(() -> {
			runnable.run();
			return null;
		});
	}
	
}
