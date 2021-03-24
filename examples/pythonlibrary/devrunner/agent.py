# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

import logging
import sys
import os
from furms import model, msgslistener, set_stream_logger

if len(sys.argv) != 2:
    print("Provide queue name as command line parameter.")
    sys.exit(1)

set_stream_logger('furms.msgslistener', logging.DEBUG)

brokerConfig = model.BrokerConfiguration(
    host='localhost', port='44444', 
    username='guest', password='guest', 
    queuename=sys.argv[1])


try:
    msgslistener.start_consuming(config=brokerConfig, listeners=None)
except KeyboardInterrupt:
    print('Interrupted')
    try:
        sys.exit(0)
    except SystemExit:
        os._exit(0)
