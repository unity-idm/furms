# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

import pika
import logging
from furms.model import BrokerConfiguration

logger = logging.getLogger(__name__)

def start_consuming(config: BrokerConfiguration):
    plainCredentials = pika.credentials.PlainCredentials(config.username, config.password)
    connectionParams = pika.ConnectionParameters(host=config.host, port=config.port, credentials=plainCredentials)
    connection = pika.BlockingConnection(connectionParams)
    channel = connection.channel()
    channel.basic_consume(queue=config.queuename, on_message_callback=msgs_listener, auto_ack=True)
    channel.start_consuming()

def msgs_listener(ch, method, properties, body):
    logger.debug("Received ch=%r, method=%r, properties=%r, body=%r" % (ch, method, properties, body))

    furmsMessageType = properties.headers['furmsMessageType']
    logger.info("received furmsMessageType=%r" % furmsMessageType)

    responseBody = None
    if furmsMessageType == 'AgentPingRequest':
        responseBody = '"OK"'

    assertTrue(responseBody, "Unsupported messag type: %s" % furmsMessageType)

    ch.basic_publish(method.exchange, 
        routing_key=properties.reply_to, 
        properties=pika.BasicProperties(
            content_type='application/json', 
            correlation_id=properties.correlation_id,
            delivery_mode=2 # make message persistent
        ), 
        body=responseBody)
    ch.basic_ack(delivery_tag=method.delivery_tag, multiple=True)

    logger.info("response published furmsMessageType=%r, body=%r" % (furmsMessageType, responseBody))

def assertTrue(condition, message):
    if not condition:
        raise Exception(message)