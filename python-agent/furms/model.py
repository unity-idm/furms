
# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

import json

"""Abstractions to interact with service models."""

class Queues:
    """Holds the names of the queues used for communication with FURMS"""
    def __init__(self, siteid):
        self.__site_to_furms = "%s-site-pub" % siteid
        self.__furms_to_site = "%s-furms-pub" % siteid

    def furms_to_site_queue_name(self):
        return self.__furms_to_site

    def site_to_furms_queue_name(self):
        return self.__site_to_furms

class BrokerConfiguration:
    """Holds the information required to connect with broker."""
    def __init__(self, siteid, username, password, host, port, cafile=None):
        self.queues = Queues(siteid)
        self.username = username
        self.password = password
        self.host = host
        self.port = port
        """
        The cafile string, if present, is the path to a file of 
        concatenated CA certificates in PEM format.
        """
        self.cafile = cafile

    def is_ssl_enabled(self):
        return self.cafile != None


class ProtocolMessage:
    @classmethod
    def message_name(cls):
        return cls.__name__.split('.')[-1]

    def to_dict(self) -> dict:
        message = {}
        message[self.message_name()] = self.__dict__
        return message
        
    def __str__(self) -> str:
        return str(self.to_dict())

class AgentPingRequest(ProtocolMessage):
    def __init__(self) -> None:
        pass

class AgentPingAck(ProtocolMessage):
    def __init__(self) -> None:
        pass

class Header:
    def __init__(self, messageCorrelationId, version, status=None, error=None):
        self.version = version
        self.messageCorrelationId = messageCorrelationId
        self.status = status
        self.error = error

    @classmethod
    def from_json(cls, data: dict):
        return cls(**data)

    def to_dict(self) -> dict:
        return {
            key:value
            for key, value in self.__dict__.items()
            if value is not None
        }
    def __str__(self) -> str:
        return str(self.to_dict())


class Payload:
    def __init__(self, header:Header, body:ProtocolMessage):
        self.header = header
        if body == None:
            raise Exception("body must not be empty")
        self.body = body

    def __str__(self) -> str:
        payload = {}
        payload['header'] = self.header.to_dict()
        payload['body'] = self.body.to_dict()
        return json.dumps(payload)

    @classmethod
    def from_body(cls, message: str):
        data = json.loads(message)
        header = Header.from_json(data["header"])

        body = data["body"]
        request = None
        if body.get(AgentPingRequest.message_name()) is not None:
            request = AgentPingRequest()

        return cls(header, request)



class RequestListeners:
    def __init__(self):
        self.listeners = {}

    def ping_listener(self, listener):
        self.listeners[AgentPingRequest.message_name()] = listener
        return self

    def get(self, message: ProtocolMessage):
        return self.listeners.get(message.message_name(), lambda: None)
