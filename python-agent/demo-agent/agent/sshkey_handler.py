# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

import furms
import logging
import os
from pathlib import Path

class SSHKeyRequestHandler:
    """
    Creates the 'ssh_keys' directory in current one with a structure of subdirectories
    that corresponds to user, and 'authorized_keys' file inside.
    FURMS ssh key requests are applied to proper authorized_keys file. e.g. the request
    to add SSH key for user with fenix id "XYZ", is translated to the '$PWD/ssh_keys/XYZ/authorized_keys'
    file update, and the given ssh key is added to it.
    """
    _logger = logging.getLogger(__name__)

    def __init__(self) -> None:
        pass

    def handle_sshkey_add(self, request:furms.UserSSHKeyAddRequest) -> furms.UserSSHKeyAddResult:
        self._logger.info("SSH key add request: %s" % request)

        UserAuthorizedKeys(request.fenixUserId).add(request.publicKey)

        return furms.UserSSHKeyAddResult(request.fenixUserId, request.uid)

    def handle_sshkey_remove(self, request:furms.UserSSHKeyRemovalRequest) -> furms.UserSSHKeyRemovalResult:
        self._logger.info("SSH key removal request: %s" % request)

        UserAuthorizedKeys(request.fenixUserId).remove(request.publicKey)

        return furms.UserSSHKeyRemovalResult(request.fenixUserId, request.uid)

    def handle_sshkey_update(self, request:furms.UserSSHKeyUpdatingRequest) -> furms.UserSSHKeyUpdateResult:
        self._logger.info("SSH key update request: %s" % request)

        UserAuthorizedKeys(request.fenixUserId).update(request.oldPublicKey, request.newPublicKey)

        return furms.UserSSHKeyUpdateResult(request.fenixUserId, request.uid)

class UserAuthorizedKeys:
    _SSH_KEYS_DIR = "ssh_keys"
    _AUTHORIZED_KEYS = "authorized_keys"

    def __init__(self, fenixUserId) -> None:
        keys_path = os.path.join(os.getcwd(), self._SSH_KEYS_DIR, fenixUserId)
        Path(keys_path).mkdir(parents=True, exist_ok=True)
        self._authorized_keys = os.path.join(keys_path, self._AUTHORIZED_KEYS)

    def add(self, publicKey):
        with open(self._authorized_keys, "a") as keys:
            keys.write(publicKey + "\n")

    def remove(self, publicKey):
        content = self._load()
        if content:
            with open(self._authorized_keys, "w") as file:
                for key in content:
                    if key.strip() != publicKey:
                        file.write(key)

    def update(self, oldPublicKey, newPublicKey):
        content = self._load()
        if content:
            with open(self._authorized_keys, "w") as file:
                for key in content:
                    if key.strip() == oldPublicKey:
                        file.write(newPublicKey + '\n')
                    else:
                        file.write(key)

    def _load(self):
        if Path(self._authorized_keys).is_file():
            file = open(self._authorized_keys, "r")
            return file.readlines()
        return []