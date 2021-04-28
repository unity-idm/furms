
# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

import json
from furms.furms_messages import ProtocolRequestMessage
from furms.furms_messages import ProtocolMessageFactory
from furms.furms_messages import ProtocolMessage

"""
Abstraction that provides Payload definition with serialization and deserialization capabilities.
"""

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
        payload = {}
        payload['header'] = self.header.to_dict()
        payload['body'] = self.body.to_dict()
        return json.dumps(payload, indent=2)

class PayloadResponse:
    def __init__(self, header:Header, body:ProtocolMessage):
        self.header = header
        if body == None:
            raise Exception("body must not be empty")
        self.body = body

    def to_body(self, indent=0) -> str:
        payload = {}
        payload['header'] = self.header.to_dict()
        payload['body'] = self.body.to_dict()
        return json.dumps(payload, indent=indent)

    def __str__(self) -> str:
        return self.to_body(indent=2)
