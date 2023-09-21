/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

class TimeFormatUtils
{
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	static String getFormatDate(ZonedDateTime zonedDateTime) {
		return zonedDateTime.format(dateTimeFormatter) + "Z";
	}
}
