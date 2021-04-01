# Development process

## Create virtual environment if not already
```
> python3 -m venv venv
```

## Activate virtual environment
```
> source venv/bin/activate
```

## Setup virtual environment if not already
```
> pip install wheel
> pip install setuptools
> pip install twine # if we want to publish library on pypi.org
> pip install pdoc3
> pip install pika
> pip install pytest==4.4.1
> pip install pytest-runner==4.4
```

## Build your library
```
python setup.py bdist_wheel
```

## Run all tests
```
> python setup.py pytest
```

## Install your library
```
pip install dist/furms-1.0.0-py3-none-any.whl
```

## Generate Documentation
```
pdoc --html furms
firefox html/furms/index.html
```

## Example of demo agent
The demo agent has been developed on top of the `furms` library and can be found in `devrunner` directory.
Configure credentials by setting the following environmental variables:
```
export BROKER_HOST=<broker-host>
export BROKER_PORT=<broker-port>
export BROKER_USERNAME=<broker-username>
export BROKER_PASSWORD=<broker-password>
```
If aforementioned variables are not present a default values takes place:
* host - 127.0.0.1
* port - 4444
* password - guest
* user - guest
```
cd devrunner
./runner.sh <name-of-queue-to-listen-to>
```
