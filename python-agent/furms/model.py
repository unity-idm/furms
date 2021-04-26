
# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

from abc import abstractmethod
from typing import Callable
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
    """
    Common boilerplate for all protocol messages
    """
    @classmethod
    def message_name(cls):
        return cls.__name__.split('.')[-1]

    def to_dict(self) -> dict:
        message = {}
        message[self.message_name()] = self.__dict__
        return message
        
    def __str__(self) -> str:
        return str(self.to_dict())

class ProtocolRequestMessage(ProtocolMessage):
    @abstractmethod
    def ack_message(self): raise NotImplementedError


###################################################
# SSK keys messages
###################################################
class UserSSHKeyAddAck(ProtocolMessage):
    def __init__(self) -> None:
        pass

class UserSSHKeyAddRequest(ProtocolRequestMessage):
    def __init__(self, fenixUserId, uid, publicKey) -> None:
        self.fenixUserId = fenixUserId
        self.uid = uid
        self.publicKey = publicKey
    def ack_message(self):
        return UserSSHKeyAddAck()

class UserSSHKeyAddResult(ProtocolMessage):
    def __init__(self, fenixUserId, uid) -> None:
        self.fenixUserId = fenixUserId
        self.uid = uid

class UserSSHKeyRemovalAck(ProtocolMessage):
    def __init__(self) -> None:
        pass

class UserSSHKeyRemovalRequest(ProtocolRequestMessage):
    def __init__(self, fenixUserId, uid, publicKey) -> None:
        self.fenixUserId = fenixUserId
        self.uid = uid
        self.publicKey = publicKey
    def ack_message(self):
        return UserSSHKeyRemovalAck()

class UserSSHKeyRemovalResult(ProtocolMessage):
    def __init__(self, fenixUserId, uid) -> None:
        self.fenixUserId = fenixUserId
        self.uid = uid

class UserSSHKeyUpdateAck(ProtocolMessage):
    def __init__(self) -> None:
        pass

class UserSSHKeyUpdatingRequest(ProtocolRequestMessage):
    def __init__(self, fenixUserId, uid, oldPublicKey, newPublicKey) -> None:
        self.fenixUserId = fenixUserId
        self.uid = uid
        self.oldPublicKey = oldPublicKey
        self.newPublicKey = newPublicKey
    def ack_message(self):
        return UserSSHKeyUpdateAck()

class UserSSHKeyUpdateResult(ProtocolMessage):
    def __init__(self, fenixUserId, uid) -> None:
        self.fenixUserId = fenixUserId
        self.uid = uid

###################################################
# Ping messages
###################################################
class AgentPingRequest(ProtocolRequestMessage):
    def __init__(self) -> None:
        pass
    def ack_message(self):
        return AgentPingAck()

class AgentPingAck(ProtocolMessage):
    def __init__(self) -> None:
        pass


###################################################
# Protocol messages
###################################################
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

class PayloadRequest:
    def __init__(self, header:Header, body:ProtocolRequestMessage):
        self.header = header
        if body == None:
            raise Exception("body must not be empty")
        self.body = body

    @classmethod
    def from_body(cls, message: str):
        data = json.loads(message)
        header = Header.from_json(data["header"])
        request = ProtocolMessageFactory.from_json(data["body"])
        return cls(header, request)

    def __str__(self) -> str:
        return "[header]: %s\n[body]: %s" % \
            (json.dumps(self.header.to_dict(), indent=2),
            json.dumps(self.body.to_dict(), indent=2),)

class PayloadResponse:
    def __init__(self, header:Header, body:ProtocolMessage):
        self.header = header
        if body == None:
            raise Exception("body must not be empty")
        self.body = body

    def to_body(self) -> str:
        payload = {}
        payload['header'] = self.header.to_dict()
        payload['body'] = self.body.to_dict()
        return json.dumps(payload)

class ProtocolMessageFactory:
    """
    Takes the first key from the body, makes the lookup in current module
    for the class, which name is the same as the first key, and create
    instance of this class.
    """
    def from_json(body: dict) -> ProtocolMessage:
        protocol_message_name = next(iter(body))
        protocol_message_name_class = getattr(__import__(__name__), protocol_message_name)
        return protocol_message_name_class(**body[protocol_message_name])


class RequestListeners:
    def __init__(self):
        self.listeners = {}

    def ping_listener(self, listener):
        self.listeners[AgentPingRequest.message_name()] = listener
        return self

    def sshkey_add_listener(self, listener: Callable[[UserSSHKeyAddRequest], UserSSHKeyAddResult]):
        self.listeners[UserSSHKeyAddRequest.message_name()] = listener
        return self

    def sshkey_remove_listener(self, listener: Callable[[UserSSHKeyRemovalRequest], UserSSHKeyRemovalResult]):
        self.listeners[UserSSHKeyRemovalRequest.message_name()] = listener
        return self

    def sshkey_update_listener(self, listener: Callable[[UserSSHKeyUpdatingRequest], UserSSHKeyUpdateResult]):
        self.listeners[UserSSHKeyUpdatingRequest.message_name()] = listener
        return self

    def get(self, message: ProtocolMessage):
        return self.listeners.get(message.message_name(), lambda: None)
