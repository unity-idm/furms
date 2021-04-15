FROM rabbitmq:3.8.14


COPY ./certs/ /etc/rabbitmq/certs
RUN chown -R rabbitmq:rabbitmq /etc/rabbitmq/certs
COPY ./rabbitmq.conf /etc/rabbitmq
