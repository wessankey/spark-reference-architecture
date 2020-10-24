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

**Example Usage**

```sh
python event_producer.py --num_events 50
```

#

## **Spark Application Usage**

The Spark Structured Streaming application consumes events from Kafka and writes the events to a Delta table corresponding to the event type. Streaming DataFrames require the schema to be defined up-front, so a Redis schema registry is leveraged to lookup the schema for a particular event type.

Events are processed in discrete batches, and each batch will run through the following steps:
1. Retrieve all event types from the Redis schema registry.
2. For each event type:
    a. Retrieve the schema for the event
    b. Parse the raw event JSON into the structured event using the schema
    c. Write the batch to the Delta table in S3

**Environment Variables**

| Variable                | Description                           | Required  | Default      |
|-------------------------|---------------------------------------|-----------|--------------|
| AWS_ACCESS_KEY          | AWS access key                        | Yes       | NA           |
| AWS_SECRET_KEY          | AWS secret access key                 | Yes       | NA           |
| REDIS_HOST              | Redis host                            | No        | localhost    |
| REDIS_PORT              | Redis port                            | No        | 6379         |
| S3_BUCKET               | S3 bucket where data will be written  | Yes       | NA           |
| S3_PREFIX               | Prefix of data in S3                  | No        | delta-demo/  |
| KAFKA_BOOTSTRAP_SERVERS | Kafka boostrap server list | Yes      | NA        |              |
| KAFKA_TOPIC             | Kafka topic                           | Yes       | NA           |

#

## **Redshift Setup**

**Note** - This section assumes that you have a running Redshift cluster.

1. Run a Spark shell with the Delta dependencies using the following command:
    ```sh
    $SPARK_HOME/bin/spark-shell \
        --packages io.delta:delta-core_2.12:0.7.0,org.apache.hadoop:hadoop-aws:2.7.7 \
        --conf "spark.delta.logStore.class=org.apache.spark.sql.delta.storage.S3SingleDriverLogStore" \
        --conf "spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension" \
        --conf "spark.sql.catalog.spark_catalog=org.apache.spark.sql.delta.catalog.DeltaCatalog" \
        --conf "spark.hadoop.fs.s3a.multipart.size=104857600" \
        --conf "spark.hadoop.fs.AbstractFileSystem.s3a.impl=org.apache.hadoop.fs.s3a.S3A"
    ```
2. Run the following script to generate the Delta table manifests:
    ```scala
    import io.delta.tables.DeltaTable

    val tables = Seq("link_clicked.v1", "page_viewed.v1")
    val rootPath = "s3a://<BUCKET>/<PREFIX>"

    tables.foreach { t =>
        DeltaTable.forPath(s"$rootPath/$t")
            .generate("symlink_format_manifest")
    }
    ```
3. Run the `redshift-setup.sql` script to create the following resources:
    - External schema called `delta_bronze`
    - Extrenal table called `delta_bronze.link_clicked_v1`
    - External table called `delta_bronze.page_viewed_v1`
