/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.sites.SiteExternalId;

public class QueueNamesService {
	private static final String FURMS_PUB = "-furms-pub";
	private static final String SITE_PUB = "-site-pub";

	public static String getFurmsPublishQueueName(SiteExternalId externalId){
		return externalId.id + FURMS_PUB;
	}

	public static String getFurmsPublishQueueName(String externalId){
		return externalId + FURMS_PUB;
	}

	public static String getSitePublishQueueName(SiteExternalId externalId){
		return externalId.id + SITE_PUB;
	}

	public static String getSitePublishQueueName(String externalId){
		return externalId + SITE_PUB;
	}

	public static String getSiteId(String queueName){
		return queueName.replace(SITE_PUB,"").replace(FURMS_PUB, "");
	}

	public static String getOppositeDirectionQueue(String queueName){
		if(queueName.contains(SITE_PUB))
			return queueName.replace(SITE_PUB, FURMS_PUB);
		if(queueName.contains(FURMS_PUB))
			return queueName.replace(FURMS_PUB, SITE_PUB);

		return queueName;
	}
}
