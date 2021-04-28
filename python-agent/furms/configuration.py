
# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

from typing import Callable
from furms.furms_messages import UserSSHKeyAddRequest
from furms.furms_messages import UserSSHKeyAddResult
from furms.furms_messages import UserSSHKeyRemovalRequest
from furms.furms_messages import UserSSHKeyRemovalResult
from furms.furms_messages import UserSSHKeyUpdatingRequest
from furms.furms_messages import UserSSHKeyUpdateResult
from furms.furms_messages import AgentPingRequest
from furms.furms_messages import ProtocolMessage

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
