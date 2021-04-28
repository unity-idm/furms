# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

from abc import abstractmethod

"""Definition of messages exchanged in payload between FURMS and local site."""

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


class ProtocolMessageFactory:
    """
    Takes the first key from the body, makes the lookup in current module
    for the class, which name is the same as the first key, and create
    instance of this class.
    """
    def from_json(body: dict) -> ProtocolMessage:
        protocol_message_name = next(iter(body))
        protocol_message_name_class = globals()[protocol_message_name]
        return protocol_message_name_class(**body[protocol_message_name])

