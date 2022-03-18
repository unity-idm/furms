/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.post_commit;

import io.imunity.furms.core.utils.InvokeAfterCommitEvent;
import io.imunity.furms.domain.FurmsEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PostCommitRunner {
	private final ApplicationEventPublisher publisher;

	PostCommitRunner(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	public void runAfterCommit(Runnable runnable){
		publisher.publishEvent(new InvokeAfterCommitEvent(runnable));
	}

	public void publishAfterCommit(FurmsEvent event){
		publisher.publishEvent(new InvokeAfterCommitEvent(() -> publisher.publishEvent(event)));
	}
}
