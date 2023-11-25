# Event-Driven Scaling with KEDA
KEDA (Kubernetes Event Driven Autoscaler) allows us to scale applications based on events.
This repository showcases KEDA by scaling our application based on an AWS SQS queue.

## Project setup

Our setup contains three components:

1. `localstack`, for providing a AWS SQS queue.
    [LocalStack](https://www.localstack.cloud/) emulates AWS cloud locally.
2. `producer`, which puts a random task descriptions on the queue.
    The producer is essentially a performance load test, deployed as a `Job`.
3. `consumer`, reading tasks from the queue.
   This consumer is what KEDA will scale.

## Instructions
Run it yourself using the instructions in [DEMO.md](DEMO.md).