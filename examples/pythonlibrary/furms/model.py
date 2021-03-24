
# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

"""Abstractions to interact with service models."""

class BrokerConfiguration:
    """Holds the information required to connect with broker."""
    def __init__(self, queuename, username, password, host, port):
        self.queuename = queuename
        self.username = username
        self.password = password
        self.host = host
        self.port = port

class MessageListeners:
    """Listeners used to trigger messages arival from FURMS."""
    def __init__(self):
        self.pingListener = None

    def pingCallback(self, callbackFunction):
        self.pingListener = callbackFunction