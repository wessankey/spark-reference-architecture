# Spark Streaming Delta Lake Reference Architecture

![repo_log](docs/architecture.png)

## **Overview**

The project demonstrates a reference architecture for a Spark Streaming application that writes data to Delta tables. It includes the following components:
- Python script to generate and send fake clickstream events to Kafka
- Spark streaming application that consumes events from Kafka and writes to Delta tables in S3

#

## **Setup**

Boostrap the environment by running the Docker compose script:

```
docker-compose up
```

This will provision the following resources:
- Redis instance
- Single-broker Kafka instance with a topic called `raw_events`

#

## **Python Event Generation Script Usage**

The `event_producer.py` script can be used to generate an arbitrary number of fake clickstream events. The events will be sent to the specified Kafka topic.

**Script Arguments**

| Argument       | Description                               | Required  | Default        |
|----------------|-------------------------------------------|-----------|----------------|
| num_events     | Number of each type of event to generate  | No        | 10             |
| topic          | Kafka topics to send events to            | No        | raw_events     |
| brokers        | List of Kafka brokers                     | No        | localhost:9092 |

#

## **Spark Application Usage**

The Spark Structured Streaming application consumes events from Kafka and writes the events to a Delta table corresponding to the event type. Streaming DataFrames require the schema to be defined up-front, so a Redis schema registry is leveraged to lookup the schema for a particular event type.

Events are processed in discrete batches, and each batch will run through the following steps:
1. Retrieve all event types from the Redis schema registry.
2. For each event type:
    a. Retrieve the schema for the event
    b. Parse the raw event JSON into the structured event using the schema
    c. Write the batch to S3

**Environment Variables**

| Variable                | Description                           | Required  | Default   |
|-------------------------|---------------------------------------|-----------|------------
| AWS_ACCESS_KEY          | AWS access key                        | Yes       | NA        |
| AWS_SECRET_KEY          | AWS secret access key                 | Yes       | NA        |
| REDIS_HOST              | Redis host                            | No        | localhost |
| REDIS_PORT              | Redis port                            | No        | 5555      |
| S3_BUCKET               | S3 bucket where data will be written  | Yes       | NA        |
| S3_PREFIX               | Prefix of data in S3                  | No        | data/     |
| KAFKA_BOOTSTRAP_SERVERS | Kafka boostrap server list | Yes      | NA        |           |
| KAFKA_TOPIC             | Kafka topic                           | Yes       | NA        |
