# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

import pika
import functools
import logging
import ssl
from furms.model import BrokerConfiguration, MessageHeaders, RequestListeners, ProtocolRequestTypes

logger = logging.getLogger(__name__)

def start_consuming(config: BrokerConfiguration, listeners:RequestListeners):
    connection = pika.BlockingConnection(connection_params(config))
    channel = connection.channel()

    on_message_callback = functools.partial(msgs_listener, args=(listeners))
    channel.basic_consume(queue=config.queuename, on_message_callback=on_message_callback, auto_ack=True)
    channel.start_consuming()

def connection_params(config: BrokerConfiguration):
    plain_credentials = pika.credentials.PlainCredentials(config.username, config.password)
    context = None
    if config.is_ssl_enabled():
        context = ssl.SSLContext(ssl.PROTOCOL_TLSv1_2)
        context.verify_mode = ssl.CERT_REQUIRED
        context.load_verify_locations(config.cafile)

    return pika.ConnectionParameters(
        host=config.host, 
        port=config.port, 
        credentials=plain_credentials, 
        ssl_options=pika.SSLOptions(context))        

def msgs_listener(channel, method, properties, body, args):
    (listeners) = args
    logger.debug("Received ch=%r, method=%r, properties=%r, body=%r" % (channel, method, properties, body))

    furmsMessageType = properties.headers['furmsMessageType']
    logger.info("received furmsMessageType=%r" % furmsMessageType)

    channel.basic_ack(delivery_tag=method.delivery_tag, multiple=True)

    if furmsMessageType == ProtocolRequestTypes.PING:
        pingListener = listeners.get(ProtocolRequestTypes.PING) 
        handleAgentPingRequest(channel, method, properties, pingListener)


def handleAgentPingRequest(channel, method, properties, pingListener):
    headers = MessageHeaders().type(ProtocolRequestTypes.PING)
    publish(channel, method, properties.reply_to, responseProperties(properties, headers.in_progress_status()))
    pingListener()
    publish(channel, method, properties.reply_to, responseProperties(properties, headers.ok_status()))


def publish(channel, method, reply_to, properties:pika.BasicProperties, responseBody=''):
    channel.basic_publish(method.exchange, 
        routing_key=reply_to, 
        properties=properties,
        body=responseBody)
    logger.info("response published properties=%r, body=%r" % (properties, responseBody))

def responseProperties(properties, headers:MessageHeaders):
    return pika.BasicProperties(
        content_type='application/json', 
        correlation_id=properties.correlation_id,
        delivery_mode=2, # make message persistent
        headers={
            'status': headers.status, 
            'furmsMessageType': headers.msgType
        }
    )