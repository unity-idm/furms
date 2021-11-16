/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.connection;

import elemental.json.Json;

class JsonOperationTypeParser {
	static String parse(String json){
		return Json.parse(Json.parse(json).get("body").toJson()).keys()[0];
	}
}
