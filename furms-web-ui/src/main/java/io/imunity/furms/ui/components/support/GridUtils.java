/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.support;

import org.apache.logging.log4j.util.Strings;

import static java.lang.String.format;

public class GridUtils {

	private static final int ID_MAX_LENGTH = 8;

	public static String showIdInGrid(String id) {
		if (id == null) {
			return Strings.EMPTY;
		}
		final String formatedId = id.length() > ID_MAX_LENGTH
				? id.substring(0, ID_MAX_LENGTH - 1)
				: id;
		return format("%s...", formatedId);
	}

}
