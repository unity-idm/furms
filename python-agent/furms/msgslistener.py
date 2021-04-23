# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

import pika
import functools
import logging
import ssl
import furms

logger = logging.getLogger(__name__)


class SiteListener:
    def __init__(self, config: furms.BrokerConfiguration, listeners: furms.RequestListeners) -> None:
        self.config = config
        self.listeners = listeners

    def start_consuming(self):
        connection = pika.BlockingConnection(self._connection_params())
        channel = connection.channel()

        channel.basic_consume(self.config.queues.furms_to_site_queue_name(), self.on_message, auto_ack=True)
        channel.start_consuming()
    
    def _connection_params(self):
        plain_credentials = pika.credentials.PlainCredentials(self.config.username, self.config.password)
        ssl_options = None
        if self.config.is_ssl_enabled():
            context = ssl.SSLContext(ssl.PROTOCOL_TLSv1_2)
            context.verify_mode = ssl.CERT_REQUIRED
            context.load_verify_locations(self.config.cafile)
            ssl_options = pika.SSLOptions(context)

        return pika.ConnectionParameters(
            host=self.config.host, 
            port=self.config.port, 
            credentials=plain_credentials, 
            ssl_options=ssl_options)      
    
    def on_message(self, channel, basic_deliver, properties, body):
        logger.debug("Received \nbody=%r\nbasic_deliver=%r, \nproperties=%r" % (body, basic_deliver, properties))

        payload = furms.Payload.from_body(body)
        logger.info("Received payload=%r" % str(payload))

        if isinstance(payload.body, furms.AgentPingRequest):
            self.handleAgentPingRequest(channel, basic_deliver, payload)


    def handleAgentPingRequest(self, channel, basic_deliver, payload:furms.Payload):
        pingListener = self.listeners.get(payload.body)
        pingListener()

        header = furms.Header(payload.header.messageCorrelationId, payload.header.version, status="OK")
        response = furms.Payload(header, furms.AgentPingAck())
        self.publish(channel, basic_deliver, response)

    def publish(self, channel, basic_deliver, payload:furms.Payload):
        response_body = str(payload)
        reply_to = self.config.queues.site_to_furms_queue_name()
        channel.basic_publish(basic_deliver.exchange, 
            routing_key=reply_to, 
            properties=pika.BasicProperties(
                content_type='application/json', 
                delivery_mode=2, # make message persistent
            ),
            body=response_body)
        logger.info("response published to %s body=%r" % (reply_to, response_body))


