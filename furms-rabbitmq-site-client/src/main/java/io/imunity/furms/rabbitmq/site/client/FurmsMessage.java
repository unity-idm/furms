/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rabbitmq.site.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate classes that defines requests sent to message broker. The
 * type value is used to set header in the message indicating kind of request
 * published to a site agent. Based in this header the site is able to
 * determine what to do with it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FurmsMessage {
	String type();
}
