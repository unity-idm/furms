/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UTCTimeUtilsTest {
	@Test
	void shouldConvertUTCToNewYorkTime() {
		LocalDateTime localDateTime = LocalDateTime.of(2021, 1, 11, 4, 11, 22);
		ZonedDateTime convertedTime = UTCTimeUtils.convertToZoneTime(localDateTime, ZoneId.of("America/New_York"));
		assertThat(convertedTime.toLocalDateTime()).isEqualToIgnoringNanos(localDateTime.minusHours(5));
	}

	@Test
	void shouldConvertNewYorkTimeToUTC() {
		ZonedDateTime zonedLocalDateTime = ZonedDateTime.of(2021, 1, 11, 4, 11, 22, 0, ZoneId.of("America/New_York"));
		LocalDateTime convertedTime = UTCTimeUtils.convertToUTCTime(zonedLocalDateTime);
		assertThat(convertedTime).isEqualToIgnoringNanos(zonedLocalDateTime.toLocalDateTime().plusHours(5));
	}
}