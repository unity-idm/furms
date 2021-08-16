/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.utils;

import java.time.*;

import static java.time.ZoneOffset.UTC;
import static java.util.Optional.ofNullable;

public class UTCTimeUtils {
	public static ZonedDateTime convertToZoneTime(LocalDateTime utcLocalDateTime, ZoneId zoneId){
		return ofNullable(utcLocalDateTime)
			.map(time -> time.atOffset(ZoneOffset.UTC))
			.map(time -> time.atZoneSameInstant(zoneId))
			.orElse(null);
	}

	public static ZonedDateTime convertToZoneTime(Instant instant) {
		return ofNullable(instant)
				.map(inst -> inst.atZone(ZoneOffset.UTC))
				.orElse(null);
	}

	public static LocalDateTime convertToUTCTime(ZonedDateTime zonedDateTime){
		return ofNullable(zonedDateTime)
			.map(ZonedDateTime::toOffsetDateTime)
			.map(time -> time.withOffsetSameInstant(ZoneOffset.UTC))
			.map(OffsetDateTime::toLocalDateTime)
			.orElse(null);
	}

	public static LocalDateTime convertToUTCTime(OffsetDateTime offsetDateTime){
		return ofNullable(offsetDateTime)
			.map(time -> time.withOffsetSameInstant(ZoneOffset.UTC))
			.map(OffsetDateTime::toLocalDateTime)
			.orElse(null);
	}

	public static ZonedDateTime convertToUTCZoned(LocalDateTime localDateTime){
		return ofNullable(localDateTime)
				.map(time -> ZonedDateTime.of(time, UTC))
				.orElse(null);
	}

	public static boolean isExpired(LocalDateTime utcEndTime) {
		return utcEndTime != null
				&& LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).isAfter(utcEndTime);
	}
}
