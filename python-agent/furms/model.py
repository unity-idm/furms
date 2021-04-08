
# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

"""Abstractions to interact with service models."""

class BrokerConfiguration:
    """Holds the information required to connect with broker."""
    def __init__(self, queuename, username, password, host, port, cafile=None):
        self.queuename = queuename
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

class ProtocolRequestTypes:
    """Defines all FURMS protocol request types"""
    PING = 'AgentPingRequest'

class RequestListeners:
    def __init__(self):
        self.listeners = {}

    def ping_listener(self, listener):
        self.listeners[ProtocolRequestTypes.PING] = listener
        return self

    def get(self, requestType):
        if requestType in self.listeners:
            return self.listeners[requestType]
        else:
            return lambda: None

class MessageHeaders:
    """Internal representation of message headers"""
    def __init__(self):
        self.status = None
        self.msgType = None

    def type(self, msgType):
        self.msgType = msgType
        return self
    
    def ok_status(self):
        self._set_status('OK')
        return self

    def in_progress_status(self):
        self._set_status('IN_PROGRESS')
        return self

    def _set_status(self, status):
        self.status = status