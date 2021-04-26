# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

import logging
import sys
import os
import time
import furms
from furms.msgslistener import SiteListener


if len(sys.argv) != 2:
    print("Provide Site Id as command line parameter.")
    sys.exit(1)

furms.set_stream_logger('furms.msgslistener', logging.DEBUG)

host = os.getenv('BROKER_HOST', '127.0.0.1')
brokerConfig = furms.BrokerConfiguration(
    host=os.getenv('BROKER_HOST', '127.0.0.1'), 
    port=os.getenv('BROKER_PORT', '44444'), 
    username=os.getenv('BROKER_USERNAME', 'guest'), 
    password=os.getenv('BROKER_PASSWORD', 'guest'), 
    cafile=os.getenv('CA_FILE', 'ca_certificate.pem'),
    siteid=sys.argv[1])


listeners = furms.RequestListeners()

listeners.ping_listener(lambda: time.sleep(2))

def handle_sshkey_add(request:furms.UserSSHKeyAddRequest) -> furms.UserSSHKeyAddResult:
    print("SSH key add request: %s" % request)
    return furms.UserSSHKeyAddResult(request.fenixUserId, request.uid)

def handle_sshkey_remove(request:furms.UserSSHKeyRemovalRequest) -> furms.UserSSHKeyRemovalResult:
    print("SSH key removal request: %s" % request)
    return furms.UserSSHKeyRemovalResult(request.fenixUserId, request.uid)

def handle_sshkey_update(request:furms.UserSSHKeyUpdatingRequest) -> furms.UserSSHKeyUpdateResult:
    print("SSH key update request: %s" % request)
    return furms.UserSSHKeyUpdateResult(request.fenixUserId, request.uid)

listeners.sshkey_add_listener(handle_sshkey_add)
listeners.sshkey_remove_listener(handle_sshkey_remove)
listeners.sshkey_update_listener(handle_sshkey_update)

try:
    SiteListener(config=brokerConfig, listeners=listeners).start_consuming()
except KeyboardInterrupt:
    print('Interrupted')
    try:
        sys.exit(0)
    except SystemExit:
        os._exit(0)

