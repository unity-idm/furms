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

try:
    SiteListener(config=brokerConfig, listeners=listeners).start_consuming()
except KeyboardInterrupt:
    print('Interrupted')
    try:
        sys.exit(0)
    except SystemExit:
        os._exit(0)
