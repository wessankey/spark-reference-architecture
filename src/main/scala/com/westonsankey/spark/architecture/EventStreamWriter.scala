package com.westonsankey.spark.architecture

import com.redis.RedisClient
import com.westonsankey.spark.architecture.Model.S3Destination
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, from_json}

class EventStreamWriter(redis: RedisClient, destination: S3Destination) {

  /**
   * Filter a DataFrame for events of a specific type and write
   * the filtered data as a Delta table to S3.
   *
   * @param eventType the event type being processed
   * @param schema    the schema of the event type
   * @param data      DataFrame containing raw Kafka events
   */
  def writeDataFrame(eventType: String, schema: String, data: DataFrame): Unit = {
    data
      .filter(col("parsed.eventType") === eventType)
      .withColumn("timestamp", col("parsed.eventTimestamp"))
      .withColumn("eventId", col("parsed.eventId"))
      .withColumn("event",
        from_json(col("parsed.event"), schema, Map.empty[String, String]))
      .drop("parsed", "value")
      .write
      .format("delta")
      .mode("append")
      .option("checkpointLocation", s"checkpoint/$eventType")
      .option("path", s"s3a://${destination.bucket}/${destination.prefix}/$eventType")
      .save()
  }

  /**
   * For each event type in the schema registry, retrieve the
   * event's schema and process the raw records for the event
   * type.
   *
   * @param data     DataFrame containing raw Kafka events
   * @param batchId  Spark Streaming batch ID
   */
  def filter_and_output(data: DataFrame, batchId: Long): Unit = {
    val eventTypes = redis.keys().get.map(mt => mt.get)
    data.persist()

    eventTypes.foreach { mt =>
      val schema = redis.get(mt)
      schema.foreach{ s =>
        writeDataFrame(mt, s, data)
      }
    }

    data.unpersist()
  }
}
