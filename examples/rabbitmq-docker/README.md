# Example of rabbitmq running in docker.

## Overview
The `rabbitmq` directory contains:
* docker-compose configuration to start the service with UI management access
* externalized configuration of Rabbitmq service

Docker immage documentation: https://hub.docker.com/_/rabbitmq

## Operating the Rabbitmq 
* Start the docker: `docker-compose -f rabbitmq/docker-compose.yml up`
* You can access the management page: http://localhost:15672 (guest/guest by default)
* To stop the service simply Ctrl+C on the terminal when the service is running.
Note there are other options to use docker-compose e.g. to start the container in the backgroupd. Plese see the docker-compose documentation for more information.

## Example python clients
Once the service is up, you can run `sender.py` and `reveiver.py` to see Rabbitmq in action.
