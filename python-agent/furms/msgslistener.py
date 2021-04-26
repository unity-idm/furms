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

        payload = furms.PayloadRequest.from_body(body)
        logger.info("Received payload\n%s" % str(payload))

        if isinstance(payload.body, furms.AgentPingRequest):
            self.handle_ping_request(channel, basic_deliver, payload)
        else:
            self.handle_request(channel, basic_deliver, payload)

    def handle_ping_request(self, channel, basic_deliver, payload:furms.PayloadRequest):
        pingListener = self.listeners.get(payload.body)
        pingListener()
        self.publish(channel, basic_deliver, self._response_header(payload), furms.AgentPingAck())

    def handle_request(self, channel, basic_deliver, payload:furms.PayloadRequest):
        header = self._response_header(payload)
        self.publish(channel, basic_deliver, header, payload.body.ack_message())

        listener = self.listeners.get(payload.body)
        try:
            result = listener(payload.body)
            self.publish(channel, basic_deliver, header, result)
        except Exception as e:
            logger.error("Failed to provide respons to FURMS", e)


    def publish(self, channel, basic_deliver, header:furms.Header, message:furms.ProtocolMessage):
        response = furms.PayloadResponse(header, message)
        self._publish(channel, basic_deliver, response)

    def _response_header(self, requestPayload:furms.PayloadRequest, status="OK") -> furms.Header:
        return furms.Header(requestPayload.header.messageCorrelationId, requestPayload.header.version, status=status)

    def _publish(self, channel, basic_deliver, payload:furms.PayloadResponse):
        response_body = payload.to_body()
        reply_to = self.config.queues.site_to_furms_queue_name()
        channel.basic_publish(basic_deliver.exchange, 
            routing_key=reply_to, 
            properties=pika.BasicProperties(
                content_type='application/json', 
                delivery_mode=2, # make message persistent
#                headers={'__TypeId__': 'io.imunity.furms.rabbitmq.site.models.Payload'},
            ),
            body=response_body)
        logger.info("response published to %s body=%r" % (reply_to, response_body))


