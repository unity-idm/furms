# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

import pika
import logging
from furms.model import BrokerConfiguration, MessageListeners

logger = logging.getLogger(__name__)

def start_consuming(config: BrokerConfiguration, listeners: MessageListeners):

    plainCredentials = pika.credentials.PlainCredentials(config.username, config.password)
    connectionParams = pika.ConnectionParameters(host=config.host, port=config.port, credentials=plainCredentials)
    connection = pika.BlockingConnection(connectionParams)
    channel = connection.channel()
    
    channel.basic_consume(queue=config.queuename, on_message_callback=msgs_listener, auto_ack=True)

    channel.start_consuming()

def msgs_listener(ch, method, properties, body):
    logger.debug("Received ch=%r, method=%r, properties=%r, body=%r" % (ch, method, properties, body))
    ch.basic_publish(method.exchange, routing_key=properties.reply_to, body='OK')
    ch.basic_ack(delivery_tag=method.delivery_tag, multiple=True)
