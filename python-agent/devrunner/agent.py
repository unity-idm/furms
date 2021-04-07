# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

import logging
import sys
import os
import time
from furms import model, msgslistener, set_stream_logger

if len(sys.argv) != 2:
    print("Provide queue name as command line parameter.")
    sys.exit(1)

set_stream_logger('furms.msgslistener', logging.DEBUG)

host = os.getenv('BROKER_HOST', '127.0.0.1')
brokerConfig = model.BrokerConfiguration(
    host=os.getenv('BROKER_HOST', '127.0.0.1'), 
    port=os.getenv('BROKER_PORT', '44444'), 
    username=os.getenv('BROKER_USERNAME', 'guest'), 
    password=os.getenv('BROKER_PASSWORD', 'guest'), 
    queuename=sys.argv[1],
    cafile=os.getenv('CA_FILE', 'ca_certificate.pem'))

listeners = model.RequestListeners()

listeners.ping_listener(lambda: time.sleep(2))

try:
    msgslistener.start_consuming(config=brokerConfig, listeners=listeners)
except KeyboardInterrupt:
    print('Interrupted')
    try:
        sys.exit(0)
    except SystemExit:
        os._exit(0)
