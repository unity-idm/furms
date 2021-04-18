# Preparation of virtual environemnt for demo agent

## Create virtual environment if not already
```
python3 -m venv furms-agent-venv
```

## Activate virtual environment
```
source furms-agent-venv/bin/activate
```

## Setup virtual environment if not already
```
pip3 install -r requirments.txt
```

## Install the `furms` client library
The demo agent has been developed on top of the `furms` client library.
Let's install it in our venv:
## Build your library
```
python3 setup.py bdist_wheel
```

## Run all tests
```
python3 setup.py pytest
```

## Library installation steps
```
pip3 install dist/furms-1.0.0-py3-none-any.whl
```

## Generate Documentation
```
pdoc --html furms
firefox html/furms/index.html
```




# Running demo agent
The demo agent has been develoyyped on top of the `furms` library and can be found in `demo-agent` directory.
Configure credentials by setting the following environmental variables:
```
export BROKER_HOST=<broker-host>
export BROKER_PORT=<broker-port>
export BROKER_USERNAME=<broker-username>
export BROKER_PASSWORD=<broker-password>
export CA_FILE=<path to CA file in PEM format>
```
If aforementioned variables are not present a default values takes place:
* host - 127.0.0.1
* port - 4444
* password - guest
* user - guest
* cafile - ./ca_certificate.pem
```
cd demo-agent
./demo-agent.sh <name-of-queue-to-listen-to>
```

